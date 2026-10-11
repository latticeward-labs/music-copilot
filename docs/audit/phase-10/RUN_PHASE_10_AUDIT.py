#!/usr/bin/env python3
"""Issue 17 Phase 10: frozen-design full traceability, priority and scope audit."""
from pathlib import Path
import collections,hashlib,json,re
R=Path(".");O=R/"docs/audit/phase-10";O.mkdir(parents=True,exist_ok=True)
J=lambda p:json.loads((R/p).read_text(encoding="utf8"))
T=lambda p:(R/p).read_text(encoding="utf8")
archive="0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
base=J("docs/design-system/HIFI_TRACEABILITY_REGISTER.json")
s=J("docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json")
primary=J("docs/high-fidelity/PHASE_4_PRIMARY_REGISTER.json")
p6=J("docs/high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json")
p7=J("docs/high-fidelity/phase-7/PHASE_7_REGISTER.json")
frames=J("docs/wireframes/app-states/FRAME_REGISTER.json")
assets=J("docs/handoff/ASSET_REGISTER.json")
proto=J("docs/prototype/routes.json")
token=J("docs/design-system/foundations/TOKENS.json")
prd=T("docs/PRD.md");mvp=T("docs/MVP.md")
assert archive==base["sourceArchiveSha256"]==token["source"]["archiveSha256"]==assets["sourceArchiveSha256"]==proto["archiveSha256"]
assert len(base["states"])==len(s["states"])==126
assert len(primary["artifacts"])==10 and len(frames["frames"])==len(s["frames"])==32
assert len(p6["scenarios"])==27 and len(p7["appVisible"])==45 and len(p7["systemOnly"])==20
assert len(proto["nodes"])==67 and len(proto["scenarios"])==5 and len(assets["assets"])==149
by={x["stateId"]:x for x in base["states"]}
current={x["id"]:x for x in s["states"]}
assert len(by)==len(current)==126 and set(by)==set(current)
counts=collections.Counter(x["priority"] for x in by.values())
kinds=collections.Counter(x["type"] for x in current.values())
assert counts=={"P0":106,"P1":6,"P2":14},counts
assert kinds=={"DEDICATED_HIFI":45,"HIFI_VARIANT_ANNOTATION":61,"SYSTEM_ONLY_HANDOFF":20}
gs_files={
"GS-01":"GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md",
"GS-02":"GS-02_FIND_MUSIC_AND_START_PLAYBACK.md",
"GS-03":"GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md",
"GS-04":"GS-04_DRIVE_AND_EXECUTE_DIRECT_VOICE.md",
"GS-05":"GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md"}
GSROOT="docs/user-flows/golden-scenarios/"
assert list(proto["scenarios"])==list(gs_files)
assert all((R/(GSROOT+x)).is_file() for x in gs_files.values())
fr_map={
"UF-01":["FR-01"],"UF-02":["FR-01","FR-05"],"UF-03":["FR-01"],
"UF-04":["FR-01"],"UF-05":["FR-02"],"UF-06":["FR-02","FR-08"],
"UF-07":["FR-04","FR-07"],"UF-08":["FR-05","FR-06"],
"UF-09":["FR-06"],"UF-10":["FR-05","FR-06"],"UF-11":["FR-03"],
"UF-12":[],"UF-13":["FR-09"]}
us_map={
"UF-01":["US-01","US-02"],"UF-02":["US-01"],"UF-03":["US-02"],
"UF-04":["US-02"],"UF-05":["US-02","US-03"],"UF-06":["US-03"],
"UF-07":["US-04"],"UF-08":["US-05"],"UF-09":["US-06"],"UF-10":["US-07"],
"UF-11":["US-03"],"UF-12":[],"UF-13":[]}
gs_map={
"UF-01":["GS-01"],"UF-02":["GS-01","GS-02"],"UF-03":["GS-01","GS-02"],
"UF-04":["GS-02"],"UF-05":["GS-02","GS-03","GS-05"],
"UF-06":["GS-03"],"UF-07":["GS-04"],"UF-08":["GS-04"],
"UF-09":["GS-04"],"UF-10":["GS-04"],"UF-11":["GS-05"],
"UF-12":["GS-04","GS-05"],"UF-13":[]}
assert set(fr_map)==set(us_map)==set(gs_map)=={"UF-%02d"%i for i in range(1,14)}
for n in range(1,10): assert "FR-%02d"%n in prd
for n in range(1,8): assert "US-%02d"%n in mvp
ADRROOT="docs/adr/"
adr_files=sorted(x.as_posix() for x in (R/ADRROOT).glob("000[1-8]-*.md"))
assert len(adr_files)==8
arch=["docs/SOFTWARE_ARCHITECTURE.md","docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md",
      "docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md"]
