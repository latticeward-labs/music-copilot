#!/usr/bin/env python3
"""Phase 7: deterministic 45 A11y + 20 OS ownership implementation handoffs."""
from pathlib import Path
import collections, hashlib, json, os, re
ROOT=Path("docs/high-fidelity")
OUT=ROOT/"phase-7"
APP=OUT/"app-visible"
SYS=OUT/"system-only"
for x in (OUT,APP,SYS):x.mkdir(parents=True,exist_ok=True)
reg=json.loads((ROOT/"HIFI_STATE_COVERAGE_REGISTER.json").read_text())
can=json.loads(Path("docs/design-system/HIFI_TRACEABILITY_REGISTER.json").read_text())
plat=json.loads(Path("docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json").read_text())
tok=json.loads(Path("docs/design-system/foundations/TOKENS.json").read_text())
special=json.loads((ROOT/"specialist/phase-6/PHASE_6_REGISTER.json").read_text())
arch=Path("docs/architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md").read_text()
assert can["sourceArchiveSha256"]=="0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
assert len(reg["states"])==126 and "PH10-MIN-001 remains open downstream" in arch
assert tok["accessibility"]["textScalePercent"]==200
assert collections.Counter(x["type"] for x in reg["states"])==dict(DEDICATED_HIFI=45,HIFI_VARIANT_ANNOTATION=61,SYSTEM_ONLY_HANDOFF=20)
assert len(plat["canonicalSystemOnly"])==20
def link(to,base):
    return os.path.relpath(str(to),str(base)).replace("\\","/")
def get(path):
    p=Path(path); assert p.is_file(),str(p);return p
