#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import re
import subprocess
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
TRACE = ROOT / "docs/architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md"
SOFTWARE = ROOT / "docs/SOFTWARE_ARCHITECTURE.md"
REVIEW_MD = ROOT / "docs/architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md"
FINDINGS_JSON = ROOT / "docs/architecture/review/ISSUE_16_FINDINGS_REGISTER.json"
CORRECTIONS_MD = ROOT / "docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md"
BASE_SHA = os.environ.get("ISSUE16_BASE_SHA", "281e1abda39a78ee0d80051c154ed6eb4af33361")

CURRENT_DOCS = [
    ROOT / "README.md",
    ROOT / "docs/DELIVERY_WORKFLOW.md",
    ROOT / "docs/TECHNICAL_DECISIONS.md",
    ROOT / "docs/DESIGN_DELIVERY_PLAN.md",
    ROOT / "docs/adr/README.md",
    SOFTWARE,
]

REQUIRED_MERMAID = [
    ROOT / "docs/architecture/diagrams/01-system-context.mmd",
    ROOT / "docs/architecture/diagrams/02-container-component-architecture.mmd",
    ROOT / "docs/architecture/diagrams/03-dependency-direction.mmd",
    ROOT / "docs/architecture/diagrams/04-state-ownership.mmd",
    ROOT / "docs/architecture/diagrams/05-playback-ownership-lifecycle.mmd",
    ROOT / "docs/architecture/diagrams/06-navigation-voice-overlay.mmd",
    ROOT / "docs/architecture/diagrams/07-critical-data-flows.mmd",
    ROOT / "docs/architecture/diagrams/08-privacy-telemetry-boundary.mmd",
]

REQUIRED_SOFTWARE_LINKS = [
    "architecture/SOURCE_AND_AUTHORITY_REGISTER.md",
    "architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md",
    "architecture/DECISION_AND_SPIKE_REGISTER.md",
    "architecture/ADR_PHASE4_AUDIT.md",
    "architecture/PHASE5_ARCHITECTURE_VIEW_AUDIT.md",
    "architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md",
    "architecture/PHASE6_IMPLEMENTATION_READINESS_AUDIT.md",
    "architecture/RUNTIME_POLICIES.md",
    "architecture/PHASE7_RUNTIME_POLICY_AUDIT.md",
    "architecture/ISSUE_16_VALIDATION.md",
    "architecture/PHASE9_REPOSITORY_SYNC_AUDIT.md",
    "architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md",
    "architecture/review/ISSUE_16_FINDINGS_REGISTER.json",
    "architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md",
    "architecture/diagrams/01-system-context.mmd",
    "architecture/diagrams/02-container-component-architecture.mmd",
    "architecture/diagrams/03-dependency-direction.mmd",
    "architecture/diagrams/04-state-ownership.mmd",
    "architecture/diagrams/05-playback-ownership-lifecycle.mmd",
    "architecture/diagrams/06-navigation-voice-overlay.mmd",
    "architecture/diagrams/07-critical-data-flows.mmd",
    "architecture/diagrams/08-privacy-telemetry-boundary.mmd",
]

errors: list[str] = []
warnings: list[str] = []


def fail(message: str) -> None:
    errors.append(message)


def nonempty(cell: str) -> bool:
    value = re.sub(r"\s+", " ", cell.strip())
    return value not in {"", "—", "-", "N/A", "n/a", "TBD", "TODO"}


def split_md_row(line: str) -> list[str]:
    raw = line.strip()
    if not (raw.startswith("|") and raw.endswith("|")):
        return []
    return [part.strip() for part in raw[1:-1].split("|")]


def is_separator(line: str) -> bool:
    cells = split_md_row(line)
    return bool(cells) and all(re.fullmatch(r":?-{3,}:?", c) for c in cells)


def iter_tables(text: str):
    lines = text.splitlines()
    i = 0
    while i + 1 < len(lines):
        headers = split_md_row(lines[i])
        if headers and is_separator(lines[i + 1]):
            rows = []
            j = i + 2
            while j < len(lines):
                cells = split_md_row(lines[j])
                if not cells:
                    break
                if len(cells) < len(headers):
                    cells += [""] * (len(headers) - len(cells))
                elif len(cells) > len(headers):
                    fail(f"{TRACE.relative_to(ROOT)}:{j+1}: row has {len(cells)} cells for {len(headers)} headers")
                    cells = cells[: len(headers)]
                rows.append((j + 1, dict(zip(headers, cells))))
                j += 1
            yield i + 1, headers, rows
            i = max(j, i + 2)
        else:
            i += 1