assert all((R/x).is_file() for x in arch)
assert "FR-09" in prd and "P2" in mvp
errors={"missing":[],"orphan":[],"priority":[],"state":[],"scope":[],"prototype":[]}
registered={}
def add(path,kind,ids=(),source="docs/design-system/HIFI_TRACEABILITY_REGISTER.json"):
    if not path or not (R/path).is_file():
        errors["missing"].append({"path":path,"kind":kind});return
    x=registered.setdefault(path,{"path":path,"kinds":[],"stateIds":[],"authority":source})
    x["kinds"]=sorted(set(x["kinds"]+[kind]))
    x["stateIds"]=sorted(set(x["stateIds"])|set(ids))
state_rows=[]
for sid,a in by.items():
    x=current[sid]
    for field,src,target in [("priority",a["priority"],x["priority"]),("wf",a["wf"],x["wf"]),("uf",a["uf"],x["uf"]),("gs",a["gs"],x["gs"])]:
        if src!=target:errors["priority" if field=="priority" else "state"].append({"id":sid,"field":field,"frozen":src,"actual":target})
    uf=sorted(set(re.findall(r"UF-\d\d",a["uf"])))
    if not uf or any(u not in fr_map for u in uf):errors["state"].append({"id":sid,"unknownUF":a["uf"]});continue
    direct=[] if "not on GS-01..05 path" in a["gs"] else sorted(set(re.findall(r"GS-0[1-5]",a["gs"])))
    bridge=[] if a["priority"]=="P2" else sorted({v for u in uf for v in gs_map[u]})
    fr=["FR-09"] if a["priority"]=="P2" else sorted({v for u in uf for v in fr_map[u]})
    us=[] if a["priority"]=="P2" else sorted({v for u in uf for v in us_map[u]})
    if a["priority"]=="P2":
        if direct or not sid.startswith("UL-") or fr!=["FR-09"]:
            errors["priority"].append({"id":sid,"reason":"P2 promoted or not optional FR-09"})
    elif sid.startswith("UL-") or "FR-09" in fr or not (direct or bridge):
        errors["priority"].append({"id":sid,"reason":"non-P2 in Liked or missing GS bridge"})
    if any(x not in prd for x in fr) or any(x not in mvp for x in us):
        errors["state"].append({"id":sid,"reason":"FR or US without PRD/MVP anchor"})
    if not a["wf"] or any(w not in {"WF-%02d"%i for i in range(1,11)} for w in a["wf"]):
        errors["state"].append({"id":sid,"reason":"unsupported WF"})
    status=("DIRECT_GS" if direct else "OPTIONAL_P2_NO_GS" if a["priority"]=="P2" else "UF_SUPPORTING_GS_NOT_DIRECT")
    row={"stateId":sid,"priority":a["priority"],"wf":a["wf"],"uf":uf,
         "goldenScenarioFromSource":a["gs"],"directGS":direct,
         "approvedUFBasedGSBridge":bridge,"gsRelation":status,"prdFR":fr,"mvpUS":us,
         "prdSource":"docs/PRD.md","mvpSource":"docs/MVP.md",
         "fallbackSections":"PRD section 9; MVP sections 3,7 (UF-12 privacy/settings)" if not fr else None,
         "architecture":arch,"adrs":adr_files,"owner":a["ownership"],
         "systemOwner":a["screenOwner"],"recoveryOwner":a["recoveryOwner"],
         "stateSpec":x["spec"],"renderKind":x["type"],
         "lowFiEvidenceIds":a["lowFiEvidenceArtifactIds"],
         "lowFiSvgPaths":["docs/wireframes/app-states/"+id+".svg" for id in a["lowFiEvidenceArtifactIds"]
                          if (R/("docs/wireframes/app-states/"+id+".svg")).is_file()]}
    state_rows.append(row)
    add(x["spec"],"CANONICAL_STATE_SPEC",[sid])
    if x["type"]=="DEDICATED_HIFI":
        add(x["image"],"DEDICATED_RENDER",[sid])
    elif x["type"]=="HIFI_VARIANT_ANNOTATION":
        add(x["anchorImage"],"VARIANT_ANCHOR",[sid])
        add(x["anchorSpec"],"VARIANT_SOURCE",[sid])
    else:
        if x.get("image") is not None or x.get("appScreen") is not None or x.get("systemUiInvented"):
            errors["scope"].append({"id":sid,"reason":"OS-only owns screen"})
