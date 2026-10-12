#!/usr/bin/env python3
"""Validate owner-directed AI-assisted design readiness; do not manufacture external GitHub state."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[2]
load = lambda p: json.loads((root / p).read_text(encoding="utf-8"))
gate = load("docs/design-system/ISSUE_17_PHASE_13_GATE.json")
phase12 = load("docs/design-system/ISSUE_17_FINAL_ACCEPTANCE_VALIDATION.json")
review = load("docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json")
policy = load(gate["acceptancePolicy"])
errors = []
def check(condition, message):
    if not condition: errors.append(message)
check(gate["issue"]==17 and gate["phase"]==13, "wrong issue/phase")
check(gate["sourceArchiveSHA256"]==phase12["attachedArchiveSha256"]=="0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8", "source hash mismatch")
check(gate["baseMainSHA"]=="23cb8d0fd3ed662ceac40f5a1fb97b13f99cace0", "Phase12 baseline drift")
check(policy["ownerInstructionOriginal"]=="تأیید انسانی بعد اتمام MVP انجام خواهد شد", "explicit owner authorization missing")
check(policy["designAcceptanceMode"]=="AI_ASSISTED_SIX_ROLE_EVIDENCE_REVIEW", "wrong acceptance mode")
check(policy["humanApprovalTiming"]==gate["humanApprovalTiming"]=="AFTER_MVP_COMPLETION", "human timing drift")
check(policy["humanApprovalRequiredForIssue17Closure"] is False and gate["humanApprovalRequiredForIssue17Closure"] is False, "pre-MVP human blocker reintroduced")
check(policy["runtimeDeviceAndProviderGatesRetained"] is True, "runtime/provider gates removed")
check((root / policy["addendumPath"]).is_file(), "authority addendum missing")
check(phase12["status"]==gate["phase12Status"]=="PASS_AI_ASSISTED_DESIGN_ACCEPTANCE", "final design audit failed")
rows=phase12["criteria"]
check(len(rows)==14 and {r["id"] for r in rows}=={"AC-%02d"%i for i in range(1,15)}, "14 unique ACs required")
check(all(r["status"]=="PASS_ARTIFACT" for r in rows), "acceptance criterion failed")
check(all(not v for v in phase12["errorGroups"].values()), "artifact/source/link errors")
check(gate["criteria"]=={"total":14,"artifactPass":14,"pendingHuman":0,"failed":0} and gate["ac12"]=="PASS_ARTIFACT", "gate ledger drift")
check({r["role"] for r in review["roles"]}==set(policy["requiredReviewRoles"]) and len(review["roles"])==6, "six-role evidence missing")
check(review["summary"]["blockerOpen"]==review["summary"]["majorOpen"]==0, "open Blocker/Major")
check(review["summary"]["independentHumanSignoffs"]==gate["independentHumanApprovalsVerified"]==0, "fabricated human approvals")
check(gate["actualHumanReviewUrls"]==[] and gate["accessibilityPrivacyHumanReviewVerified"] is False, "fabricated human evidence")
f=next(x for x in review["findings"] if x["id"]=="PH11-MIN-004")
check(f["status"]=="OPEN_DOWNSTREAM_NON_BLOCKING" and f["humanApprovalTiming"]=="AFTER_MVP_COMPLETION" and f["blocksIssue17Closure"] is False, "deferred finding incorrectly closed or gated")
check(gate["ph11Min004Resolved"] is False and gate["blockingFindings"]==[], "human finding falsely resolved")
check(gate["status"]=="READY_AI_ASSISTED_DESIGN_CLOSEOUT" and gate["recordType"]=="PRE_MERGE_READINESS_SNAPSHOT", "wrong ledger type/status")
check(gate["mergeAuthorized"] is True and gate["issueClosureAuthorized"] is True and gate["finalDesignAuditPassed"] is True, "owner-directed authorization missing")
for field in ("finalHeadCIAndConflictVerified","finalPRMerged","issue17Completed","projectDoneVerified","designDependenciesCleared"):
    check(gate[field] is False, "pre-merge snapshot falsely claims external completion: "+field)
for path in gate["requiredDeliveryEvidence"]:
    check((root/path).is_file(), "delivery evidence missing: "+path)
check(gate["downstreamIssues"]==policy["designDependencies"]==[5,6,9,10,11], "design dependency list drift")
if errors:
    for e in errors: print("FAIL:",e)
    raise SystemExit(1)
print("PASS_AI_ASSISTED_DESIGN_CLOSEOUT_READINESS — verify final-head green CI, merge, actual Issue/Project state separately; human review after MVP")