def validate_mermaid_sources() -> None:
    declarations = re.compile(
        r"^(flowchart|graph|sequenceDiagram|stateDiagram(?:-v2)?|classDiagram|erDiagram|journey|gantt|pie|mindmap|timeline)\b"
    )
    source_texts: list[str] = []
    for path in REQUIRED_MERMAID:
        if not path.is_file():
            fail(f"missing Mermaid source: {path.relative_to(ROOT)}")
            continue
        text = path.read_text(encoding="utf-8").strip()
        source_texts.append(text)
        first = next((ln.strip() for ln in text.splitlines() if ln.strip()), "")
        if not declarations.match(first):
            fail(f"{path.relative_to(ROOT)}: invalid/unknown Mermaid declaration: {first!r}")
        if "```" in text:
            fail(f"{path.relative_to(ROOT)}: standalone .mmd must not contain Markdown fences")
        subgraphs = len(re.findall(r"(?m)^\s*subgraph\b", text))
        ends = len(re.findall(r"(?m)^\s*end\s*$", text))
        if subgraphs != ends:
            fail(f"{path.relative_to(ROOT)}: subgraph/end imbalance {subgraphs}/{ends}")
        if text.count('"') % 2:
            fail(f"{path.relative_to(ROOT)}: unbalanced double quotes")

    if not SOFTWARE.is_file():
        fail(f"missing {SOFTWARE.relative_to(ROOT)}")
        return
    software = SOFTWARE.read_text(encoding="utf-8")
    inline = [block.strip() for block in re.findall(r"```mermaid\s*\n(.*?)\n```", software, re.S)]
    if len(inline) != len(REQUIRED_MERMAID):
        fail(f"{SOFTWARE.relative_to(ROOT)}: expected {len(REQUIRED_MERMAID)} inline Mermaid blocks, found {len(inline)}")
    else:
        unmatched = list(inline)
        for path, source in zip(REQUIRED_MERMAID, source_texts):
            if source in unmatched:
                unmatched.remove(source)
            else:
                fail(f"{path.relative_to(ROOT)}: standalone source is not byte-equivalent to an inline Mermaid block")
        if unmatched:
            fail(f"{SOFTWARE.relative_to(ROOT)}: {len(unmatched)} inline Mermaid block(s) have no matching standalone source")


def markdown_link_target(raw_target: str) -> str | None:
    target = raw_target.strip()
    if target.startswith("<") and ">" in target:
        target = target[1 : target.index(">")]
    if " " in target and not target.startswith(("http://", "https://")):
        target = target.split(" ", 1)[0]
    if not target or target.startswith(("#", "mailto:", "tel:")):
        return None
    parts = urlsplit(target)
    if parts.scheme in {"http", "https"}:
        return None
    path = unquote(parts.path)
    return path or None


def validate_links() -> None:
    active_files = list(CURRENT_DOCS)
    active_files += sorted((ROOT / "docs/architecture").rglob("*.md"))
    active_files += sorted((ROOT / "docs/adr").glob("*.md"))
    active_files = list(dict.fromkeys(active_files))
    link_re = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")
    checked = 0
    unique_targets: set[str] = set()
    github_links = 0
    wrong_repo_links = 0

    for path in active_files:
        if not path.is_file():
            fail(f"link-audit input missing: {path.relative_to(ROOT)}")
            continue
        text = path.read_text(encoding="utf-8")
        for match in link_re.finditer(text):
            raw = match.group(1).strip()
            if raw.startswith("https://github.com/"):
                github_links += 1
                if not raw.startswith("https://github.com/latticeward-labs/music-copilot/"):
                    wrong_repo_links += 1
                    fail(f"{path.relative_to(ROOT)}: unexpected GitHub repository link -> {raw}")
            rel = markdown_link_target(raw)
            if rel is None:
                continue
            checked += 1
            resolved = ROOT / rel.lstrip("/") if rel.startswith("/") else path.parent / rel
            resolved = Path(os.path.normpath(resolved))
            unique_targets.add(str(resolved))
            if not resolved.exists():
                line = text.count("\n", 0, match.start()) + 1
                fail(f"{path.relative_to(ROOT)}:{line}: broken relative link -> {raw}")

    print(
        f"LINK_AUDIT relative_links={checked} unique_targets={len(unique_targets)} "
        f"github_links={github_links} wrong_repo_links={wrong_repo_links} "
        f"broken={sum('broken relative link' in e for e in errors)}"
    )