# Ordered, existing product components; names are semantic role descriptions
# and not extra controls. A11y supports ephemeral UI labels from runtime owners.
specs={
"WF-01":(
"Startup heading -> stable loading/error status -> recovery explanation -> Retry when available",
[("Retry","Retry startup")],
"Loading progress or written Startup problem, not indicator color alone",
"Vertical growth of error/retry; avoid fixed-height dialogs or repetitive progress announcements"),
"WF-02":(
"Onboarding heading -> explanation -> page index -> Continue/Skip/Back",
[("Continue","Continue onboarding"),("Skip","Skip optional voice"),("Back","Previous onboarding step")],
"Spoken step x of y, not colored dots only",
"Illustration shrinks before any text; Continue/Skip stay reachable at 200%"),
"WF-03":(
"Permission heading -> purpose -> granted/denied status -> approved action -> return guidance",
[("Open Settings","Open Android Settings"),("Not now","Not now; continue using manual controls"),("Retry","Retry permission check")],
"Granted/Denied/Permanently denied label plus alert icon; color is supplemental",
"Explanations and controls stack; no OS dialog launch on entry; recheck grant on return"),
"WF-04":(
"Library heading -> scan/ready/error state -> Songs/Artists/Albums/Folders tabs -> content rows -> retry or navigation",
[("Songs","Browse songs"),("Artists","Browse artists"),("Albums","Browse albums"),("Folders","Browse folders"),("Retry","Retry scanning local music")],
"Scanning/Empty/Error text and shape, never spinner-only",
"Rows expand and wrap; category tabs scroll, no clipped error or blocked bottom navigation"),
"WF-05":(
"Search heading -> query input -> clear action -> result/error/no result heading -> rows or retry -> navigation",
[("Search","Search local music"),("Clear","Clear search query"),("Retry","Retry local search"),("Open","Open selected local result")],
"Explicit No local results/Search failed label plus shape, not empty or colored field",
"Text input and results wrap; preserve query and access to keyboard/clear/retry at 200%"),
"WF-06":(
"Now playing heading -> Queue -> artwork/metadata -> playback status -> progress -> Previous/Play-Pause/Next -> Driving entry",
[("Queue","Open current queue"),("Previous","Previous track"),("Play","Play playback"),("Pause","Pause playback"),("Next","Next track"),("Driving","Enter Driving Mode")],
"Playing/Paused/Completed/Corrupt written state and semantic action, never icon color only",
"Artwork yields to multi-line metadata; controls and Driving remain independent with no overlap"),
"WF-07":(
"Driving heading -> immediate Exit -> Playing/Paused/No Track -> verified metadata -> transport actions -> Mic",
[("Exit","Exit Driving Mode immediately"),("Previous","Previous track"),("Play","Play playback"),("Pause","Pause playback"),("Next","Next track"),("Seek Back","Seek backward ten seconds"),("Mic","Voice command; press to talk")],
"State words and disabled text for invalid transport; Play last only if verified local item",
"Fixed one-layer Exit and large transport priority; shrink artwork first; no new list/keyboard/deep flow"),
"WF-08":(
"Overlay heading -> Listening/Processing/Confirming/Executed/Failed -> one status sentence -> immutable 1-2 candidates -> Neither/Cancel OR eligible Retry/Manual",
[("Cancel","Cancel voice and return to manual playback"),("Neither","Neither candidate; return to manual playback"),("Retry once","Retry voice once"),("Manual controls","Return to manual playback"),("Dismiss","Dismiss voice feedback")],
"Written Voice state plus shape; never waveform/error color alone",
"Scroll overlay if 200% text; all candidate labels wrap; manual/cancel and origin Exit stay reachable"),
"WF-09":(
"Liked Songs P2 heading -> loading/empty/ready -> rows -> Play list/Unlike/Back -> navigation",
[("Play list","Play liked local songs"),("Unlike","Remove liked selection"),("Back to Library","Return to local library")],
"Selected and not-selected stateDescription plus heart shape and words",
"Rows wrap; no clipping; removing P2 never affects P0 controls"),
"WF-10":(
"Settings heading -> permission status -> optional Bluetooth -> Privacy/disclosure -> clear confirmation -> Cancel/Confirm -> navigation",
[("Open","Open settings section"),("Privacy","Read privacy details"),("Save","Save selected preference"),("Confirm clear","Confirm clearing selected app data"),("Cancel","Cancel without clearing"),("Not now","Decline optional cloud voice")],
"Consent/permission/selection/clear status written; color cannot imply grant or completion",
"Long retention/disclosure text wraps; destructive and cancel remain separately reachable at 200%")
}
assert len(specs)==10
overrides={
"S-01":"Retry must not erase verified local data; migration failure is stated explicitly.",
"S-03":"Permanent denial offers explicit Android Settings only outside Driving; recheck actual permission on return.",
"S-04":"Scanning start and stable completion/error announced once, not every percent.",
"CB-11":"No results preserve current search input; announce zero matching local items.",
"CB-12":"Search error preserves query; Retry visible and announced.",
"CB-17":"Completed queue announced as Completed, never Playing.",
"CB-18":"Queue order: current item then subsequent items; each row accessible as grouped metadata plus action.",
"CC-05":"Playing action is Pause; first actionable focus target is Exit.",
"CC-06":"Paused action is Play; never auto-start on entry.",
"CC-07":"No Track never has enabled Play last absent verified local reference; Exit and manual remain accessible.",
"CC-11":"P1 Bluetooth suggestion only outside Driving; no automatic entry.",
"CD-03":"Attempt 1 only starts on explicit mic tap and is six seconds max.",
"CD-04":"Attempt 2 consumes last retry budget; no auto third capture.",
"CD-05":"Processing must not expose full transcript or optimistic success.",
"CD-06":"Command pending waits for PlaybackSnapshot acknowledgment.",
"CD-07":"Executed only after service ack; TTS failure cannot revoke executed status.",
"CD-08":"Use Phase-6 specialist: rejected execution is Manual-only, NOT generic Retry.",
"CD-09":"One immutable candidate; Neither and Cancel are recovery actions.",
"CD-10":"Two immutable options max; one confirmation round; Neither/Cancel outside options.",
"CD-11":"Selected candidate index/token revalidated, never UI-resolved via search.",
"CD-12":"Failed confirmation terminates; no repeat confirmation or new retry.",
"CD-13":"Artist not found: no guessed nearest match, Manual-only.",
"CD-14":"Unknown phrase: no approximate execution, Manual-only.",
"CD-15":"Unsupported command: no approximation, Manual-only.",
"CD-16":"Missing artist slot: no conversation prompt chain, Manual-only.",
"CD-17":"Eligible attempt-one STT failure permits exactly one explicit Retry.",
"CD-18":"Retry once only, no automatic capture.",
"CD-19":"Second failure terminal Manual/Exit, never third Retry.",
"CD-23":"Cloud consent is external to active Driving; PH10-MIN-002 still blocks remote STT.",
"CE-21":"Privacy disclosure has readable purpose, retention and no default raw voice storage.",
"CE-22":"Consent processing must be gated by approved provider disclosure/legal review.",
"CE-23":"Clear data confirmation must not delete user music or stop playback without authorized behavior."
}
special_map={}
for case in special["scenarios"]:
    special_map.setdefault(case["stateId"],case["image"])