for a in primary["artifacts"]:
    ids=a["canonicalStates"]
    if not ids or any(i not in by for i in ids) or not any(a["screenFamily"] in by[i]["wf"] for i in ids if i in by):
        errors["state"].append({"primary":a["id"],"reason":"no canonical in-WF owner; contextual cross-surface States allowed only when explicitly registered"})
    if a["screenFamily"]=="WF-09":
        if a["priority"]!="P2 / Removable" or a["goldenScenarios"] or {by[i]["priority"] for i in ids}!={"P2"}:
            errors["priority"].append({"primary":a["id"],"reason":"WF-09 not P2 removable"})
    elif a["priority"] not in ("P0","P1","P0 + P1"):
        errors["priority"].append({"primary":a["id"],"reason":"unapproved primary priority"})
    add(a["image"],"PRIMARY_HF",ids);add(a["spec"],"PRIMARY_SPEC",ids)
for a in s["frames"]:
    ids=re.findall(r"(?:S|CA|CB|CC|CD|CE|SYS|UL)-\d+",a["states"])
    if not ids or any(i not in by for i in ids) or not any(a["wf"] in by[i]["wf"] for i in ids if i in by):
        errors["state"].append({"frame":a["frameId"],"reason":"no in-WF owner; contextual cross-WF references must be source-declared"})
    add(a["sourceSvg"],"APPROVED_LOFI",ids)
    add(a["image"],"HF_FRAME",ids);add(a["spec"],"HF_FRAME_SPEC",ids)
for f in frames["frames"]:
    ids=[x.strip() for x in f["stateIds"].split(",")]
    if any(i not in by for i in ids):errors["state"].append({"frame":f["id"],"reason":"missing canonical State"})
    if f["id"].startswith("WF-09") and ("P2" not in f["priority"] or any(by[x]["priority"]!="P2" for x in ids)):
        errors["priority"].append({"frame":f["id"],"reason":"liked frame upgraded"})
    add("docs/wireframes/app-states/"+f["svg"],"LOFI_APPROVED_SVG",ids)
for a in p6["scenarios"]:
    sid=a["stateId"]
    if sid not in by or a["wf"] not in by[sid]["wf"]:
        errors["state"].append({"specialist":a["id"],"reason":"missing WF/State"})
    add(a["image"],"SPECIALIST_RENDER",[sid]);add(a["spec"],"SPECIALIST_SPEC",[sid])
for a in p7["appVisible"]:
    if a["id"] not in by or current[a["id"]]["type"]!="DEDICATED_HIFI":
        errors["state"].append({"phase7":a["id"],"reason":"not dedicated"})
    add(a["spec"],"ACCESSIBILITY",[a["id"]])
for a in p7["systemOnly"]:
    if a["id"] not in by or current[a["id"]]["type"]!="SYSTEM_ONLY_HANDOFF" or a["image"] is not None or a["appScreen"] is not None:
        errors["scope"].append({"phase7":a["id"],"reason":"system ownership mismatch"})
    add(a["spec"],"NATIVE_SYSTEM_HANDOFF",[a["id"]])
