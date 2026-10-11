#!/usr/bin/env python3
"""Issue #17 Phase 12 final acceptance audit — evidence, link and byte-integrity gates.

Running this script never signs a human approval, performs Android QA, or closes Issue #17.
The source package SHA is externally verified against the attached RAR at run setup.
"""
from __future__ import annotations
import collections
import hashlib
import json
import pathlib
import re
import sys
import urllib.parse

ROOT = pathlib.Path(".").resolve()
DOCS = ROOT / "docs"
OUT = DOCS / "design-system"
EXPECTED = "0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
REPO = "https://github.com/latticeward-labs/music-copilot"
DATE = "2026-10-11"

def read(path):
    return (ROOT / path).read_text(encoding="utf-8")
def jsonfile(path):
    return json.loads(read(path))
def exists(path):
    return (ROOT / path).is_file()
def url(path, anchor=None):
    if not path: return ""
    res = f"{REPO}/blob/main/{path}"
    return res + ("#" + anchor if anchor else "")
def evidence(paths):
    return ", ".join(f"[{pathlib.Path(p).name}]({url(p)})" for p in paths)
def check(condition, label, errors):
    if not condition: errors.append(label)

s = jsonfile("docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json")
frozen = jsonfile("docs/design-system/HIFI_TRACEABILITY_REGISTER.json")
primary = jsonfile("docs/high-fidelity/PHASE_4_PRIMARY_REGISTER.json")
frames = jsonfile("docs/wireframes/app-states/FRAME_REGISTER.json")
tokens = jsonfile("docs/design-system/foundations/TOKENS.json")
components = jsonfile("docs/design-system/components/COMPONENT_TOKENS.json")
p6 = jsonfile("docs/high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json")
p7 = jsonfile("docs/high-fidelity/phase-7/PHASE_7_REGISTER.json")
p8 = jsonfile("docs/prototype/PHASE_8_VALIDATION.json")
browser = jsonfile("docs/prototype/PHASE_8_BROWSER_VALIDATION.json")
prototype = jsonfile("docs/prototype/routes.json")
assets = jsonfile("docs/handoff/ASSET_REGISTER.json")
p9 = jsonfile("docs/handoff/PHASE9_VALIDATION.json")
p10 = jsonfile("docs/audit/phase-10/PHASE_10_VALIDATION.json")
trace = jsonfile("docs/audit/phase-10/PHASE_10_TRACEABILITY_REGISTER.json")
review = jsonfile("docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json")
p7validation = jsonfile("docs/high-fidelity/phase-7/PHASE_7_VALIDATION.json")

errors = {"source":[],"family":[],"state":[],"artifact":[],"prototype":[],"architecture":[],"scope":[],"links":[]}
checks = []
canonical = {x["id"]:x for x in s["states"]}
baseline = {x["stateId"]:x for x in frozen["states"]}
types = collections.Counter(x["type"] for x in s["states"])
priorities = collections.Counter(x["priority"] for x in s["states"])
auths = [frozen["sourceArchiveSha256"],s["authority"]["sourceArchiveSha256"],
         tokens["source"]["archiveSha256"],p7["sourceArchiveSha256"],p8["archiveSha256"],
         prototype["archiveSha256"],assets["sourceArchiveSha256"],p9["sourceArchiveSha256"],
         p10["sourceArchiveSha256"],trace["sourceArchiveSha256"]]
check(all(a==EXPECTED for a in auths),"frozen source SHA mismatch",errors["source"])
check(len(canonical)==len(baseline)==len(s["states"])==126,"missing or duplicate frozen states",errors["state"])
check(types=={"DEDICATED_HIFI":45,"HIFI_VARIANT_ANNOTATION":61,"SYSTEM_ONLY_HANDOFF":20},
      "disposition 45/61/20 mismatch",errors["state"])
check(priorities=={"P0":106,"P1":6,"P2":14},"P0/P1/P2 changed",errors["scope"])
for sid,base in baseline.items():
    row=canonical.get(sid)
    if not row:
        errors["state"].append(f"{sid} missing from current Hifi state registry");continue
    for key,expected in [("wf",base["wf"]),("uf",base["uf"]),("gs",base["gs"]),
                         ("priority",base["priority"]),("owner",base["ownership"]),
                         ("screenOwner",base["screenOwner"]),("recoveryOwner",base["recoveryOwner"])]:
        if row.get(key)!=expected:
            errors["state"].append(f"{sid}: {key} drifted from frozen Issue-15 trace")
    if base["hifiDisposition"]["type"]!=row["type"]:
        errors["state"].append(f"{sid}: disposition altered")
