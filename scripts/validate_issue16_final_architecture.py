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
PHASE11 = ROOT / "docs/architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md"
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
    "architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md",
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
    return bool(cells) and all(re.fullmatch(r":?-{3,}:?", cell) for cell in cells)


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
                    cells = cells[: len(headers)]
                rows.append((j + 1, dict(zip(headers, cells))))
                j += 1
            yield headers, rows
            i = max(j, i + 2)
        else:
            i += 1


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
    targets: set[str] = set()
    github_links = 0
    wrong_repo = 0
    broken = 0

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
                    wrong_repo += 1
                    fail(f"{path.relative_to(ROOT)}: unexpected GitHub repository link -> {raw}")
            rel = markdown_link_target(raw)
            if rel is None:
                continue
            checked += 1
            resolved = ROOT / rel.lstrip("/") if rel.startswith("/") else path.parent / rel
            resolved = Path(os.path.normpath(resolved))
            targets.add(str(resolved))
            if not resolved.exists():
                broken += 1
                line = text.count("\n", 0, match.start()) + 1
                fail(f"{path.relative_to(ROOT)}:{line}: broken relative link -> {raw}")

    print(
        f"LINK_AUDIT relative_links={checked} unique_targets={len(targets)} "
        f"github_links={github_links} wrong_repo_links={wrong_repo} broken={broken}"
    )


def validate_repository_status() -> None:
    texts: dict[str, str] = {}
    for path in CURRENT_DOCS:
        if not path.is_file():
            fail(f"current status document missing: {path.relative_to(ROOT)}")
            continue
        texts[str(path.relative_to(ROOT))] = path.read_text(encoding="utf-8")
    if len(texts) != len(CURRENT_DOCS):
        return

    required_tokens = {
        "README.md": ["docs/SOFTWARE_ARCHITECTURE.md", "Issue #16", "Phase 11", "DONE", "UNBLOCKED / READY"],
        "docs/DELIVERY_WORKFLOW.md": ["Issue #16", "Phase 11", "Done", "Issue #4", "UNBLOCKED / READY"],
        "docs/TECHNICAL_DECISIONS.md": ["ADR-0007", "ADR-0008", "Phase 11", "Issue #16", "UNBLOCKED / READY"],
        "docs/DESIGN_DELIVERY_PLAN.md": ["Gate C", "Phase 11", "PASS / COMPLETE / MERGED"],
        "docs/adr/README.md": ["ADR-0007", "ADR-0008", "Phase 11", "PR #46", "no new ADR"],
        "docs/SOFTWARE_ARCHITECTURE.md": ["FINAL / APPROVED / MERGED", "PHASE 11 PASS", "PHASE11_FINAL_ACCEPTANCE_AUDIT.md", "UNBLOCKED / READY"],
    }
    for rel, tokens in required_tokens.items():
        for token in tokens:
            if token not in texts[rel]:
                fail(f"{rel}: missing final-status token {token!r}")

    combined = "\n".join(texts.values())
    stale_patterns = [
        r"Issue #16[^\n]*\bIn Progress\b",
        r"Issue #16[^\n]*(final acceptance/merge/closeout[^\n]*pending|final exit[^\n]*pending)",
        r"Issue #4[^\n]*\bBlocked\b",
        r"BASELINE REVIEWED THROUGH PHASE 10 / FINAL EXIT-MERGE PENDING",
        r"Current through Issue #16 Phase 10",
        r"final Software Architecture remains a separate approval gate",
    ]
    hits = [pattern for pattern in stale_patterns if re.search(pattern, combined, re.I)]
    if hits:
        fail(f"current documents contain stale final-status patterns: {hits}")

    software = texts["docs/SOFTWARE_ARCHITECTURE.md"]
    missing_links = [target for target in REQUIRED_SOFTWARE_LINKS if target not in software]
    if missing_links:
        fail(f"SOFTWARE_ARCHITECTURE.md missing required architecture links: {missing_links}")

    print(
        f"REPOSITORY_STATUS_AUDIT current_docs={len(CURRENT_DOCS)} stale_hits={len(hits)} "
        f"required_arch_links={len(REQUIRED_SOFTWARE_LINKS)-len(missing_links)}/{len(REQUIRED_SOFTWARE_LINKS)}"
    )


def validate_mermaid() -> None:
    standalone: list[str] = []
    for path in REQUIRED_MERMAID:
        if not path.is_file():
            fail(f"missing Mermaid source: {path.relative_to(ROOT)}")
            continue
        text = path.read_text(encoding="utf-8").strip()
        standalone.append(text)
        first = next((line.strip() for line in text.splitlines() if line.strip()), "")
        if not re.match(r"^(flowchart|graph|sequenceDiagram|stateDiagram(?:-v2)?|classDiagram|erDiagram)\b", first):
            fail(f"{path.relative_to(ROOT)}: invalid Mermaid declaration {first!r}")
        if "```" in text:
            fail(f"{path.relative_to(ROOT)}: standalone Mermaid contains Markdown fence")

    software = SOFTWARE.read_text(encoding="utf-8") if SOFTWARE.is_file() else ""
    inline = [block.strip() for block in re.findall(r"```mermaid\s*\n(.*?)\n```", software, re.S)]
    if len(inline) != 8:
        fail(f"SOFTWARE_ARCHITECTURE.md: expected 8 inline Mermaid blocks, found {len(inline)}")
    else:
        unmatched = list(inline)
        for source in standalone:
            if source in unmatched:
                unmatched.remove(source)
            else:
                fail("standalone Mermaid source is not byte-equivalent to an inline block")
        if unmatched:
            fail(f"{len(unmatched)} inline Mermaid block(s) have no standalone match")
    print(f"MERMAID_PARITY standalone={len(standalone)}/8 inline={len(inline)}/8")