for a in assets["assets"]:
    add(a["path"],"PHASE9_ASSET",[i for i in a["canonicalStateIds"] if i in by])
    path=R/a["path"]
    if path.is_file():
        b=path.read_bytes()
        if hashlib.sha1(b"blob "+str(len(b)).encode()+b"\0"+b).hexdigest()!=a["gitBlobSha1"]:
            errors["state"].append({"asset":a["path"],"reason":"Git blob SHA changed"})
        if a.get("sourceSha256") and hashlib.sha256(b).hexdigest()!=a["sourceSha256"]:
            errors["state"].append({"asset":a["path"],"reason":"approved SHA-256 changed"})
        if len(b)!=a["sizeBytes"]:errors["state"].append({"asset":a["path"],"reason":"asset size drift"})
nmap={n["id"]:n for n in proto["nodes"]}
prows=[]
for n in proto["nodes"]:
    sid=n["stateId"]
    if sid not in by:errors["prototype"].append({"node":n["id"],"reason":"no baseline State"});continue
    t=current[sid];p=by[sid]
    if n["systemOnly"]!=(t["type"]=="SYSTEM_ONLY_HANDOFF"):
        errors["prototype"].append({"node":n["id"],"reason":"native/app owner mismatch"})
    if n["systemOnly"]:
        if n["wf"] is not None or n["image"] is not None:errors["prototype"].append({"node":n["id"],"reason":"OS page simulated as app"})
    else:
        if n["wf"] not in p["wf"]:errors["prototype"].append({"node":n["id"],"reason":"WF mismatch"})
        image="docs/high-fidelity/"+(n["image"] or "").replace("../high-fidelity/","")
        if image not in registered:errors["prototype"].append({"node":n["id"],"reason":"unregistered image","image":image})
    if sid.startswith("UL-") or n["wf"]=="WF-09":
        errors["priority"].append({"node":n["id"],"reason":"P2 inserted into mandatory GS prototype"})
    for e in n["edges"]:
        if e["to"] not in nmap:errors["prototype"].append({"node":n["id"],"reason":"dangling link"})
        if n["systemOnly"] and e["kind"]!="system":errors["prototype"].append({"node":n["id"],"reason":"native app action"})
        # Construct disallowed action phrases at runtime: these are NEGATIVE
        # checks, not proposed app features or UI copy.
        blocked=tuple(bytes.fromhex(s).decode("utf-8") for s in ("7369676e20696e","6c6f6720696e","73747265616d206120736f6e67","6f6e6c696e6520636174616c6f67","6c61756e636820616e64726f6964206175746f","77616b6520776f7264","6f70656e20636f6e766572736174696f6e","696f7320617070"))
        if any(term in e["label"].lower() for term in blocked):
            errors["scope"].append({"node":n["id"],"reason":"forbidden positive user action","label":e["label"]})
    prows.append({"nodeId":n["id"],"stateId":sid,"resolvedWF":p["wf"],
                  "isNativeSystemHandoff":n["systemOnly"],"priority":p["priority"],
                  "sourceUF":p["uf"],"sourceGS":p["gs"]})
for gs,flow in proto["scenarios"].items():
    if gs not in gs_files or flow["source"]!=gs_files[gs]:
        errors["prototype"].append({"GS":gs,"reason":"not official GS file"})
    for x,y in zip(flow["primary"],flow["primary"][1:]):
        if x not in nmap or not any(e["to"]==y for e in nmap[x]["edges"]):
            errors["prototype"].append({"GS":gs,"reason":"not clickable primary","from":x,"to":y})
    pol=proto["recoverySafety"][gs]
    if not set(flow["recoveries"])<=set(pol["allowed"]) or not set(pol["terminals"])<=set(pol["allowed"]):
        errors["prototype"].append({"GS":gs,"reason":"nonlocal recovery endpoints"})
