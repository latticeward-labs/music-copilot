#!/usr/bin/env python3
"""Phase 13 documentary delivery preflight; NEVER an approval or issue closure gate.

Run from repository root. This checks present files and honest pending status.
Final human GitHub approvals, project field, merged SHA and issue state MUST be
verified independently from actual GitHub records before any merge or closure.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[2]
load = lambda p: json.loads((root / p).read_text(encoding="utf-8"))
gate = load("docs/design-system/ISSUE_17_PHASE_13_GATE.json")
phase12 = load("docs/design-system/ISSUE_17_FINAL_ACCEPTANCE_VALIDATION.json")
review = load("docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json")
expected_sha = "0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
errors = []
def check(cond, msg):
    if not cond: errors.append(msg)
check(gate.get("issue") == 17 and gate.get("phase") == 13, "wrong issue/phase")
check(gate.get("sourceArchiveSHA256") == expected_sha, "source package hash mismatch")
check(gate.get("baseMainSHA") == "23cb8d0fd3ed662ceac40f5a1fb97b13f99cace0", "phase12 baseline changed")
check(phase12.get("status") == "PASS_ARTIFACT_AUDIT_PENDING_HUMAN_FINAL_ACCEPTANCE", "phase12 evidence gate changed")
rows = {x["id"]: x["status"] for x in phase12.get("criteria", [])}
check(len(rows) == 14 and set(rows) == {"AC-%02d" % i for i in range(1, 15)}, "14 ACs missing or duplicate")
check(sum(s == "PASS_ARTIFACT" for s in rows.values()) == 13, "artifact pass count mismatch")
check(rows.get("AC-12") == "PENDING_HUMAN", "AC-12 expected pending independent human")
check(gate.get("criteria") == {"total":14, "artifactPass":13, "pendingHuman":1, "failed":0}, "criteria ledger drift")
check(gate.get("ac12") == rows.get("AC-12"), "AC-12 ledger drift")
check(review.get("summary",{}).get("independentHumanSignoffs") == 0, "Phase 11 recorded reviewers changed: inspect actual evidence")
check(gate.get("independentHumanApprovalsVerified") == 0, "unverified human approvals asserted")
check(gate.get("actualHumanReviewUrls") == [], "human review links added without fresh approval gate run")
check(gate.get("accessibilityPrivacyHumanReviewVerified") is False, "human A11y/Privacy falsely marked verified")
check(gate.get("status") == "DRAFT_READY_FINAL_MERGE_BLOCKED_AC12", "this is a pre-approval preflight; never a final closeout")
for key in ("mergeAuthorized","issueClosureAuthorized","finalPRMerged","issue17Completed","projectDoneVerified","designDependenciesCleared","ph11Min004Resolved"):
    check(gate.get(key) is False, key + " incorrectly marked true")
for file in gate.get("requiredDeliveryEvidence", []):
    check((root / file).is_file(), "missing delivery evidence: " + file)
for path in ("docs/design-system/ISSUE_17_PHASE_13_DELIVERY_MANIFEST.md",
             "docs/design-system/ISSUE_17_HUMAN_APPROVAL_TEMPLATE.md"):
    check((root / path).is_file(), "missing Phase13 packet: " + path)
check(gate.get("downstreamIssues") == [5,6,9,10,11], "UI-heavy issues list changed")
if errors:
    for error in errors: print("FAIL:", error)
    raise SystemExit(1)
print("PASS_DOCUMENTARY_PREFLIGHT_ONLY — AC-12 PENDING HUMAN, NO MERGE/CLOSE AUTHORIZED")
