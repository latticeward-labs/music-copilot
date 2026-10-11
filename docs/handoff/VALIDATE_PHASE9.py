#!/usr/bin/env python3
"""Independent Issue 17 Phase 9 audit; never claims Android runtime success."""
from pathlib import Path
import json,hashlib,re,collections
R=Path(".");O=R/"docs/handoff";Q=chr(96)
J=lambda p:json.loads((R/p).read_text())
s=J("docs/high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json")
w=J("docs/wireframes/app-states/FRAME_REGISTER.json")
t=J("docs/design-system/foundations/TOKENS.json")
c=J("docs/design-system/components/COMPONENT_TOKENS.json")
p4=J("docs/high-fidelity/PHASE_4_PRIMARY_REGISTER.json")
p6=J("docs/high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json")
p7=J("docs/high-fidelity/phase-7/PHASE_7_REGISTER.json")
a=J("docs/handoff/ASSET_REGISTER.json")
required=["SCREEN_SPECS.md","COMPONENT_SPECS.md","STATE_ACTION_MATRIX.md",
          "COMPOSE_TOKEN_MAPPING.md","ASSET_REGISTER.json"]
assert all((O/x).is_file() for x in required)
sc,cm,m,tk=[(O/x).read_text() for x in required[:4]]
sha="0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
assert a["sourceArchiveSha256"]==t["source"]["archiveSha256"]==sha
assert len(s["states"])==126 and len(w["frames"])==32 and len(c["components"])==23
assert len(p4["artifacts"])==10 and len(p6["scenarios"])==27
assert len(p7["appVisible"])==45 and len(p7["systemOnly"])==20
assert collections.Counter(x["type"] for x in s["states"])=={
 "DEDICATED_HIFI":45,"HIFI_VARIANT_ANNOTATION":61,"SYSTEM_ONLY_HANDOFF":20}
for i in range(1,11):
 assert len(re.findall(r"^### WF-%02d — "%i,sc,re.M))==1,i
for id,v in c["components"].items():
 assert len(re.findall(r"^### "+id+r" — ",cm,re.M))==1,id
 for k,n in v["geometry"].items():
    value=str(n).lower() if isinstance(n,bool) else str(n)
  assert (Q+k+" = "+value+Q) in cm,(id,k)
  assert (Q+k+Q+" | "+Q+value+Q) in tk,(id,k)
for k,v in t["color"]["semantic"].items():
 assert Q+"color.semantic."+k+Q+" | "+Q+v+Q in tk,k
for k,v in t["typography"].items():
 if isinstance(v,dict) and "fontSizeSp" in v:
  assert Q+"typography."+k+Q in tk,k
for z in s["states"]:
 assert len(re.findall(r"^\| "+Q+re.escape(z["id"])+Q+r" \|",m,re.M))==1,z["id"]
for z in w["frames"]:
 assert len(re.findall(r"^\| "+Q+re.escape(z["id"])+Q+r" \|",m,re.M))==1,z["id"]
 assert len(re.findall(r"^\| "+Q+re.escape(z["id"])+Q+r" \|",sc,re.M))==1,z["id"]
assert len(a["canonicalScreenFamilies"])==10
assert len(a["canonicalDedicatedStates"])==45
assert len(a["variantAnnotationStates"])==61
assert len(a["nativeSystemOnly"])==20
assert len(a["specialistOverrides"])==27
assets=a["assets"];paths=[x["path"] for x in assets]
assert len(paths)==len(set(paths))==a["filesCount"]
sha256checked=0;pngcount=0;svgcount=0
for x in assets:
 p=R/x["path"];assert p.is_file(),p
 b=p.read_bytes()
 assert hashlib.sha1(b"blob "+str(len(b)).encode()+b"\0"+b).hexdigest()==x["gitBlobSha1"],p
 assert len(b)==x["sizeBytes"],p
 if x["format"]=="PNG":
  assert b[:8]==b"\x89PNG\r\n\x1a\n",p
  assert int.from_bytes(b[16:20],"big")>0 and int.from_bytes(b[20:24],"big")>0
  pngcount+=1
 else:
  assert x["format"]=="SVG" and b"<svg" in b[:4000],p;svgcount+=1
 if x.get("sourceSha256"):
  assert hashlib.sha256(b).hexdigest()==x["sourceSha256"],p;sha256checked+=1
for x in a["nativeSystemOnly"]:
 assert x["image"] is None and x["appScreen"] is None,x["id"]
 src=next(z for z in s["states"] if z["id"]==x["id"])
 assert src["type"]=="SYSTEM_ONLY_HANDOFF"
 assert (src["owner"],src["screenOwner"],src["recoveryOwner"])==(
 x["actualOwner"],x["actualScreenOwner"],x["actualRecoveryOwner"])
for x in a["canonicalDedicatedStates"]:
 assert any(y["path"]==x["asset"] and x["id"] in y["canonicalStateIds"] for y in assets)
for k in ["MediaSessionService","PlaybackGateway","VoiceSessionCoordinator","TelemetryPrivacyGate"]:
 assert all(k in txt for txt in (sc,cm,m)),k
for finding in ("PH10-MAJ-001","PH10-MAJ-002"):
 assert finding in (R/"docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md").read_text()
assert "PH10-MIN-001" in sc and "PH10-MIN-001" in tk
assert "Confirming.options" in cm
out={"date":"2026-10-11","issue":17,"phase":9,
 "status":"PASS_SOURCE_LOCKED_COMPOSE_HANDOFF_DOCUMENTATION",
 "sourceArchiveSha256":sha,"requiredFiles":5,"screenFamilies":10,
 "components":23,"canonicalStates":126,"approvedAppFrames":32,
 "nativeSystemOnly":20,"sourceAssets":len(assets),
 "pngAssets":pngcount,"svgAssets":svgcount,
 "gitBlobSHA1Verified":len(assets),"acceptedSHA256Verified":sha256checked,
 "missing":[],"duplicates":[],"ownerMismatches":[],
 "runtimeComposeQA":"NOT_RUN","realTalkBack200Percent":"NOT_RUN",
 "realMedia3STT":"NOT_RUN","humanApproval":"PENDING"}
(O/"PHASE9_VALIDATION.json").write_text(json.dumps(out,indent=2)+"\n")
(O/"PHASE9_VALIDATION.md").write_text(
 "# Phase 9 independent audit\n\n"
 "**PASS** for source-locked Jetpack Compose engineering specification: "
 "10 Screen Families, 23 CMP Components, 126 State rows, 32 approved frames, "
 "20 native OS-only owners and 5 required files.\n\n"
 "All actual asset bytes are checked against their Git blob SHA-1 and size; "
 "canonical prior SHA-256 verified wherever supplied.\n\n"
 "**Non-claims:** Kotlin/Compose runtime not implemented; device, Media3, STT, "
 "TalkBack, 200-percent text scaling and independent human signoff NOT RUN. "
 "PH10-MIN-001 still open.\n")
print("PHASE9_PASS",json.dumps(out,sort_keys=True))
