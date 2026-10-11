#!/usr/bin/env python3
"""Fail-closed Phase 7 independent contract validation (no GUI/test claims)."""
from pathlib import Path
import json,collections,hashlib
ROOT=Path("docs/high-fidelity")
OUT=ROOT/"phase-7"
r=json.loads((OUT/"PHASE_7_REGISTER.json").read_text())
s=json.loads((ROOT/"HIFI_STATE_COVERAGE_REGISTER.json").read_text())
t=json.loads(Path("docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json").read_text())
prev=json.loads((ROOT/"specialist/phase-6/PHASE_6_REGISTER.json").read_text())
m=json.loads(Path("docs/design-system/HIFI_TRACEABILITY_REGISTER.json").read_text())
byid={x["id"]:x for x in s["states"]}
apps={x["id"]:x for x in r["appVisible"]}
systems={x["id"]:x for x in r["systemOnly"]}
wantapp={x["id"] for x in s["states"] if x["type"]=="DEDICATED_HIFI"}
wantsys={x["id"] for x in s["states"] if x["type"]=="SYSTEM_ONLY_HANDOFF"}
assert m["sourceArchiveSha256"]==r["sourceArchiveSha256"]
assert len(s["states"])==126 and len(byid)==126
assert len(apps)==len(r["appVisible"])==45 and set(apps)==wantapp
assert len(systems)==len(r["systemOnly"])==20 and set(systems)==wantsys==set(t["canonicalSystemOnly"])
assert not set(apps)&set(systems)
assert len({x["id"] for x in apps.values()})==45
assert r["counts"]==dict(appVisible=45,systemOnly=20,missing=0,duplicate=0,unassigned=0,fakeAppSystemScreens=0)
for id,x in apps.items():
    src=byid[id]
    assert x["type"]=="APP_VISIBLE"
    assert x["wf"]==src["wf"] and x["uf"]==src["uf"] and x["owner"]==src["owner"]
    assert x["minimumTouchDp"]>=48 and x["drivingTransportDp"]>=64 and x["drivingMicDp"]>=72 and x["drivingExitDp"]>=56
    assert x["fontScalePercent"]==[100,150,200] and x["focusRestore"]
    assert x["privacyMinimized"] and x["qa"]=="NOT_RUN"
    assert len(x["readingOrder"])>=4 and len(x["actions"])>=1
    for field in ("spec","sourceSpec","sourceArt"):
        assert Path(x[field]).is_file(),(id,field,x[field])
    spec=Path(x["spec"]).read_text()
    for required in ("TalkBack","200%","48","Non-color-only","Privacy","NOT RUN"):
        assert required.lower() in spec.lower(),(id,required)
    if id in ("CD-08","CD-12","CD-13","CD-14","CD-15","CD-16","CD-19"):
        assert all(y["action"]!="Retry once" for y in x["actions"]),id
        assert x["sourceArt"].startswith("docs/high-fidelity/specialist/phase-6/"),id
    if id=="CC-07":
        assert any("otherwise absent" in y["talkBack"] for y in x["actions"]),id
        assert x["sourceArt"].startswith("docs/high-fidelity/specialist/phase-6/"),id
    if id in ("CD-09","CD-10"):
        assert any(y["action"]=="Candidate choice" for y in x["actions"])
for id,x in systems.items():
    src=byid[id]
    assert x["type"]=="SYSTEM_ONLY" and x["wf"]==src["wf"] and x["uf"]==src["uf"]
    for field in ("owner","screenOwner","recoveryOwner"):
        assert x[field]==src[field],(id,field)
    assert x["appScreen"] is None and x["image"] is None
    assert x["focusReturn"] and x["privacyMinimized"] and x["qa"]=="NOT_RUN"
    expected={y["id"] for y in t["transitions"] if id in y["systemStates"]}
    assert set(x["transitions"])==expected and expected
    for field in ("sourceSpec","spec"):assert Path(x[field]).is_file(),(id,field)
    content=Path(x["spec"]).read_text()
    for item in ("Platform","return","owner","NOT RUN","Privacy"):
        assert item.lower() in content.lower(),(id,item)
assert sum(bool(x.get("image")) or bool(x.get("appScreen")) for x in systems.values())==0
assert len(prev["scenarios"])==27 and prev["validation"]["ufStateCoverage"]==42
matrix=(OUT/"ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md").read_text()
assert len([x for x in matrix.splitlines() if x.startswith("| ") and x.split("|")[1].strip() in apps])==45
assert len([x for x in matrix.splitlines() if x.startswith("| ") and x.split("|")[1].strip() in systems])==20
qa=(OUT/"PHASE_7_DEVICE_QA_MATRIX.md").read_text()
assert sum(x.startswith("| A11Y-") for x in qa.splitlines())==45
assert sum(x.startswith("| OS-") for x in qa.splitlines())==20
assert "NOT RUN" in qa
for f in ("COMPOSE_A11Y_HANDOFF.md","PRIVACY_EVIDENCE_HANDOFF.md","SYSTEM_BOUNDARY_HANDOFF.md"):
    assert (OUT/f).is_file()
assert "PH10-MIN-001 remains OPEN" in (OUT/"COMPOSE_A11Y_HANDOFF.md").read_text()
checks=dict(date="2026-10-11",phase=7,issue=17,status="PASS_SPECIFICATION_45_20_PENDING_RUNTIME",
    checked126OriginalStates=126,appVisible=45,systemOnly=20,
    missing=[],duplicates=[],unassigned=[],unmodifiedSystemOwnerCount=20,
    appWithReadingOrderAndTalkback=45,appWith200PercentReflowContract=45,
    appWith48DpAndDrivingOverrides=45,appWithPrivacyEvidenceContract=45,
    systemWithOwnerReturnAndPlatformFocus=20,invalidSystemAppScreens=0,
    realCompose200PercentTested=False,realTalkBackTested=False,
    realPrivacyTelemetryTested=False,independentHumanSignoff=False,
    PH10_MIN_001="SPECIFIED_HERE_RUNTIME_QA_STILL_OPEN")
(OUT/"PHASE_7_VALIDATION.json").write_text(json.dumps(checks,indent=2)+"\n")
(OUT/"PHASE_7_VALIDATION.md").write_text(
"# Phase 7 validation — specification gate\n\n"
"**PASS**: 45/45 app-visible accessibility dispositions; 20/20 system-only owner/return handoffs; 0 missing/duplicates/unassigned; 0 system-only PNGs. Source 126-state identity unchanged.\n\n"
"Every app-visible State has explicit TalkBack reading order, control labels, non-color cues, 48dp targets (Driving 64/72/56), focus-return semantics, 100/150/200% reflow and prohibited-data protections. Every system-only State retains its authoritative owner, return/recheck, terminal, native accessibility and privacy boundary.\n\n"
"**Non-claims**: real Compose semantics, 200% runtime reflow, physical TalkBack, manual QA, telemetry capture audit and independent human approval are **NOT RUN / PENDING**. PH10-MIN-001 remains open for implementation QA.\n\n"
"[Full matrix](ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md) · [machine register](PHASE_7_REGISTER.json) · [65 device QA scenarios](PHASE_7_DEVICE_QA_MATRIX.md).\n")
print("PHASE7_PASS",json.dumps(checks,sort_keys=True))