check(len(frames["frames"])==32,"32 approved low-fi wireframe frames missing",errors["family"])
check(len(components["components"])==23,"23 Design System components missing",errors["family"])
check(len(primary["artifacts"])==10,"10 primary screen families missing",errors["family"])
fam_ids={f"WF-{i:02d}" for i in range(1,11)}
if set(a["screenFamily"] for a in primary["artifacts"]) != fam_ids:
    errors["family"].append("WF-01..WF-10 primary family mismatch")
if set(a["id"][:5] for a in primary["artifacts"]) != {f"HF-{i:02d}" for i in range(1,11)}:
    errors["family"].append("HF-01..HF-10 primary ID mismatch")
for x in primary["artifacts"]:
    for field in ("image","spec"):
        if not exists(x[field]):errors["artifact"].append(f"missing primary {x['id']} {field}: {x[field]}")
for x in s["states"]:
    if x["type"]=="DEDICATED_HIFI":
        for field in ("image","spec"):
            if not exists(x.get(field,"")):errors["artifact"].append(f"missing dedicated {x['id']} {field}")
    elif x["type"]=="HIFI_VARIANT_ANNOTATION":
        for field in ("anchorImage","anchorSpec","spec"):
            if not exists(x.get(field,"")):errors["artifact"].append(f"missing variant {x['id']} {field}")
    else:
        if x.get("image") or x.get("appScreen") or not exists(x.get("spec","")):
            errors["artifact"].append(f"fake/missing system-only boundary {x['id']}")
check(len(p6["scenarios"])==27,"27 Driving/Voice specialist images missing",errors["family"])
for row in p6["scenarios"]:
    if not exists(row["image"]):errors["artifact"].append("missing specialist "+str(row["image"]))
check(len(p7["appVisible"])==45 and len(p7["systemOnly"])==20,
      "Phase 7 45+20 annotation counts incorrect",errors["state"])
p7id = {x["id"] for x in p7["appVisible"]}
dedicated = {x["id"] for x in s["states"] if x["type"]=="DEDICATED_HIFI"}
if p7id!=dedicated:errors["state"].append("Phase-7 app-visible state IDs diverge")
sysids = {x["id"] for x in s["states"] if x["type"]=="SYSTEM_ONLY_HANDOFF"}
if {x["id"] for x in p7["systemOnly"]}!=sysids:
    errors["state"].append("Phase-7 system-only IDs diverge")
for x in p7["appVisible"]:
    if not exists(x["spec"]) or not x["readingOrder"] or not x["actions"] or not x["nonColorOnly"] or not x["reflow200Percent"] or not x["privacyMinimized"] or x["fontScalePercent"]!=[100,150,200]:
        errors["artifact"].append("incomplete A11y/Privacy handoff: "+x["id"])
for x in p7["systemOnly"]:
    c=canonical[x["id"]]
    if x["image"] is not None or x["appScreen"] is not None or not exists(x["spec"]):
        errors["artifact"].append("native OS-only screen incorrectly rendered: "+x["id"])
    if (x["owner"],x["screenOwner"],x["recoveryOwner"])!=(c["owner"],c["screenOwner"],c["recoveryOwner"]):
        errors["architecture"].append("OS owner mismatch: "+x["id"])

# Whole-accountable asset integrity: size + actual Git blob SHA-1 + optional source SHA-256.
asset_stats={"count":len(assets["assets"]),"png":0,"svg":0,"gitBlobSha1Verified":0,
             "sourceSha256Verified":0,"missing":[],"corrupt":[]}
check(len(assets["assets"])==149,"expected 149 source assets",errors["artifact"])
check(len({a["path"] for a in assets["assets"]})==len(assets["assets"]),
      "duplicate registered asset path",errors["artifact"])
for a in assets["assets"]:
    path=a["path"]
    if not exists(path):
        asset_stats["missing"].append(path);errors["artifact"].append("asset missing "+path);continue
    b=(ROOT/path).read_bytes()
    if len(b)!=a["sizeBytes"]:
        asset_stats["corrupt"].append(path);errors["artifact"].append("size mismatch "+path)
    sha1=hashlib.sha1(b"blob "+str(len(b)).encode()+b"\0"+b).hexdigest()
    if sha1!=a["gitBlobSha1"]:
        asset_stats["corrupt"].append(path);errors["artifact"].append("Git blob mismatch "+path)
    else:asset_stats["gitBlobSha1Verified"]+=1
    if a.get("sourceSha256"):
        if hashlib.sha256(b).hexdigest()!=a["sourceSha256"]:
            asset_stats["corrupt"].append(path);errors["artifact"].append("SHA256 mismatch "+path)
        else:asset_stats["sourceSha256Verified"]+=1
    if a["format"]=="PNG":
        asset_stats["png"]+=1
        if b[:8]!=b"\x89PNG\r\n\x1a\n" or len(b)<24 or int.from_bytes(b[16:20],"big")<1 or int.from_bytes(b[20:24],"big")<1:
            asset_stats["corrupt"].append(path);errors["artifact"].append("corrupt PNG "+path)
    elif a["format"]=="SVG":
        asset_stats["svg"]+=1
        if b"<svg" not in b[:4000]:
            asset_stats["corrupt"].append(path);errors["artifact"].append("invalid SVG "+path)
    else:
        errors["artifact"].append("unregistered asset format "+path)