privacy=("EXCLUDE from repository evidence, event payload, logs, screenshots/crash reports: raw microphone audio, "
"full transcript, actual user file titles/artist/paths/URI, Voice candidate labels/tokens, exact speech scores, precise playback "
"position, exact location/GPS, and device/Bluetooth identifiers. Use generic synthetic fixtures and redact screenshots. "
"Telemetry is limited to approved coarse status/intent enum/latency bucket after the privacy gate.")
a=[]
for s in reg["states"]:
    if s["type"]!="DEDICATED_HIFI":continue
    id=s["id"];wf=s["wf"][0];order,actions,cues,reflow=specs[wf]
    if id in ("CD-08","CD-12","CD-13","CD-14","CD-15","CD-16","CD-19"):
        actions=[("Manual controls","Return to manual playback"),("Cancel","Cancel voice")]
    if id in ("CD-17","CD-18"):
        actions=[("Retry once","Retry speech once"),("Manual controls","Return to manual playback"),("Cancel","Cancel voice")]
    if id in ("CD-09","CD-10"):
        actions=[("Candidate choice","Artist option index and ephemeral label from Confirming.options"),("Neither","Neither candidate; use manual playback"),("Cancel","Cancel voice")]
    if id=="CC-07":
        actions=[("Exit","Exit Driving Mode immediately"),("Play last","Play last verified local item, otherwise absent"),("Mic","Voice command; press to talk")]
    # Specialist traversal must represent the rendered current state, not
    # an aggregate sequence containing unavailable candidates or Retry.
    if wf=="WF-08":
        if id in ("CD-09","CD-10"):
            count="one" if id=="CD-09" else "two"
            order=("Voice overlay: Which artist? -> Brief confirmation purpose -> "
                   +count+" immutable option(s) in presented order -> Neither -> Cancel -> Return to manual playback")
        elif id in ("CD-17","CD-18"):
            order="Voice failure heading -> Brief eligible attempt-one failure -> Retry once -> Manual controls -> Cancel/Driving Exit"
        elif id in ("CD-03","CD-04","CC-14"):
            order="Listening heading -> Six-second attempt status -> Cancel -> Manual controls/Driving Exit"
        elif id in ("CD-05","CD-06","CD-11"):
            order="Processing heading -> Pending status -> Cancel -> Manual controls/Driving Exit"
        elif id=="CD-07":
            order="Done heading -> Acknowledged playback status -> Dismiss -> Manual controls/Driving Exit"
        else:
            order="Typed voice failure heading -> One short reason -> Manual controls -> Cancel/Driving Exit"
    elif id=="CC-07":
        order="Driving Mode heading -> Immediate Exit -> No Track status -> Explanation -> Optional verified Play last -> Mic when supported"
    elif id=="CC-11":
        order="Non-driving source heading -> Dismissible optional Bluetooth suggestion -> Enter Driving when explicitly chosen -> Not now -> Existing manual playback"
    elif id=="CB-18":
        order="Current queue heading -> Current verified item -> Remaining queue in service order -> Back to current player"
    elif id=="CB-11":
        order="Search heading -> Preserved query -> No local results status -> Clear/Edit -> Bottom navigation"
    elif id=="CB-12":
        order="Search heading -> Preserved query -> Search failed status -> Retry -> Existing manual navigation"
    elif id=="S-01":
        order="Startup problem heading -> Preservation explanation -> Retry -> Safe retained data status"
    elif id=="S-03":
        order="Permission recovery heading -> Permanently denied explanation -> Open Android Settings -> Not now"
    elif id=="S-04":
        order="Library heading -> Scanning local music status -> Existing category tabs -> Manual navigation"
    elif id=="CE-23":
        order="Clear selected data heading -> Selection/consequence -> Preserved music files notice -> Cancel -> Confirm clear"

    baseline=get(s["spec"]);art=get(special_map.get(id,s["image"]))
    doc=APP/(id+".md")
    doc.write_text("\n".join([
"# Phase 7 accessibility disposition — "+id+" / "+s["state"],"",
"**Status:** SPECIFIED_FOR_IMPLEMENTATION; runtime QA NOT RUN; independent approval NOT CLAIMED.",
"**WF:** "+", ".join(s["wf"])+"; **UF:** "+s["uf"]+"; **GS:** "+s["gs"]+"; **priority:** "+s["priority"],
"**Playback/Voice/screen owner:** "+s["owner"]+"; **recovery:** "+s["recoveryOwner"],
"**Review image:** ["+art.name+"]("+link(art,APP)+"); **authoritative prior state spec:** ["+baseline.name+"]("+link(baseline,APP)+")",
"","## Explicit reading order for TalkBack","",
*("- "+str(i+1)+". "+item.strip() for i,item in enumerate(order.split(" -> "))),
"","## Spoken labels on EXISTING controls only","",
"| Existing control | TalkBack label / action |","|---|---|",
*("| "+k+" | "+v+" |" for k,v in actions),
"","## Compose semantics/focus contract","",
"- Mark top-level title as heading; group only related track metadata, never collapse action buttons into non-clickable parent semantics.",
"- Set dynamic stateDescription on actual authoritative status; disabled actions expose enabled=false with a label, not merely dim colors.",
"- Set role=Button on applicable controls and label 'verb + object'; restore focus to origin after overlay closes, preserve first accessible Exit in Driving.",
"- Provide one concise transition announcement; no per-frame spamming. Do not announce success before MediaSession/Voice owner acknowledgment.",
"- State-specific recovery: "+overrides.get(id,"Keep approved WF recovery and return focus; never create a new route."),
"","## Non-color-only cues","",
"- "+cues+". Icons/words and visible focus outline convey focus, pending, error and success without color alone.",
"","## 200% text scaling / reflow / targets","",
"- Runtime 100%, 150% and 200% system fontScale, narrow screen and large accessibility font: all headings, state and error/consent text fully readable.",
"- "+reflow+". Use sp typography, scrolling/weight-based Compose layout, no fixed-height clipping; 200% reading order stays logical.",
"- Every touch action >=48x48dp; Driving transport >=64dp, Mic >=72dp, Exit >=56dp, even when fontScale=200%.",
"- Safe touch placement considers system gesture/insets; visible keyboard/overlay cannot trap Cancel or Back.",
"","## Privacy, platform and evidence","",
"- "+privacy,
"- Android-native permissions/Settings/notifications/lock-screen/external car media controls are NOT app screens.",
"- MediaSessionService owns playback; VoiceSessionCoordinator owns ephemeral active voice and confirmation tokens; never log candidates.",
"","## QA status (real Compose execution needed)","",
"- [ ] TalkBack spoken labels/reading order/focus restore validated on device.",
"- [ ] 100/150/200% text reflow, scroll and truncation captured with sanitized test fixture.",
"- [ ] 48dp plus Driving specialized targets asserted in Compose UI tests.",
"- [ ] Focus ring/stateDescription/non-color-only cues and user permissions/voice fallback tested.",
"" ]),encoding="utf-8")
    a.append(dict(id=id,type="APP_VISIBLE",wf=s["wf"],uf=s["uf"],owner=s["owner"],sourceSpec=str(baseline),sourceArt=str(art),spec=str(doc),readingOrder=order.split(" -> "),actions=[dict(action=k,talkBack=v) for k,v in actions],nonColorOnly=cues,reflow200Percent=reflow,
    minimumTouchDp=48,drivingTransportDp=64,drivingMicDp=72,drivingExitDp=56,fontScalePercent=[100,150,200],focusRestore=True,privacyMinimized=True,qa="NOT_RUN"))