def validate_review_and_phase11() -> None:
    for path in (REVIEW_MD, FINDINGS_JSON, CORRECTIONS_MD, PHASE11):
        if not path.is_file():
            fail(f"review/final artifact missing: {path.relative_to(ROOT)}")
    if any("review/final artifact missing" in error for error in errors):
        return

    try:
        findings = json.loads(FINDINGS_JSON.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"invalid findings JSON: {exc}")
        return

    summary = findings.get("summary", {})
    reviewer = findings.get("reviewer", {})
    approvals = {str(row.get("role")): str(row.get("result")) for row in findings.get("approvals", [])}
    required_roles = {
        "Product",
        "Android architecture/feasibility",
        "Design/UX and navigation",
        "QA/testability",
        "Accessibility architecture",
        "Privacy/telemetry",
    }
    required_final = {"Product", "Android architecture/feasibility", "Design/UX and navigation", "QA/testability"}

    if findings.get("status") != "PASS":
        fail("Phase 10 findings status is not PASS")
    if summary.get("blockerOpen") != 0 or summary.get("majorOpen") != 0:
        fail(f"open Blocker/Major is not zero: {summary.get('blockerOpen')}/{summary.get('majorOpen')}")
    if summary.get("majorFound") != summary.get("majorResolved"):
        fail("Major found/resolved counts do not match")
    if set(approvals) != required_roles:
        fail(f"review role set mismatch: {sorted(set(approvals) ^ required_roles)}")
    final_approved = sum(approvals.get(role, "").startswith("APPROVED_AI_ASSISTED") for role in required_final)
    if final_approved != 4:
        fail(f"required final approvals are {final_approved}/4")
    if reviewer.get("humanIdentityClaimed") is not False or reviewer.get("independentHumanSignatureClaimed") is not False:
        fail("reviewer metadata permits fabricated human/independent signature claim")
    if findings.get("source", {}).get("persianAuthorityUsed") is not False:
        fail("findings source metadata does not exclude Persian authority")

    for finding in findings.get("findings", []):
        if finding.get("severity") in {"Blocker", "Major"} and finding.get("status") != "RESOLVED":
            fail(f"{finding.get('id')}: unresolved {finding.get('severity')}")
        if finding.get("severity") == "Minor" and finding.get("status") != "RESOLVED":
            if not nonempty(str(finding.get("owner", ""))) or not nonempty(str(finding.get("downstreamGate", ""))):
                fail(f"{finding.get('id')}: open Minor lacks owner/downstream gate")

    corrections = CORRECTIONS_MD.read_text(encoding="utf-8")
    for token in ["PH10-MAJ-001", "PH10-MAJ-002", "VoiceConfirmationOption", "PlaybackSnapshot", "No new ADR"]:
        if token not in corrections:
            fail(f"contract corrections missing token {token!r}")

    phase11 = PHASE11.read_text(encoding="utf-8")
    checked = len(re.findall(r"(?m)^- \[x\] ", phase11))
    for token in ["PASS / COMPLETE / MERGED / IMPLEMENTATION-READY", "PR #46", "33319820646", "0d3b8890ee37bd19ccbd2a7b0658f5b5d5745f2b", "UNBLOCKED / READY"]:
        if token not in phase11:
            fail(f"Phase 11 audit missing final token {token!r}")
    if checked != 14:
        fail(f"Phase 11 audit Acceptance Criteria count is {checked}, expected 14")

    print(
        "FINAL_REVIEW_AUDIT "
        f"roles={len(approvals)}/6 required_final_approvals={final_approved}/4 "
        f"blocker_open={summary.get('blockerOpen')} major_open={summary.get('majorOpen')} "
        f"acceptance_criteria={checked}/14 human_signature_claim={reviewer.get('independentHumanSignatureClaimed')}"
    )