# Check 541 independently listed linked artifacts, not only images.
regpaths=[a["path"] for a in trace["registeredArtifacts"]]
check(len(regpaths)==len(set(regpaths))==541,"541-item traceability register dup/missing",errors["artifact"])
for p in regpaths:
    if not exists(p):errors["artifact"].append("registered trace asset missing: "+p)
for name in ["SCREEN_SPECS.md","COMPONENT_SPECS.md","STATE_ACTION_MATRIX.md",
             "COMPOSE_TOKEN_MAPPING.md","ASSET_REGISTER.json"]:
    if not exists("docs/handoff/"+name):errors["artifact"].append("missing Compose handoff "+name)
for name in ["PHASE9_VALIDATION.json","PHASE_10_VALIDATION.json",
             "PHASE_8_VALIDATION.json"]:
    pass
screen=read("docs/handoff/SCREEN_SPECS.md")
component=read("docs/handoff/COMPONENT_SPECS.md")
actions=read("docs/handoff/STATE_ACTION_MATRIX.md")
mapping=read("docs/handoff/COMPOSE_TOKEN_MAPPING.md")
arch=read("docs/SOFTWARE_ARCHITECTURE.md")
architecture_keys=["PlaybackGateway","MediaSessionService","VoiceSessionCoordinator","MediaStore","Room","DataStore"]
for keyword in architecture_keys:
    if keyword not in arch:errors["architecture"].append("missing architecture authority "+keyword)
for word in ["PlaybackGateway.snapshot.currentItem","SearchViewModel"]:
    if word not in component or word not in screen:
        errors["architecture"].append("PH11 resolved Major correction missing "+word)
for word in ["MediaSessionService","PlaybackGateway","VoiceSessionCoordinator","TelemetryPrivacyGate"]:
    if any(word not in z for z in [component,screen,actions]):
        errors["architecture"].append("Compose handoff runtime owner missing "+word)
for i in range(1,11):
    if len(re.findall(r"^### WF-%02d — "%i,screen,re.M))!=1:
        errors["architecture"].append("WF-%02d Compose Screen contract missing"%i)
for key in components["components"]:
    if "### "+key+" — " not in component:errors["architecture"].append("missing CMP handoff "+key)
for key in canonical:
    if "| \x60"+key+"\x60 |" not in actions:
        errors["architecture"].append("missing State Action row "+key)
if p9["status"]!="PASS_SOURCE_LOCKED_COMPOSE_HANDOFF_DOCUMENTATION":
    errors["architecture"].append("Phase-9 source validator not passed")
if any(p10["errors"][k] for k in ["missing","orphan","priority","state","scope","prototype"]):
    errors["scope"].append("Phase 10 retained structural or scope findings")
if p10["canonicalStates"]!=126 or p10["registeredSourceArtifacts"]!=541:
    errors["scope"].append("Phase 10 audit differs from source manifest")
if p8["nodeCount"]!=67 or browser["passed"]!=14 or browser["failed"]!=0:
    errors["prototype"].append("Phase 8 re-audit or Chromium smoke mismatch")
if not all(p8.get(k) is True for k in ["fiveGoldenPathsConnected","allRequiredRecoveriesReachable",
                                        "allRecoveryEndpointsProvenInOwnScenario",
                                        "noFakeAndroidSystemImages","oneRetryOneConfirmationNoApproximation",
                                        "noAutomaticRouteResume","drivingExitAlwaysAvailable"]):
    errors["prototype"].append("Golden Scenario recovery safety not fully validated")
if len(prototype["nodes"])!=67 or set(prototype["scenarios"])!={f"GS-0{i}" for i in range(1,6)}:
    errors["prototype"].append("Prototype source routes absent/misaligned")
for file in ["docs/prototype/index.html","docs/prototype/routes.js","docs/prototype/app.js",
             "docs/prototype/TEST_PLAN.md","docs/high-fidelity/phase-7/COMPOSE_A11Y_HANDOFF.md",
             "docs/high-fidelity/phase-7/PRIVACY_EVIDENCE_HANDOFF.md",
             "docs/high-fidelity/phase-7/SYSTEM_BOUNDARY_HANDOFF.md"]:
    if not exists(file):errors["artifact"].append("missing required UX handoff "+file)