def validate_repository_status() -> None:
    missing_docs = [str(p.relative_to(ROOT)) for p in CURRENT_DOCS if not p.is_file()]
    if missing_docs:
        fail(f"current status documents missing: {missing_docs}")
        return

    texts = {str(p.relative_to(ROOT)): p.read_text(encoding="utf-8") for p in CURRENT_DOCS}
    combined = "\n".join(texts.values())

    required_tokens = {
        "README.md": ["docs/SOFTWARE_ARCHITECTURE.md", "Issue #15", "Done / Closed", "Issue #16", "Phase 10", "In Progress"],
        "docs/DELIVERY_WORKFLOW.md": ["Issue #3", "Done / Closed", "Issue #15", "Issue #16", "Phase 10"],
        "docs/TECHNICAL_DECISIONS.md": ["ADR-0007", "ADR-0008", "SOFTWARE_ARCHITECTURE.md", "Phase 10"],
        "docs/DESIGN_DELIVERY_PLAN.md": ["PASS / COMPLETE / CLOSED", "Gate C", "Phase 10"],
        "docs/adr/README.md": ["ADR-0007", "ADR-0008", "Phase 10", "no new ADR"],
        "docs/SOFTWARE_ARCHITECTURE.md": ["Version:", "Status:", "Updated:", "Tracking:", "Phase 10", "ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md"],
    }
    for rel, tokens in required_tokens.items():
        for token in tokens:
            if token not in texts[rel]:
                fail(f"{rel}: missing current-status token {token!r}")

    stale_patterns = [
        r"Issue #3[^\n]*\*\*In Progress\*\*",
        r"Issue #15[^\n]*(Ready; not yet approved|IN PROGRESS|approval pending)",
        r"Issue #15 remains open",
        r"Issue #16[^\n]*Phase 9[^\n]*(final cross-role review|review pending)",
        r"BASELINE COMPLETE THROUGH PHASE 9 / FINAL REVIEW PENDING",
        r"final Software Architecture remains a separate approval gate",
    ]
    stale_hits: list[str] = []
    for pattern in stale_patterns:
        if re.search(pattern, combined, re.I):
            stale_hits.append(pattern)
    if stale_hits:
        fail(f"current documents contain stale status patterns: {stale_hits}")

    software = texts["docs/SOFTWARE_ARCHITECTURE.md"]
    missing_links = [target for target in REQUIRED_SOFTWARE_LINKS if target not in software]
    if missing_links:
        fail(f"SOFTWARE_ARCHITECTURE.md missing required diagram/matrix/review links: {missing_links}")

    print(
        f"REPOSITORY_STATUS_AUDIT current_docs={len(CURRENT_DOCS)} "
        f"stale_hits={len(stale_hits)} required_arch_links={len(REQUIRED_SOFTWARE_LINKS)-len(missing_links)}/{len(REQUIRED_SOFTWARE_LINKS)}"
    )