assert len(a)==45
system_rows=[]
for s in reg["states"]:
    if s["type"]!="SYSTEM_ONLY_HANDOFF":continue
    id=s["id"]; matches=[t for t in plat["transitions"] if id in t["systemStates"]]
    assert matches,id
    assert s.get("appScreen") is None and s.get("image") is None
    prior=get(s["spec"]);doc=SYS/(id+".md")
    lines=["# Phase 7 system-only ownership handoff — "+id+" / "+s["state"],"",
    "**Disposition:** SYSTEM_ONLY, NO APP SCREEN, runtime QA NOT RUN.",
    "**Existing REAL state owner (unaltered):** "+s["owner"],
    "**Platform screen owner (unaltered):** "+s["screenOwner"],
    "**Return/recovery owner (unaltered):** "+s["recoveryOwner"],
    "**UF:** "+s["uf"]+"; **WF:** "+", ".join(s["wf"]),
    "**Source spec:** ["+prior.name+"]("+link(prior,SYS)+")","",
    "## Actual platform transition and return"]
    for t in matches:
        lines +=["","### "+t["id"]+" / "+t["title"],
                "- Trigger: "+t["trigger"],
                "- Handoff to REAL owner: "+t["handoff"],
                "- Authoritative playback/service state: "+t["service"],
                "- Return recheck (never infer from onResume alone): "+t["returnPath"],
                "- Safe terminal: "+t["terminal"],
                "- Retry/recovery bounds: "+t["retry"]]
    lines +=["","## Android accessibility and privacy boundary","",
     "- App owns spoken purpose on its pre-handoff action only; Android/provider owns dialog/Settings/lockscreen/notification/vehicle TalkBack labels, focus and drawing.",
     "- After system return, re-query actual grant/MediaSession/route/provider; restore focus to original app action or safe recovery status.",
     "- Do not create a fake app UI representation of this native system surface or add a new navigation destination.",
     "- No automatic microphone/Settings/consent request from Driving; no Android Auto MVP.",
     "- "+privacy,
     "- Cloud processing disabled until separately approved provider/legal privacy review (PH10-MIN-002).",
     "","## Real QA evidence (NOT RUN)","",
     "- [ ] Native platform handoff/return tested on Android 10/12/14+.",
     "- [ ] TalkBack OS ownership and app-origin focus restoration captured with redacted test content.",
     "- [ ] Safe paused/manual terminal and fresh authoritative recheck verified.",
     "- [ ] No invented app screen or personal data leakage in evidence.",""]
    doc.write_text("\n".join(lines),encoding="utf-8")
    system_rows.append(dict(id=id,type="SYSTEM_ONLY",wf=s["wf"],uf=s["uf"],owner=s["owner"],screenOwner=s["screenOwner"],recoveryOwner=s["recoveryOwner"],sourceSpec=str(prior),spec=str(doc),
       transitions=[t["id"] for t in matches],safeTerminals=sorted(set(t["terminal"] for t in matches)),appScreen=None,image=None,focusReturn=True,privacyMinimized=True,qa="NOT_RUN"))