summary=review["summary"]
if summary["blockerOpen"] or summary["majorOpen"] or summary["majorResolved"]!=2:
    errors["architecture"].append("Phase-11 open Major/Blocker or correction mismatch")
human_signed=summary["independentHumanSignoffs"]
if human_signed!=0:
    errors["source"].append("Human sign-off state changed: revalidate actual signed approvals before release")
if len(review["roles"])!=6:
    errors["source"].append("Six-role AI-assisted review record missing")

# Markdown relative/internal GitHub link audit for all current high-fidelity/DS/handoff/proto,
# source traceability and core authority documents. External websites are NOT claimed checked.
link_roots=[
    "docs/design-system","docs/high-fidelity","docs/handoff","docs/prototype",
    "docs/audit/phase-10","docs/wireframes/app-states","docs/wireframes/recovery-system",
    "docs/user-flows/golden-scenarios"
]
link_sources=[]
for root in link_roots:
    link_sources += sorted((ROOT/root).rglob("*.md"))
for p in ["docs/DESIGN_DELIVERY_PLAN.md","docs/SOFTWARE_ARCHITECTURE.md","docs/MVP.md","docs/PRD.md",
          "docs/ISSUE_17_REPOSITORY_HIFI_CONTRACT_ADDENDUM_2026-10-03.md"]:
    link_sources.append(ROOT/p)
link_sources=sorted(set(link_sources))
link_details={"markdownFilesAudited":len(link_sources),"internalPathLinks":0,
              "internalSameRepoGithubLinks":0,"externalLinksSkipped":0,"fragmentTargetsNotVerified":0,
              "missingTargets":[],"samples":[]}
markdown_links=re.compile(r"!?\[[^\]\n]*\]\((<?[^)\n]+>?)\)")
hrefs=re.compile(r'<(?:a|img)[^>]+(?:href|src)=["\x27]([^"\x27]+)["\x27]',re.I)
for source in link_sources:
    txt=source.read_text("utf-8")
    # ignore fenced code blocks to avoid counting sample Markdown as a real link.
    txt=re.sub(r"(?ms)^ *\x60\x60\x60.*?^ *\x60\x60\x60.*?$","",txt)
    raw_targets=[z.group(1) for z in markdown_links.finditer(txt)]
    raw_targets += [z.group(1) for z in hrefs.finditer(txt)]
    for raw in raw_targets:
        raw=raw.strip().strip("<>")
        if not raw:continue
        if " \x22" in raw:raw=raw.split(" \x22",1)[0]
        if " \x27" in raw:raw=raw.split(" \x27",1)[0]
        if raw.startswith(("mailto:","data:","javascript:","tel:")):
            link_details["externalLinksSkipped"]+=1;continue
        if raw.startswith(("#","?")):
            link_details["fragmentTargetsNotVerified"]+=1;continue
        parsed=urllib.parse.urlsplit(raw)
        candidate=None
        if parsed.scheme in ("https","http"):
            if parsed.netloc.lower()=="github.com" and parsed.path.startswith("/latticeward-labs/music-copilot/"):
                parts=parsed.path.split("/")
                if len(parts)>5 and parts[4] in ("blob","tree") and parts[5]=="main":
                    candidate=ROOT / urllib.parse.unquote("/".join(parts[6:]))
                    link_details["internalSameRepoGithubLinks"]+=1
                else:
                    link_details["externalLinksSkipped"]+=1;continue
            else:
                link_details["externalLinksSkipped"]+=1;continue
        elif parsed.scheme:
            link_details["externalLinksSkipped"]+=1;continue
        else:
            rel=urllib.parse.unquote(parsed.path)
            if not rel:link_details["fragmentTargetsNotVerified"]+=1;continue
            candidate=(source.parent / rel).resolve()
            link_details["internalPathLinks"]+=1
        if parsed.fragment:link_details["fragmentTargetsNotVerified"]+=1
        if candidate is not None:
            if candidate!=ROOT and ROOT not in candidate.parents:
                link_details["missingTargets"].append({"source":source.relative_to(ROOT).as_posix(),"target":raw,"reason":"outside repository"})
            elif not candidate.exists():
                link_details["missingTargets"].append({"source":source.relative_to(ROOT).as_posix(),"target":raw,"reason":"missing path"})
link_details["missingTargets"]=sorted(link_details["missingTargets"],key=lambda x:(x["source"],x["target"]))
for item in link_details["missingTargets"][:80]:
    errors["links"].append(f"{item['source']} -> {item['target']}")