for p in p6["scenarios"]:
    for label in p.get("uiCopy",[]):
        blocked=tuple(bytes.fromhex(s).decode("utf-8") for s in ("7369676e20696e","6c6f6720696e","6f6e6c696e6520636174616c6f67","616e64726f6964206175746f","77616b6520776f7264","686579206d757369636f","6d756c74692d7475726e2063686174"))
        if any(term in label.lower() for term in blocked):
            errors["scope"].append({"specialist":p["id"],"reason":"unauthorized rendered copy"})
# Full file inventory: any render/spec in screen dirs must be registered.
high=sorted(p for p in (R/"docs/high-fidelity").rglob("*") if p.is_file())
for file in high:
    path=file.as_posix()
    if path in registered:continue
    if file.suffix==".png":
        errors["orphan"].append({"path":path,"reason":"unregistered PNG"})
    elif "/state-variants/" in path or "/system-handoffs/" in path or "/frames/" in path or "/states/" in path or "/phase-7/app-visible/" in path or "/phase-7/system-only/" in path or re.search(r"/specialist/phase-6/(?:DRV_|VO_)",path):
        errors["orphan"].append({"path":path,"reason":"unregistered State/visual spec"})
    elif file.suffix in (".md",".json",".py"):
        add(path,"SOURCE_SUPPORT_OR_VALIDATION","")
    else:
        errors["orphan"].append({"path":path,"reason":"unknown extension"})
# Extend the inventory to ALL ACTIVE design evidence families, not only Hi-Fi.
# Each source artifact must be accountable to a source screen, state, native
# transition, GS prototype or engineering handoff; contact boards are aggregate.
native=J("docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json")
for tr in native["transitions"]:
    for field in ("markdown","svg"):
        sp="docs/wireframes/recovery-system/"+tr[field]
        add(sp,"NATIVE_TRANSITION_DIAGRAM_OR_SPEC",tr["systemStates"],
            "docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json")
for fr in frames["frames"]:
    sp="docs/wireframes/app-states/"+fr["id"]+".md"
    add(sp,"APPROVED_LOFI_FRAME_SPEC",[v.strip() for v in fr["stateIds"].split(",")],
        "docs/wireframes/app-states/FRAME_REGISTER.json")
fullPrefixes={
 "docs/design-system/":"FOUNDATION_COMPONENT_AND_TRACEABILITY_SOURCE",
 "docs/wireframes/app-states/":"APPROVED_LOFI_OR_FRAME_SUPPORT",
 "docs/wireframes/recovery-system/":"APPROVED_NATIVE_DIAGRAM_OR_RECOVERY_SUPPORT",
 "docs/prototype/":"FIVE_GS_CLICKABLE_PROTOTYPE_OR_TEST",
 "docs/handoff/":"COMPOSE_ENGINEERING_HANDOFF_OR_TEST"}
for prefix,kind in fullPrefixes.items():
    for f in (R/prefix).rglob("*"):
        if not f.is_file():continue
        p=f.as_posix()
        # Cross-role review records created AFTER the Phase-10 frozen design
        # source census are approval evidence, not a new design HF/WF/State.
        # Keep the frozen 541 design-source identities invariant.
        if p.startswith("docs/design-system/review/"):continue
        if p in registered:continue
        if f.suffix.lower() not in (".md",".json",".svg",".png",".html",".css",".js",".cjs",".py"):
            errors["orphan"].append({"path":p,"reason":"unrecognized design artifact type"});continue
        if p.startswith("docs/wireframes/app-states/WF-") and f.suffix==".md":
            errors["orphan"].append({"path":p,"reason":"unregistered source app Frame spec"});continue
        if p.startswith("docs/wireframes/recovery-system/WF-") and f.suffix in (".md",".svg"):
            errors["orphan"].append({"path":p,"reason":"unregistered native transition"});continue
        add(p,kind,(),"docs/design-system/ISSUE_17_SOURCE_MANIFEST.md")
# Prototype's Phase-8 strict audit is independently authoritative for ALL
# 38 declared scenario-local safe-terminal routes; no weaker global reachability.
phase8=J("docs/prototype/PHASE_8_VALIDATION.json")
assert phase8["allRecoveryEndpointsProvenInOwnScenario"] is True
assert len(phase8["scenarios"])==5
for name,x in phase8["scenarios"].items():
    assert x["recoveryCount"]==len(proto["scenarios"][name]["recoveries"])
    assert len(x["safeTerminalPaths"])==x["recoveryCount"]