def validate_phase10_review() -> None:
    for path in (REVIEW_MD, FINDINGS_JSON, CORRECTIONS_MD):
        if not path.is_file():
            fail(f"Phase 10 review artifact missing: {path.relative_to(ROOT)}")
    if errors and any("Phase 10 review artifact missing" in e for e in errors):
        return

    review_text = REVIEW_MD.read_text(encoding="utf-8")
    corrections = CORRECTIONS_MD.read_text(encoding="utf-8")
    try:
        data = json.loads(FINDINGS_JSON.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"{FINDINGS_JSON.relative_to(ROOT)}: invalid JSON: {exc}")
        return

    if data.get("status") != "PASS":
        fail("Phase 10 findings register status is not PASS")
    reviewer = data.get("reviewer", {})
    if reviewer.get("humanIdentityClaimed") is not False or reviewer.get("independentHumanSignatureClaimed") is not False:
        fail("Phase 10 findings register fabricates or permits a human/independent signature claim")
    if "AI-assisted" not in str(reviewer.get("type", "")):
        fail("Phase 10 reviewer type must explicitly say AI-assisted")
    if data.get("source", {}).get("persianAuthorityUsed") is not False:
        fail("Phase 10 source register must explicitly exclude Persian/Farsi product authority")

    summary = data.get("summary", {})
    if summary.get("blockerOpen") != 0:
        fail(f"Phase 10 open Blocker count must be 0, found {summary.get('blockerOpen')}")
    if summary.get("majorOpen") != 0:
        fail(f"Phase 10 open Major count must be 0, found {summary.get('majorOpen')}")
    if summary.get("majorFound") != summary.get("majorResolved"):
        fail("Phase 10 Major found/resolved counts do not match")

    required_roles = {
        "Product",
        "Android architecture/feasibility",
        "Design/UX and navigation",
        "QA/testability",
        "Accessibility architecture",
        "Privacy/telemetry",
    }
    approvals = {str(row.get("role")): str(row.get("result")) for row in data.get("approvals", [])}
    missing_roles = required_roles - approvals.keys()
    if missing_roles:
        fail(f"Phase 10 missing review roles: {sorted(missing_roles)}")
    for role in required_roles:
        if role in approvals and not approvals[role].startswith("APPROVED_AI_ASSISTED"):
            fail(f"Phase 10 role {role!r} is not recorded as AI-assisted approval: {approvals[role]!r}")

    required_final = {"Product", "Android architecture/feasibility", "Design/UX and navigation", "QA/testability"}
    final_approved = sum(1 for role in required_final if approvals.get(role, "").startswith("APPROVED_AI_ASSISTED"))
    if final_approved != len(required_final):
        fail(f"required Product/Android/Design/QA approvals are {final_approved}/{len(required_final)}")

    findings = data.get("findings", [])
    open_minors = 0
    for finding in findings:
        sev = finding.get("severity")
        status = finding.get("status")
        if sev in {"Blocker", "Major"} and status != "RESOLVED":
            fail(f"{finding.get('id')}: open {sev} finding")
        if sev == "Minor" and status != "RESOLVED":
            open_minors += 1
            if not nonempty(str(finding.get("owner", ""))):
                fail(f"{finding.get('id')}: open Minor missing owner")
            if not nonempty(str(finding.get("downstreamGate", ""))):
                fail(f"{finding.get('id')}: open Minor missing downstream gate")

    for token in ["PH10-MAJ-001", "PH10-MAJ-002", "Open Blockers: **0**", "Open Majors: **0**", "4/4"]:
        if token not in review_text:
            fail(f"{REVIEW_MD.relative_to(ROOT)}: missing gate token {token!r}")
    for token in ["PH10-MAJ-001", "PH10-MAJ-002", "VoiceConfirmationOption", "PlaybackSnapshot", "No new ADR"]:
        if token not in corrections:
            fail(f"{CORRECTIONS_MD.relative_to(ROOT)}: missing correction token {token!r}")

    print(
        "PHASE10_REVIEW_AUDIT "
        f"roles={len(approvals)}/6 required_final_approvals={final_approved}/4 "
        f"blocker_open={summary.get('blockerOpen')} major_open={summary.get('majorOpen')} "
        f"minor_open={open_minors} human_signature_claim={reviewer.get('independentHumanSignatureClaimed')}"
    )


def validate_traceability() -> None:
    if not TRACE.is_file():
        fail(f"missing {TRACE.relative_to(ROOT)}")
        return

    text = TRACE.read_text(encoding="utf-8")
    expected_match = re.search(r"\b(\d+)\s+traceability rows\b", text)
    expected_rows = int(expected_match.group(1)) if expected_match else None

    catalog: dict[str, dict[str, str]] = {}
    trace_rows = 0
    p0_rows = 0
    referenced_ids: set[str] = set()
    p0_ids: set[str] = set()
    row_failures = 0

    for _, headers, rows in iter_tables(text):
        if "Recovery" not in headers or "Test" not in headers:
            continue
        has_priority = "Priority" in headers
        for line_no, row in rows:
            path_cell = row.get("Architecture path", row.get("Path", ""))
            ids = set(re.findall(r"\bAP-\d{2}\b", path_cell))
            referenced_ids.update(ids)
            if row.get("Path", "").startswith("AP-") and not has_priority:
                catalog[row["Path"]] = row
            if has_priority and ids:
                trace_rows += 1
                if "P0" in row.get("Priority", ""):
                    p0_rows += 1
                    p0_ids.update(ids)
                if not nonempty(row.get("Recovery", "")) or not nonempty(row.get("Test", "")):
                    row_failures += 1
                    fail(f"{TRACE.relative_to(ROOT)}:{line_no}: traceability row lacks Recovery or Test seam")

    expected_catalog = {f"AP-{i:02d}" for i in range(1, 13)}
    if set(catalog) != expected_catalog:
        fail(f"architecture path catalog mismatch: expected {sorted(expected_catalog)}, found {sorted(catalog)}")
    unknown = referenced_ids - set(catalog)
    if unknown:
        fail(f"traceability references unknown Architecture IDs: {sorted(unknown)}")
    for pid in sorted(p0_ids):
        row = catalog.get(pid)
        if not row:
            fail(f"P0 Architecture path {pid} missing catalog row")
            continue
        if not nonempty(row.get("Recovery", "")):
            fail(f"P0 Architecture path {pid} has no Recovery assertion")
        if not nonempty(row.get("Test", "")):
            fail(f"P0 Architecture path {pid} has no Test seam")
    if expected_rows is not None and trace_rows != expected_rows:
        fail(f"traceability row count mismatch: document says {expected_rows}, parser found {trace_rows}")

    print(
        "TRACEABILITY_AUDIT "
        f"catalog={len(catalog)} referenced_ids={len(referenced_ids)} "
        f"traceability_rows={trace_rows} p0_rows={p0_rows} p0_paths={','.join(sorted(p0_ids))} "
        f"missing_test_or_recovery={row_failures}"
    )