link_details["samples"]=link_details["missingTargets"][:40]
if len(link_details["missingTargets"])>80:
    errors["links"].append(f"additional missing paths: {len(link_details['missingTargets'])-80}")

def add(id,description,condition,paths,limitation):
    checks.append({"id":id,"criterion":description,
                   "status":"PASS_ARTIFACT" if condition else "FAIL",
                   "evidencePaths":paths,"limitation":limitation})
def clean(*groups):
    return not any(errors[k] for k in groups)
add("AC-01","Visual structure preserves approved Issue-15 Wireframes and UF/State/WF mappings",
    clean("family","state","scope"),["docs/wireframes/app-states/FRAME_REGISTER.json",
                                   "docs/design-system/HIFI_TRACEABILITY_REGISTER.json",
                                   "docs/audit/phase-10/PHASE_10_VALIDATION.json"],
    "Structure/register comparison only; final human pixel-level visual approval remains open.")
add("AC-02","All 10/10 HF families, 126/126 dispositions and 45/45 dedicated app-visible designs",
    clean("family","state","artifact"),
    ["docs/high-fidelity/PHASE_4_PRIMARY_REGISTER.json",
     "docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json","docs/high-fidelity/HIFI_STATE_COVERAGE_MATRIX.md"],
    "P2 Liked Songs remains optional/removable. This is repository artifact coverage, not Android runtime.")
add("AC-03","Reusable components/variants defined in Design System",
    len(components["components"])==23 and clean("family"),
    ["docs/design-system/components/COMPONENT_TOKENS.json","docs/design-system/components/COMPONENT_CATALOG.md"],
    "Design tokens and specs only, not a compiled Compose library.")
add("AC-04","Driving single layer, stable controls, Exit and 48dp+ documented",
    clean("prototype","state") and p7validation["appWith48DpAndDrivingOverrides"]==45,
    ["docs/high-fidelity/specialist/phase-6/PHASE_6_VALIDATION.md",
     "docs/high-fidelity/phase-7/COMPOSE_A11Y_HANDOFF.md",
     "docs/prototype/PHASE_8_VALIDATION.json"],
    "Static screenshot and route contract; real vehicle usability not tested.")
add("AC-05","Driving has no typing, deep menus, long lists or distracting motion",
    clean("prototype","scope"),
    ["docs/prototype/routes.json","docs/audit/phase-10/PHASE_10_VALIDATION.json"],
    "Executable static flow, not road-distraction safety certification.")
add("AC-06","TalkBack/focus/reflow, contrast, non-color-only annotations documented",
    clean("artifact","state") and p7validation["appWithReadingOrderAndTalkback"]==45,
    ["docs/high-fidelity/phase-7/PHASE_7_REGISTER.json",
     "docs/high-fidelity/phase-7/PHASE_7_VALIDATION.json",
     "docs/high-fidelity/phase-7/COMPOSE_A11Y_HANDOFF.md"],
    "PH10-MIN-001 actual Compose TalkBack and 200% on devices is still OPEN.")
add("AC-07","Voice TTS and error copy bounded to one concise sentence",
    clean("prototype") and len(p6["scenarios"])==27,
    ["docs/high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json",
     "docs/high-fidelity/specialist/phase-6/PHASE_6_VALIDATION.md",
     "docs/prototype/PHASE_8_VALIDATION.json"],
    "Design/copy policy, not an audio provider field test.")
add("AC-08","Loading, Empty, disabled, error and permission recovery prepared for engineering",
    clean("state","artifact","architecture"),
    ["docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json",
     "docs/handoff/SCREEN_SPECS.md","docs/handoff/STATE_ACTION_MATRIX.md"],
    "Actual operating-system permission outcomes must be implemented/tested.")
add("AC-09","Five clickable Golden Scenarios GS-01..05 and all 38 scoped recovery endpoints",
    clean("prototype") and sum(v["recoveryCount"] for v in p8["scenarios"].values())==38,
    ["docs/prototype/routes.json","docs/prototype/PHASE_8_VALIDATION.json",
     "docs/prototype/PHASE_8_BROWSER_VALIDATION.json"],
    "14/14 Chromium smoke tests, not five actual Android scenarios.")
add("AC-10","Jetpack Compose-ready tokens, dimensions and component specifications",
    clean("architecture","family"),
    ["docs/design-system/foundations/TOKENS.json",
     "docs/design-system/components/COMPONENT_TOKENS.json",
     "docs/handoff/COMPOSE_TOKEN_MAPPING.md"],
    "Compose production code and runtime QA not included.")
