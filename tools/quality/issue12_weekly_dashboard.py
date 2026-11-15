#!/usr/bin/env python3
"""Issue #12 P11 aggregate-only weekly dashboard CLI (stdlib, offline).

AUTHORED ONLY — no Gradle, execution, test, CI or APK before final P14.
Run manually only after the project owner authorizes verification in P14.

Usage:
  python3 tools/quality/issue12_weekly_dashboard.py \
    --input tools/quality/fixtures/issue12_dashboard_SYNTHETIC.json \
    --output /tmp/music-copilot-demo-weekly.html
"""
from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

from issue12_dashboard_core import AggregateSchemaError, report
from issue12_dashboard_render import render_html

MAX_FILE_BYTES = 64 * 1024


def unique_pairs(pairs):
    """Reject duplicate JSON object keys rather than silently overwriting."""
    result = {}
    for key, value in pairs:
        if key in result:
            raise AggregateSchemaError("JSON: duplicate key")
        result[key] = value
    return result


def reject_constant(_value):
    raise AggregateSchemaError("JSON: nonfinite numeric constant")


def load_input(path):
    # No logs, no cloud, and no raw per-user records. Schema is exact-allowlist.
    if not path.is_file() or path.stat().st_size > MAX_FILE_BYTES:
        raise AggregateSchemaError("input: missing or exceeds 64 KiB")
    try:
        text = path.read_text(encoding="utf-8")
        data = json.loads(text, object_pairs_hook=unique_pairs,
                          parse_constant=reject_constant)
    except (ValueError, UnicodeError) as exc:
        raise AggregateSchemaError("input: malformed JSON or encoding") from exc
    return report(data)


def write_private_file(path, content):
    """Explicit output path, no overwriting existing files, owner-only mode."""
    flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL
    flags |= getattr(os, "O_NOFOLLOW", 0)
    fd = os.open(path, flags, 0o600)
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as f:
            f.write(content)
    except BaseException:
        try:
            path.unlink()
        except OSError:
            pass
        raise


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Render verified-aggregate-only Issue #12 P11 weekly HTML; "
                    "synthetic fixture always DEMO_ONLY."
    )
    parser.add_argument("--input", type=Path, required=True,
                        help="strict v1 aggregate JSON, NOT individual event rows")
    parser.add_argument("--output", type=Path, required=True,
                        help="new local HTML path; existing files are never overwritten")
    parser.add_argument("--summary-output", type=Path, default=None,
                        help="optional new local report.json containing validated totals only")
    args = parser.parse_args(argv)
    destinations = [args.output]
    if args.summary_output:
        destinations.append(args.summary_output)
    try:
        if len({p.resolve() for p in [args.input] + destinations}) != 1 + len(destinations):
            raise AggregateSchemaError("paths: input/output must be distinct")
        if any(p.exists() or p.is_symlink() for p in destinations):
            raise AggregateSchemaError("output: destination already exists")
        result = load_input(args.input)
        html = render_html(result)
        write_private_file(args.output, html)
        if args.summary_output:
            # Serialize ONLY known validated report fields, not arbitrary input.
            # Fail closed on report JSON write; remove the newly created HTML.
            try:
                sanitized = json.dumps(result, indent=2, ensure_ascii=True,
                                       sort_keys=True) + "\n"
                write_private_file(args.summary_output, sanitized)
            except BaseException:
                args.output.unlink(missing_ok=True)
                raise
    except (AggregateSchemaError, OSError, ValueError, OverflowError) as exc:
        # Error content is fixed schema/path labels; never dump the JSON.
        print("Dashboard refused input/output: " + str(exc), file=sys.stderr)
        return 2
    print("Local aggregate dashboard created. Decision evidence: " +
          result["decision"])
    if result["synthetic"]:
        print("SYNTHETIC DEMO ONLY; no real pilot measurements or PASS.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
