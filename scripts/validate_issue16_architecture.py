#!/usr/bin/env python3
from __future__ import annotations

import os
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
TRACE = ROOT / "docs/architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md"
SOFTWARE = ROOT / "docs/SOFTWARE_ARCHITECTURE.md"
BASE_SHA = os.environ.get("ISSUE16_BASE_SHA", "281e1abda39a78ee0d80051c154ed6eb4af33361")

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
    active_files = [SOFTWARE]
    active_files += sorted((ROOT / "docs/architecture").glob("*.md"))
    active_files += sorted((ROOT / "docs/adr").glob("*.md"))
    link_re = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")
    checked = 0
    unique_targets: set[str] = set()

    for path in active_files:
        if not path.is_file():
            fail(f"link-audit input missing: {path.relative_to(ROOT)}")
            continue
        text = path.read_text(encoding="utf-8")
        for match in link_re.finditer(text):
            rel = markdown_link_target(match.group(1))
            if rel is None:
                continue
            checked += 1
            if rel.startswith("/"):
                resolved = ROOT / rel.lstrip("/")
            else:
                resolved = path.parent / rel
            resolved = Path(os.path.normpath(resolved))
            unique_targets.add(str(resolved))
            if not resolved.exists():
                line = text.count("\n", 0, match.start()) + 1
                fail(f"{path.relative_to(ROOT)}:{line}: broken relative link -> {match.group(1)}")

    print(f"LINK_AUDIT relative_links={checked} unique_targets={len(unique_targets)} broken={sum('broken relative link' in e for e in errors)}")


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
                pid = row["Path"]
                catalog[pid] = row

            if has_priority and ids:
                trace_rows += 1
                is_p0 = "P0" in row.get("Priority", "")
                if is_p0:
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
            ["git", "diff", "--name-only", f"{BASE_SHA}...HEAD"],
            cwd=ROOT,
            text=True,
            stderr=subprocess.STDOUT,
        ).splitlines()
    except subprocess.CalledProcessError as exc:
        fail(f"scope diff could not run against {BASE_SHA}: {exc.output.strip()}")
        return

    allowed_extra = {
        ".github/workflows/issue-16-architecture-validation.yml",
        "scripts/validate_issue16_architecture.py",
    }
    unauthorized = [p for p in changed if not p.startswith("docs/") and p not in allowed_extra]
    if unauthorized:
        fail(f"scope diff contains non-document/non-validation changes: {unauthorized}")

    forbidden = re.compile(
        r"\b(Android Auto|CarPlay|iOS|backend|account|cloud sync|streaming|online catalog|"
        r"wake word|always[- ]listening|LLM|humming|recommendations?)\b",
        re.I,
    )
    negative = re.compile(
        r"\b(no|not|never|forbid(?:den)?|prohibit(?:ed)?|exclude(?:d)?|absent|"
        r"out[- ]of[- ]scope|non[- ]goal|disabled|without|must not|may not)\b",
        re.I,
    )
    suspicious: list[str] = []
    try:
        diff = subprocess.check_output(
            ["git", "diff", "--unified=0", f"{BASE_SHA}...HEAD", "--", "docs"],
            cwd=ROOT,
            text=True,
            stderr=subprocess.STDOUT,
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

    if suspicious:
        fail("possible positive scope-creep additions detected:\n  - " + "\n  - ".join(suspicious[:20]))

    print(
        f"SCOPE_AUDIT changed_files={len(changed)} unauthorized_paths={len(unauthorized)} "
        f"suspicious_positive_scope_lines={len(suspicious)}"
    )


def main() -> int:
    validate_mermaid_sources()
    validate_links()
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