def validate_scope_diff() -> None:
    try:
        changed = subprocess.check_output(
            ["git", "diff", "--name-only", f"{BASE_SHA}...HEAD"], cwd=ROOT, text=True, stderr=subprocess.STDOUT
        ).splitlines()
    except subprocess.CalledProcessError as exc:
        fail(f"scope diff could not run against {BASE_SHA}: {exc.output.strip()}")
        return

    allowed_extra = {".github/workflows/issue-16-architecture-validation.yml", "scripts/validate_issue16_architecture.py"}
    unauthorized = [p for p in changed if not p.startswith("docs/") and p != "README.md" and p not in allowed_extra]
    if unauthorized:
        fail(f"scope diff contains non-document/non-validation changes: {unauthorized}")

    forbidden = re.compile(
        r"\b(Android Auto|CarPlay|iOS|backend|account|cloud sync|streaming|online catalog|wake word|always[- ]listening|LLM|humming|recommendations?)\b",
        re.I,
    )
    negative = re.compile(
        r"\b(no|not|never|forbid(?:den)?|prohibit(?:ed)?|exclude(?:d)?|absent|out[- ]of[- ]scope|non[- ]goal|disabled|without|must not|may not|blocked)\b",
        re.I,
    )
    suspicious: list[str] = []
    try:
        diff = subprocess.check_output(
            ["git", "diff", "--unified=0", f"{BASE_SHA}...HEAD", "--", "README.md", "docs"],
            cwd=ROOT, text=True, stderr=subprocess.STDOUT,
        )
        for line in diff.splitlines():
            if not line.startswith("+") or line.startswith("+++"):
                continue
            content = line[1:].strip()
            if forbidden.search(content) and not negative.search(content):
                if "ADR-0006" in content:
                    continue
                suspicious.append(content)
    except subprocess.CalledProcessError as exc:
        fail(f"semantic scope scan could not run: {exc.output.strip()}")
        return

    known_negative_context = re.compile(
        r"(introducing backend/account/streaming|open-ended/LLM intent expansion|generic backend/account/streaming clients|"
        r"generic core/data -> backend/account/streaming client|provider credentials, account identifiers|Explicit exclusions|"
        r"Future discovery.*after Beta|Out of scope|Streaming, downloads, accounts|No backend/account/streaming|"
        r"No product backend|No product services|Android Auto/iOS platform expansion|remote/cloud STT activation|"
        r"provider-specific cloud STT|cloud speech remains disabled)", re.I,
    )
    unreviewed = [line for line in suspicious if not known_negative_context.search(line)]
    if unreviewed:
        fail("unreviewed possible positive scope-creep additions detected:\n  - " + "\n  - ".join(unreviewed[:20]))

    print(
        f"SCOPE_AUDIT changed_files={len(changed)} unauthorized_paths={len(unauthorized)} "
        f"semantic_hits={len(suspicious)} preclassified_negative_context={len(suspicious)-len(unreviewed)} "
        f"unreviewed_positive_scope_lines={len(unreviewed)}"
    )


def main() -> int:
    validate_mermaid_sources()
    validate_links()
    validate_repository_status()
    validate_phase10_review()
    validate_traceability()
    validate_scope_diff()

    if errors:
        print("\nISSUE_16_ARCHITECTURE_VALIDATION=FAIL")
        for error in errors:
            print(f"ERROR: {error}")
        for warning in warnings:
            print(f"WARN: {warning}")
        return 1

    print("\nISSUE_16_ARCHITECTURE_VALIDATION=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
