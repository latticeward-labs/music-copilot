#!/usr/bin/env python3
"""Issue #5 P01 delivery contract gate. This does not test Android runtime."""
import argparse
import json
import re
from pathlib import Path

SHA = "0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
BRANCH = "feature/5-onboarding-permissions-recovery"
FILES = [
 "docs/implementation/ISSUE_5_PHASE0_TRACEABILITY.json",
 "docs/implementation/ISSUE_5_PHASE0_VALIDATION.json",
 "docs/implementation/ISSUE_5_PHASE1_GITHUB_EXECUTION.md",
 "docs/implementation/ISSUE_5_PHASE1_VALIDATION.json",
 "docs/implementation/ISSUE_5_EVIDENCE_INDEX.md",
 "docs/implementation/ISSUE_5_PHASED_EXECUTION_PLAN.md",
 ".github/PULL_REQUEST_TEMPLATE/issue-5-onboarding-permissions-recovery.md",
 ".github/workflows/issue-5-phase1-contract.yml",
 "scripts/validate_issue5_phase1.py",
 "CONTRIBUTING.md",
]

class ContractError(Exception):
    pass

def require(condition, detail):
    if not condition:
        raise ContractError(detail)

def exact_ids(rows, prefix, number):
    found = [x.get("id") for x in rows]
    want = [prefix + str(i).zfill(2) for i in range(1, number + 1)]
    require(found == want, "Duplicate or missing ID: " + prefix)

def selftest():
    for bad in ([{"id": "AC-01"}] * 13, [{"id": "AC-01"}]):
        try:
            exact_ids(bad, "AC-", 13)
        except ContractError:
            continue
        raise ContractError("Fail-closed negative fixture failed")

def validate(root):
    absent = [p for p in FILES if not (root / p).is_file()]
    require(not absent, "Missing contract files: " + repr(absent))
    p0 = json.loads((root / "docs/implementation/ISSUE_5_PHASE0_TRACEABILITY.json").read_text())
    v0 = json.loads((root / "docs/implementation/ISSUE_5_PHASE0_VALIDATION.json").read_text())
    p1 = json.loads((root / "docs/implementation/ISSUE_5_PHASE1_VALIDATION.json").read_text())
    require(p0["archive"]["sha256"] == SHA == p1["archive_sha256"], "Wrong archive SHA")
    require(len(p0["archive"]["members"]) == 12, "Expected 12 official PDFs")
    require(p0["branch"] == BRANCH == p1["branch"], "Wrong branch")
    require(p0["main_commit"] == p1["base_main_commit"], "Frozen main mismatch")
    require(len(p0["definition_of_ready"]) == 4, "DoR count")
    require(v0["repo_source_paths_checked"] == 51 and not v0["repo_source_paths_missing"], "P00 source path gate changed")
    ac = p0["acceptance_criteria"]
    exact_ids(ac, "AC-", 13)
    for row in ac:
        require(row["phase0_status"] == "MAPPED_NOT_IMPLEMENTED" and row["implementation_evidence"] is None, "False AC implementation")
        for key in ("behavior_source", "design_or_architecture_source"):
            require((root / row[key]).is_file(), "Missing AC source for " + row["id"])
        require(row["implementation_phases"] and row["test_scenarios"], "Missing phase/test AC mapping")
    tests = p0["test_scenarios"]
    exact_ids(tests, "T", 17)
    require(all(t["status"] == "PLANNED_NOT_RUN" for t in tests), "P00 tests falsely passed")
    require(p1["acceptance_mapped"] == 13 and p1["test_cases_planned"] == 17, "P01 traceability totals")
    require(p1["runtime_ac_passed"] == p1["runtime_tests_executed"] == 0, "False runtime PASS")
    require(not any(p1[x] for x in ("pr_opened","review_approved","merged","base_exception_approved")), "Unapproved governance")
    require(p1["proposed_pr_base"] == "main", "Unexpected PR base")
    text = (root / "docs/implementation/ISSUE_5_PHASE1_GITHUB_EXECUTION.md").read_text()
    index = (root / "docs/implementation/ISSUE_5_EVIDENCE_INDEX.md").read_text()
    pr = (root / ".github/PULL_REQUEST_TEMPLATE/issue-5-onboarding-permissions-recovery.md").read_text()
    plan = (root / "docs/implementation/ISSUE_5_PHASED_EXECUTION_PLAN.md").read_text()
    yml = (root / ".github/workflows/issue-5-phase1-contract.yml").read_text()
    for keyword in ("develop", "main", "independent", "P14", "13/13"):
        require(keyword.lower() in text.lower(), "Missing delivery-gate disclosure: " + keyword)
    require("develop" in (root / "CONTRIBUTING.md").read_text(), "Generic policy unexpectedly missing")
    require(len(re.findall(r"^\| T\d{2} \|", index, re.M)) == 17, "T01..T17 fixture coverage incomplete")
    require(len(re.findall(r"^\| AC-\d{2} \|", pr, re.M)) == 13, "AC-01..13 template incomplete")
    for keyword in ("Closes #5", "Do NOT claim", "TalkBack", "Privacy", "independent", "main"):
        require(keyword.lower() in pr.lower(), "PR gate missing: " + keyword)
    require("| P01 |" in plan and ("P01 ARTIFACT READY" in plan or "P01 PASS" in plan), "P01 plan not updated")
    require(BRANCH in yml and "python3 scripts/validate_issue5_phase1.py" in yml, "CI wiring missing")
    return {"status":"PASS_PHASE1_CONTRACT_ONLY","required_files":len(FILES),"archive_pdfs":12,
            "mapped_ac":13,"planned_synthetic_cases":17,"dor":4,"runtime_ac_passed":0,
            "runtime_tests_executed":0}

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path("."))
    ap.add_argument("--self-test", action="store_true")
    args = ap.parse_args()
    try:
        if args.self_test:
            selftest()
        print(json.dumps(validate(args.root), sort_keys=True))
    except (ContractError, KeyError, ValueError, OSError, TypeError) as err:
        raise SystemExit("ISSUE5_P01_CONTRACT_FAIL: " + str(err))