def validate_traceability() -> None:
    if not TRACE.is_file():
        fail(f"missing {TRACE.relative_to(ROOT)}")
        return
    text = TRACE.read_text(encoding="utf-8")
    expected = re.search(r"\b(\d+)\s+traceability rows\b", text)
    expected_rows = int(expected.group(1)) if expected else None
    catalog: dict[str, dict[str, str]] = {}
    referenced: set[str] = set()
    p0_ids: set[str] = set()
    trace_rows = 0
    p0_rows = 0
    missing = 0

    for headers, rows in iter_tables(text):
        if "Recovery" not in headers or "Test" not in headers:
            continue
        has_priority = "Priority" in headers
        for line_no, row in rows:
            path_cell = row.get("Architecture path", row.get("Path", ""))
            ids = set(re.findall(r"\bAP-\d{2}\b", path_cell))
            referenced.update(ids)
            if row.get("Path", "").startswith("AP-") and not has_priority:
                catalog[row["Path"]] = row
            if has_priority and ids:
                trace_rows += 1
                if "P0" in row.get("Priority", ""):
                    p0_rows += 1
                    p0_ids.update(ids)
                if not nonempty(row.get("Recovery", "")) or not nonempty(row.get("Test", "")):
                    missing += 1
                    fail(f"{TRACE.relative_to(ROOT)}:{line_no}: traceability row lacks Recovery/Test")

    expected_catalog = {f"AP-{index:02d}" for index in range(1, 13)}
    if set(catalog) != expected_catalog:
        fail(f"architecture path catalog mismatch: {sorted(set(catalog) ^ expected_catalog)}")
    if referenced - set(catalog):
        fail(f"unknown architecture IDs referenced: {sorted(referenced - set(catalog))}")
    for path_id in p0_ids:
        row = catalog.get(path_id, {})
        if not nonempty(row.get("Recovery", "")) or not nonempty(row.get("Test", "")):
            fail(f"P0 architecture path {path_id} lacks Recovery/Test seam")
    if expected_rows is not None and trace_rows != expected_rows:
        fail(f"traceability row count mismatch: expected {expected_rows}, parsed {trace_rows}")

    print(
        f"TRACEABILITY_AUDIT catalog={len(catalog)} referenced_ids={len(referenced)} "
        f"traceability_rows={trace_rows} p0_rows={p0_rows} p0_paths={','.join(sorted(p0_ids))} "
        f"missing_test_or_recovery={missing}"
    )


def validate_scope_diff() -> None:
    try:
        changed = subprocess.check_output(
            ["git", "diff", "--name-only", f"{BASE_SHA}...HEAD"], cwd=ROOT, text=True, stderr=subprocess.STDOUT
        ).splitlines()
    except subprocess.CalledProcessError as exc:
        fail(f"scope diff failed: {exc.output.strip()}")
        return

    allowed_extra = {
        ".github/workflows/issue-16-architecture-validation.yml",
        "scripts/validate_issue16_architecture.py",
        "scripts/validate_issue16_final_architecture.py",
    }
    unauthorized = [path for path in changed if not path.startswith("docs/") and path != "README.md" and path not in allowed_extra]
    if unauthorized:
        fail(f"scope diff contains unauthorized paths: {unauthorized}")

    forbidden = re.compile(
        r"\b(Android Auto|CarPlay|iOS|backend|account|cloud sync|streaming|online catalog|wake word|always[- ]listening|LLM|humming|recommendations?)\b",
        re.I,
    )
    negative = re.compile(
        r"\b(no|not|never|forbid(?:den)?|prohibit(?:ed)?|exclude(?:d)?|absent|out[- ]of[- ]scope|non[- ]goal|disabled|without|must not|may not|blocked)\b",
        re.I,
    )
    known = re.compile(
        r"(introducing backend/account/streaming|open-ended/LLM intent expansion|generic backend/account/streaming clients|"
        r"generic core/data -> backend/account/streaming client|provider credentials, account identifiers|Explicit exclusions|"
        r"Future discovery.*after Beta|Out of scope|Streaming, downloads, accounts|No backend/account/streaming|"
        r"No product backend|No product services|Android Auto/iOS platform expansion|remote/cloud STT activation|"
        r"provider-specific cloud STT|cloud speech remains disabled)", re.I,
    )
    suspicious: list[str] = []
    try:
        diff = subprocess.check_output(
            ["git", "diff", "--unified=0", f"{BASE_SHA}...HEAD", "--", "README.md", "docs"], cwd=ROOT, text=True, stderr=subprocess.STDOUT
        )
        for line in diff.splitlines():
            if not line.startswith("+") or line.startswith("+++"):
                continue
            content = line[1:].strip()
            if forbidden.search(content) and not negative.search(content) and "ADR-0006" not in content:
                suspicious.append(content)
    except subprocess.CalledProcessError as exc:
        fail(f"semantic scope scan failed: {exc.output.strip()}")
        return
    unreviewed = [line for line in suspicious if not known.search(line)]
    if unreviewed:
        fail("unreviewed possible positive scope additions:\n  - " + "\n  - ".join(unreviewed[:20]))

    print(
        f"SCOPE_AUDIT changed_files={len(changed)} unauthorized_paths={len(unauthorized)} "
        f"semantic_hits={len(suspicious)} preclassified_negative_context={len(suspicious)-len(unreviewed)} "
        f"unreviewed_positive_scope_lines={len(unreviewed)}"
    )


def main() -> int:
    validate_links()
    validate_repository_status()
    validate_mermaid()
    validate_review_and_phase11()
    validate_traceability()
    validate_scope_diff()

    if errors:
        print("\nISSUE_16_FINAL_ARCHITECTURE_VALIDATION=FAIL")
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    print("\nISSUE_16_FINAL_ARCHITECTURE_VALIDATION=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