assert len(system_rows)==20
assert {r["id"] for r in system_rows}==set(plat["canonicalSystemOnly"])
assert len({r["id"] for r in a+system_rows})==65
register=dict(issue=17,phase=7,date="2026-10-11",sourceArchiveSha256=can["sourceArchiveSha256"],
    status="PASS_DOCUMENTATION_45_20_RUNTIME_PENDING",original126States=126,
    counts=dict(appVisible=len(a),systemOnly=len(system_rows),missing=0,duplicate=0,unassigned=0,fakeAppSystemScreens=0),
    appVisible=a,systemOnly=system_rows,runtimeComposeTested=False,independentHumanApproval=False)
(OUT/"PHASE_7_REGISTER.json").write_text(json.dumps(register,indent=2,ensure_ascii=False)+"\n")
matrix=["# Issue #17 Phase 7 — Accessibility / Privacy / System Boundary Matrix","",
"**Date:** 11 October 2026. **Gate:** PASS (documentation coverage only). **Actual Compose/device QA:** NOT RUN.",
"**Authority:** Phase-5 126-state register; Phase-6 specialist UX; Issue-16 PH10-MIN-001; Android system transition register.",
"","| Gate | Target | Verified |","|---|---:|---:|","| App-visible A11y disposition | 45 | 45 |","| System-only owner/return evidence | 20 | 20 |","| Missing / duplicate / unassigned | 0 | 0 |","| Fabricated Android/system app screens | 0 | 0 |","",
"## 45 app-visible State contracts","",
"| State | WF | Reading order/TalkBack/200%/48dp/Privacy | Source visual |",
"|---|---|---|---|"]
for x in a:
    matrix.append("| "+x["id"]+" | "+", ".join(x["wf"])+" | [A11y disposition]("+link(x["spec"],OUT)+") | [review image]("+link(x["sourceArt"],OUT)+") |")