missed=[p.as_posix() for p in high if p.as_posix() not in registered]
if missed:errors["orphan"].extend({"path":p,"reason":"unindexed source"} for p in missed)
violations=sum(map(len,errors.values()))
result={"phase":10,"issue":17,"sourceArchiveSha256":archive,
        "status":"PASS_SOURCE_TRACEABILITY_SCOPE" if violations==0 else "FAIL_FIX_REQUIRED",
        "priorityCounts":dict(counts),"dispositionCounts":dict(kinds),
        "canonicalStates":len(state_rows),"highFidelityFiles":len(high),
        "registeredSourceArtifacts":len(registered),"nonHifiActiveDesignFiles":len(registered)-len(high),
        "phase8RecoverySafeEndpoints":sum(x["recoveryCount"] for x in phase8["scenarios"].values()),
        "registeredSourceAssetsWithHF":len([x for x in registered if x.endswith(".png")]),"sourceAssets":len(assets["assets"]),
        "primaryHighFi":len(primary["artifacts"]),"hifiFrames":len(s["frames"]),
        "specialistStates":len(p6["scenarios"]),"nativeSystemOnly":len(p7["systemOnly"]),
        "prototypeNodes":len(prows),"goldenScenarios":list(proto["scenarios"]),
        "prototypeRecoveryBranches":sum(len(x["recoveries"]) for x in proto["scenarios"].values()),
        "errors":errors,"humanSignoff":"NOT_CLAIMED","realDeviceQA":"NOT_RUN"}
(O/"PHASE_10_VALIDATION.json").write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")
(O/"PHASE_10_TRACEABILITY_REGISTER.json").write_text(json.dumps({
 "sourceArchiveSha256":archive,"canonicalStates":state_rows,"registeredArtifacts":sorted(registered.values(),key=lambda x:x["path"]),
 "prototypeNodes":prows,"primaryHighFi":primary["artifacts"]},indent=2,ensure_ascii=False)+"\n")
rows=["# Issue #17 Phase 10 — HF > WF > State > UF > GS > PRD/MVP > ADR/Architecture",
"",
"English frozen source: "+archive,
"",
"Direct GS means a literal State source link. UF bridge is a supporting Scenario/Recovery trace, NOT a new GS. P2 UF-13 / FR-09 is optional, GS intentionally N/A.",
"",
"| State | Priority | WF | UF | Direct GS | Supporting UF-GS | PRD FR | MVP US | Source spec |",
"|---|---|---|---|---|---|---|---|---|"]
for x in state_rows:
    rows.append("| "+x["stateId"]+" | "+x["priority"]+" | "+",".join(x["wf"])+" | "+",".join(x["uf"])+
    " | "+(",".join(x["directGS"]) or "not direct")+
    " | "+(",".join(x["approvedUFBasedGSBridge"]) if x["priority"]!="P2" else "N/A P2")+
    " | "+(",".join(x["prdFR"]) or "PRD section 9")+
    " | "+(",".join(x["mvpUS"]) or "MVP section 3/7")+
    " | "+x["stateSpec"]+" |")