add("AC-11","Engineering handoff specifies State/owner/action/Back/analytics/privacy",
    clean("architecture","state","artifact"),
    ["docs/handoff/STATE_ACTION_MATRIX.md",
     "docs/handoff/COMPONENT_SPECS.md",
     "docs/high-fidelity/phase-7/PRIVACY_EVIDENCE_HANDOFF.md"],
    "Contracts only, not runtime telemetry proof.")
checks.append({"id":"AC-12",
   "criterion":"Independent named Product, Design/UX, Android and QA human approval; A11y/Privacy review evidence",
   "status":"PENDING_HUMAN",
   "evidencePaths":["docs/design-system/review/ISSUE_17_CROSS_ROLE_REVIEW_2026-10-11.md",
                    "docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json",
                    "docs/design-system/review/PHASE_11_FINAL_PASS_CLOSEOUT_2026-10-11.md"],
   "limitation":"Only one AI-assisted reviewer across six roles; 0 independent humans, 0 human signatures. PH11-MIN-004 requires dated real review; cannot mark PASS."})
add("AC-13","Stable IDs, dated evidence and PRs/commits recorded in GitHub",
    clean("source","state"),
    ["docs/design-system/ISSUE_17_SOURCE_MANIFEST.md",
     "docs/handoff/ASSET_REGISTER.json",
     "docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json"],
    "Existing PRs #48–#65 merged; this Phase-12 PR must be merged separately.")
add("AC-14","Design/HiFi/prototype/handoff/behavior/architecture/traceability synchronized",
    clean("source","family","state","artifact","architecture","scope","prototype","links"),
    ["docs/audit/phase-10/PHASE_10_VALIDATION.json",
     "docs/handoff/PHASE9_VALIDATION.json",
     "docs/design-system/review/ISSUE_17_REVIEW_VALIDATION.md"],
    "Document-level source and path reconciliation; final stakeholder sign-off is AC-12, still pending.")

pass_count=sum(x["status"]=="PASS_ARTIFACT" for x in checks)
fail_count=sum(x["status"]=="FAIL" for x in checks)
pending_count=sum(x["status"]=="PENDING_HUMAN" for x in checks)
summary_status=("PASS_ARTIFACT_AUDIT_PENDING_HUMAN_FINAL_ACCEPTANCE" if fail_count==0 else
                "FAIL_ARTIFACT_AUDIT_PENDING_CORRECTIONS")
report={
 "issue":17,"phase":12,"date":DATE,"status":summary_status,"attachedArchiveSha256":EXPECTED,
 "acceptanceCriteriaTotal":len(checks),"artifactPass":pass_count,"failed":fail_count,
 "pendingHuman":pending_count,"criteria":checks,"sourcePriorityCounts":dict(priorities),
 "stateDispositions":dict(types),"sourceAssetIntegrity":asset_stats,
 "registeredTraceabilityArtifacts":len(regpaths),"prototypeNodes":len(prototype["nodes"]),
 "recoveryBranches":sum(v["recoveryCount"] for v in p8["scenarios"].values()),
 "chromiumCasesPassed":browser["passed"],"linkAudit":link_details,
 "errorGroups":errors,
 "independentHumanApprovalsRecorded":0,
 "phase11OpenMinors":["PH11-MIN-001","PH11-MIN-002","PH11-MIN-003","PH11-MIN-004"],
 "implementationAndDeviceQA":"NOT_RUN","issue17CloseAuthorized":False
}
out_json=OUT/"ISSUE_17_FINAL_ACCEPTANCE_VALIDATION.json"
out_json.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
rows=[]
for c in checks:
    ev=evidence(c["evidencePaths"])
    rows.append(f"| {c['id']} | {c['criterion']} | **{c['status']}** | {ev} | {c['limitation']} |")
errlines="\n".join(f"- **{k}:** {len(v)} finding(s)."+(" "+ "; ".join(v[:4]) if v else "")
                   for k,v in errors.items())
failedlinks="\n".join(f"- \x60{a['source']}\x60 → \x60{a['target']}\x60 ({a['reason']})"
                     for a in link_details["missingTargets"][:40])