matrix +=["","## 20 system-only owner contracts","",
"| State | Real owner | Actual platform screen owner | Return owner | System handoff |",
"|---|---|---|---|---|"]
for x in system_rows:
    matrix.append("| "+x["id"]+" | "+x["owner"].replace("|","/")+" | "+x["screenOwner"].replace("|","/")+" | "+x["recoveryOwner"].replace("|","/")+" | [owner + return contract]("+link(x["spec"],OUT)+") |")
matrix +=["","## Downstream specs","",
"- [PH10-MIN-001 Compose semantics, 200% reflow, focus](COMPOSE_A11Y_HANDOFF.md)",
"- [Privacy and safe evidence contract](PRIVACY_EVIDENCE_HANDOFF.md)",
"- [OS and service ownership boundary](SYSTEM_BOUNDARY_HANDOFF.md)",
"- [65 real-device test handoffs (NOT RUN)](PHASE_7_DEVICE_QA_MATRIX.md)",
"- [Machine-readable register](PHASE_7_REGISTER.json)",
"- [Automatic validation](PHASE_7_VALIDATION.md)","",
"**Scope:** no new Screen Family, navigation, Android dialog or app-owned media control; Phase-5 and Phase-6 artwork retained. Static documentation does not close the implementation/runtime PH10-MIN-001 finding.",""]
(OUT/"ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md").write_text("\n".join(matrix),encoding="utf-8")
qa=["# Phase 7 per-state Android implementation QA — all NOT RUN","",
"Use Android 10/12/14+, Pixel/Samsung/Xiaomi and selected vehicle routes. Attach reviewer/date, device, sanitized Compose semantics snapshot and redacted TalkBack evidence.",
"","| Case | ID | Manual/runtime action | Required result | Observed |",
"|---|---|---|---|---|"]
for n,x in enumerate(a,1):
    qa.append("| A11Y-"+str(n).zfill(3)+" | "+x["id"]+" | 100/150/200% font + TalkBack traversal + target audit | No clipping; announced state/action; valid focus/48dp; privacy-safe | NOT RUN |")
for n,x in enumerate(system_rows,1):
    qa.append("| OS-"+str(n).zfill(3)+" | "+x["id"]+" | Platform-to-app transfer and return + native TalkBack | OS owns native surface, recheck/recover, restore app focus, no false screen | NOT RUN |")
qa +=["","No runtime assertions are considered PASS until observed evidence, reviewer identity/date and product/privacy review are recorded.",""]
(OUT/"PHASE_7_DEVICE_QA_MATRIX.md").write_text("\n".join(qa),encoding="utf-8")
print("GEN_PASS",len(a),len(system_rows))