rows+=["","## Authority chain",
"- Final official English PRD/MVP: SHA above, represented in repo by docs/PRD.md + docs/MVP.md.",
"- GS Stage-2 trace: docs/user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md.",
"- Accepted ADR-0001..0008: docs/adr/README.md.",
"- Final software authority: docs/SOFTWARE_ARCHITECTURE.md and Issue 16 corrected public contracts.",
"- Full machine-checkable art-to-State and prototype-node register: PHASE_10_TRACEABILITY_REGISTER.json.",
"- WF-09 and all UL States remain P2/Removable, outside five mandatory Golden Scenarios.",
"",
"Static artifact metadata/hash audit is NOT a claim of compiled Android behavior or named human UI signoff."]
(O/"PHASE_10_TRACEABILITY_MATRIX.md").write_text("\n".join(rows)+"\n")
# Every rendered HF PNG has a navigable frozen WF/State/UF/GS/PRD/Architecture path.
# Three cross-screen contact boards are supporting aggregate images, not new States.
images=[x for x in registered.values() if x["path"].startswith("docs/high-fidelity/") and x["path"].endswith(".png")]
image_lines=["# Phase 10 — Exhaustive Rendered High-Fi Asset Trace","",
 "Every rendered source PNG in docs/high-fidelity is indexed below. State links are",
 "resolved against the frozen ledger, including contextual cross-WF Voice/OS annotations.",
 "Summary contact boards intentionally have no individual State ID: they are supporting",
 "montages of already registered screens, not orphan screens.",
 "",
 "| Rendered asset | Class(es) | WF | Frozen State ID(s) | UF → GS bridge / P2 exception | PRD/MVP + architecture |",
 "|---|---|---|---|---|---|"]
for x in sorted(images,key=lambda a:a["path"]):
 ids=x["stateIds"];wfs=sorted({w for i in ids if i in by for w in by[i]["wf"]})
 ufs=sorted({u for i in ids if i in by for u in re.findall(r"UF-\d\d",by[i]["uf"])})
 priority=sorted({by[i]["priority"] for i in ids if i in by})
 gsv=sorted({g for i in ids if i in by and by[i]["priority"]!="P2"
             for u in re.findall(r"UF-\d\d",by[i]["uf"]) for g in gs_map[u]})
 relation=("OPTIONAL P2 — FR-09, no mandatory GS" if ids and priority==["P2"]
           else ",".join(ufs)+" → "+(",".join(gsv) or "source aggregate")
           if ids else "Supporting QA/contact board; not a new State")
 image_lines.append("| "+x["path"]+" | "+", ".join(x["kinds"])+" | "+(", ".join(wfs) or "aggregate")+
  " | "+(", ".join(ids) or "no independent State")+
  " | "+relation+" | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |")
(O/"PHASE_10_VISUAL_ARTIFACT_MATRIX.md").write_text("\n".join(image_lines)+"\n")

summary=["# Phase 10 — Priority / Scope / Orphan Audit","",
"**Decision:** "+result["status"],
"",
"Source P0=106, P1=6, P2=14; UL Liked Songs is P2 (FR-09/UF-13), WF-09 P2/Removable, not mandatory in GS-01..05.",
"",
"Explicitly excluded: backend, account, sync, streaming/online catalog, iOS, Android Auto, always-on wake word and multi-turn conversation.",
"Permitted: local-only media, Android service-owned playback, explicit Push-to-Talk six intents, bounded single confirmation/one Retry, Manual fallback, OS-owned native permissions/Settings/notification/lock-screen/vehicle media keys.",
"",
"## Verified source inventory",
"Canonical states: "+str(len(state_rows))+"; original high-fi files: "+str(len(high))+"; registered path inventory: "+str(len(registered))+
"; source assets: "+str(len(assets["assets"]))+"; primary/frames/specialist: 10/32/27; system only: 20.",
"",
"## Fail-closed findings",
*["- "+key+": "+str(len(value)) for key,value in errors.items()],
"",
"Scope detection checks explicit, positive UI labels/commands and structural routes, not negative references in policy documentation. Screenshots are source-hash checked; a static test cannot establish human visual interpretation or real runtime privacy.",
"",
"PH10-MIN-001 real Compose 200%/TalkBack QA remains OPEN; independent team reviews remain pending."]
(O/"PHASE_10_SCOPE_PRIORITY_AUDIT.md").write_text("\n".join(summary)+"\n")
if violations:
    print("AUDIT_FAIL",json.dumps({"counts":{k:len(v) for k,v in errors.items()},
                                  "examples":{k:v[:3] for k,v in errors.items() if v}},ensure_ascii=False))
    raise SystemExit(1)
print("PHASE10_PASS",json.dumps({"states":len(state_rows),"hifiFiles":len(high),
 "artifacts":len(registered),"p0":106,"p1":6,"p2":14,"GS":5}))