if not failedlinks:failedlinks="- No missing internal path target detected in the audited Markdown scope."
md=f"""# Issue #17 — Phase 12 Final Acceptance Audit

**Date:** {DATE} · **Issue:** [#17]({REPO}/issues/17) · **Source:** attached English \x60MSM2(1)(1).rar\x60 SHA-256 \x60{EXPECTED}\x60.

**Audit disposition: {summary_status}.** Of **{len(checks)}** binding GitHub Acceptance Criteria: **{pass_count} verified at design/document/evidence level**, **{pending_count} pending named independent human review**, **{fail_count} failed source/artifact checks**.

**Important:** The Phase-12 *documentary/source audit* is not equivalent to final Issue #17 acceptance. AC-12 is NOT passed; actual human signoffs remain **0**. Issue #17 must remain **OPEN / In Progress**. Do not tick AC-12, authorize closure, claim release readiness or imply real Android/Media3/STT/vehicle/TalkBack testing.

## 1. Binding acceptance criteria — complete 14-point audit

Source of these exact criteria: [Issue #17]({REPO}/issues/17) § Acceptance criteria, repository-native contract superseding historical Figma requirements.

| ID | Binding Acceptance Criterion | Evidence-based decision | Direct existing GitHub evidence | Qualification / non-claim |
|---|---|---|---|---|
{chr(10).join(rows)}

## 2. Source and coverage invariants

| Verified object | Result |
|---|---|
| Uploaded English RAR archive | SHA-256 \x60{EXPECTED}\x60; equal to source-lock records |
| Approved screen families | \x60WF-01..WF-10\x60: **{len(primary["artifacts"])}/10**, paired primary PNG/Markdown |
| Frozen canonical States | **{len(s["states"])}/126**, IDs and WF/UF/GS/P0/P1/P2/owner parity checked against Phase-1 Issue-15-derived freeze |
| Dedicated app-visible HiFi State designs | **{types["DEDICATED_HIFI"]}/45** image/spec pairs |
| Component/frame variants | **{types["HIFI_VARIANT_ANNOTATION"]}/61** source-anchored specs |
| Android/System-only transitions | **{types["SYSTEM_ONLY_HANDOFF"]}/20**, zero app screen/PNG impersonations |
| Low-fidelity app frames | **{len(frames["frames"])}/32** |
| Design System component catalog | **{len(components["components"])}/23** |
| Specialist Driving/Voice artwork | **{len(p6["scenarios"])}/27** |
| App A11y/Privacy state docs | **{len(p7["appVisible"])}/45** |
| Native OS handoff specs | **{len(p7["systemOnly"])}/20** |
| Handoff registered source visual assets | **{asset_stats["count"]}/149**, {asset_stats["png"]} PNG + {asset_stats["svg"]} SVG |
| Actual Git blob checksums matched | **{asset_stats["gitBlobSha1Verified"]}/149** |
| Recorded source SHA-256 verified | **{asset_stats["sourceSha256Verified"]}** assets where authoritative hash supplied |
| Phase-10 registered trace/provenance artifacts | **{len(regpaths)}/541** |
| Golden Scenario interactive source graph | **{len(prototype["nodes"])}** nodes, \x60GS-01..GS-05\x60 |
| Scoped safe recovery branches | **{sum(v["recoveryCount"] for v in p8["scenarios"].values())}/38** source-checked |
| Actual browser click audit (Chromium only) | **{browser["passed"]}/{len(browser["cases"])}** cases |
| Frozen state priority | **P0 {priorities["P0"]}, P1 {priorities["P1"]}, P2 {priorities["P2"]}**; Liked Songs remains P2/removable |

## 3. Link audit — real repository path resolution

Checked **{link_details["markdownFilesAudited"]}** current Markdown documents across \x60docs/design-system/\x60, \x60docs/high-fidelity/\x60, \x60docs/handoff/\x60, \x60docs/prototype/\x60, \x60docs/audit/phase-10/\x60, approved Wireframe/Golden Scenario sources and core authority docs.

- Relative repository-local Markdown/image link references: **{link_details["internalPathLinks"]}**.
- Same-repository GitHub \x60blob/main\x60 and \x60tree/main\x60 references: **{link_details["internalSameRepoGithubLinks"]}**.
- External/non-main/third-party links skipped (not claimed live-verified): **{link_details["externalLinksSkipped"]}**.
- Link fragments/anchors not validated as exact heading slugs: **{link_details["fragmentTargetsNotVerified"]}**.
- Missing local path targets: **{len(link_details["missingTargets"])}**.

{failedlinks}

No false claim is made that external URLs, fragment slugs, hosting availability or PR review signatures passed path-only checks. JSON evidence: [Phase 12 machine audit]({url("docs/design-system/ISSUE_17_FINAL_ACCEPTANCE_VALIDATION.json")}).

## 4. Structural/architecture divergence and known reviewed corrections

- **Structural source comparison:** each of 126 State IDs retains its original WF, UF, GS disposition, P0/P1/P2, owner and recovery owner compared to the Issue #15-derived [frozen Phase-1 register]({url("docs/design-system/HIFI_TRACEABILITY_REGISTER.json")}). Ten HF screen families/32 approved Wireframe frames remain linked. This is a deterministic **source/document-level** check; a dated human per-image pixel review is still PH11-MIN-004.
- **Architecture source comparison:** [Issue #16 Software Architecture]({url("docs/SOFTWARE_ARCHITECTURE.md")}) remains authority. Handoff components and screens carry \x60MediaSessionService\x60, \x60PlaybackGateway\x60, \x60VoiceSessionCoordinator\x60 and \x60TelemetryPrivacyGate\x60 boundaries. Resolved Phase-11 Major 001 binds Player/Driving artwork and metadata to service-ACK \x60PlaybackGateway.snapshot.currentItem\x60; Major 002 binds ephemeral query to \x60SearchViewModel\x60 without logging/persistence. These are **spec/contract tests**, not tested implementations.
- **Safety/Privacy:** Driving remains one layer with Exit, Voice STT is limited to one eligible Retry and a bounded confirmation, actual Android-owned controls stay system-only. Raw audio, transcript, song identities, location and device addresses are prohibited in telemetry/evidence. Cloud STT must stay disabled until provider-specific consent/Legal review. Actual telemetry negative test and physical device QA are **NOT RUN**.
- **Scope:** local-first English Android MVP. No new backend/account/streaming/iOS/Android Auto/wake word/cloud catalog feature. Phase-10 structured scope scan passed; no independent exhaustive pixel/content source audit is asserted.

## 5. Severity, human review and outstanding gates

[Phase-11 severity register]({url("docs/design-system/review/ISSUE_17_FINDINGS_REGISTER.json")}) documents **0 open Blocker, 0 open Major**, with 2 corrected Majors and **4 open downstream Minors**.

| Tracking ID | Real owner / required evidence | Gate |
|---|---|---|
| \x60PH11-MIN-004\x60 | **Named independent human Product, Design/UX, Android and QA approvals** with per-primary/dedicated/specialist visual and hotspot review, reviewer identity, date, GitHub PR/review evidence. Accessibility/Privacy review evidence must also be acknowledged by responsible reviewers. | **Blocks final Issue #17 acceptance and closure — AC-12 PENDING.** |
| \x60PH11-MIN-001\x60 / \x60PH10-MIN-001\x60 | Android UI + QA: Compose TalkBack, focus/read order, 100/150/200% scaling, real 48dp/Driving 64/72/56dp. | Implementation feature DoD / release; not a pass of runtime in this audit. |
| \x60PH11-MIN-002\x60 / \x60PH10-MIN-003\x60 | Android + QA: Media3/Service, playback ACK, safe route, real Voice/OEM/Bluetooth and 3+ vehicle/head-unit test matrix. | Before release/field QA. |
| \x60PH11-MIN-003\x60 / \x60PH10-MIN-002\x60 | Product + Privacy/Legal: chosen remote-STT provider legal terms, explicit opt-in/revoke, retention/deletion and signoff. | Before any remote STT enablement; remains disabled. |

**Final rule:** report completion and source/artifact audit PASS can be recorded, but **do not mark all 14 Issue #17 checkboxes as completed or close Issue #17** until independent humans provide the required evidence. An AI six-role review is not six independent human signoffs.

## 6. Automated reproducibility / findings

- [Machine-readable acceptance and link/asset results]({url("docs/design-system/ISSUE_17_FINAL_ACCEPTANCE_VALIDATION.json")}).
- [Reproducible Phase-12 auditor]({url("docs/design-system/RUN_ISSUE_17_FINAL_ACCEPTANCE_AUDIT.py")}): from repository root run \x60python3 docs/design-system/RUN_ISSUE_17_FINAL_ACCEPTANCE_AUDIT.py\x60.
- [Prior Phase-9 handoff audit]({url("docs/handoff/PHASE9_VALIDATION.json")}), [prior Phase-10 541-asset audit]({url("docs/audit/phase-10/PHASE_10_VALIDATION.json")}), [Phase-11 six-role AI audit]({url("docs/design-system/review/ISSUE_17_REVIEW_VALIDATION.md")}).

{errlines}

**Decision:** **{summary_status}**. Phase 12 evidence audit is complete subject to the actual recorded checks; **final Issue #17 acceptance authorization remains PENDING HUMAN**.
"""
(OUT/"ISSUE_17_FINAL_ACCEPTANCE_AUDIT.md").write_text(md,encoding="utf-8")
print(json.dumps({"status":summary_status,"criteriaTotal":len(checks),"artifactPass":pass_count,
                  "failed":fail_count,"pendingHuman":pending_count,
                  "linkMissing":len(link_details["missingTargets"]),
                  "verifiedAssets":asset_stats["gitBlobSha1Verified"],
                  "errors":{k:len(v) for k,v in errors.items()}},sort_keys=True))
# Fail closed if any source/artifact/real-link gate fails (human gate never misreported as PASS).
if fail_count:
    sys.exit(1)
