#!/usr/bin/env python3
"""Issue 17 Phase 6. Specialist static UX renders from approved WF-07/08 and UF-07..10."""
from pathlib import Path
import hashlib,json,math
from PIL import Image,ImageDraw,ImageFont
BASE=Path("docs/high-fidelity")
OUT=BASE/"specialist"/"phase-6"
src=json.loads(Path("docs/design-system/HIFI_TRACEABILITY_REGISTER.json").read_text())
old=json.loads((BASE/"HIFI_STATE_COVERAGE_REGISTER.json").read_text())
tok=json.loads(Path("docs/design-system/foundations/TOKENS.json").read_text())
cmp=json.loads(Path("docs/design-system/components/COMPONENT_TOKENS.json").read_text())["components"]
assert src["sourceArchiveSha256"]=="0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8"
assert len(src["states"])==len(old["states"])==126
assert set(["CMP-05","CMP-06","CMP-07","CMP-16","CMP-19"]).issubset(cmp)
states={s["stateId"]:s for s in src["states"]}
clr=tok["color"]["semantic"]
BG=clr["background"];SUR=clr["surfaceVariant"];TEXT=clr["textPrimary"];SOFT=clr["textSecondary"]
BORDER=clr["border"];CYAN=clr["primary"];DARK=clr["onPrimary"];DIM=clr["disabledContent"]
AMBER=clr["warning"];GREEN=clr["success"];ERROR=clr["error"]
fontfile=next(p for p in ["/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf","/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"] if Path(p).is_file())
boldfile=next(p for p in ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf","/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf"] if Path(p).is_file())
def ft(sz,b=False):return ImageFont.truetype(boldfile if b else fontfile,round(sz*3))
def z(x):return round(x*3)
def rr(d,rect,color=BG,r=0,outline=None):
    d.rounded_rectangle(tuple(z(n) for n in rect),radius=z(r),fill=color,outline=outline,width=3)
def tt(d,s,x,y,sz=16,b=True,col=TEXT,max_width=330,align="mm"):
    f=ft(sz,b)
    while d.textbbox((0,0),s,font=f)[2]>z(max_width) and sz>10:
        sz-=1;f=ft(sz,b)
    assert d.textbbox((0,0),s,font=f)[2]<=z(max_width),s
    d.text((z(x),z(y)),s,font=f,fill=col,anchor=align)
def btn(d,s,x,y,w,h,mode="secondary"):
    assert w>=48 and h>=48
    fill=CYAN if mode=="primary" else clr["disabledContainer"] if mode=="disabled" else SUR
    fg=DARK if mode=="primary" else DIM if mode=="disabled" else TEXT
    rr(d,(x,y,x+w,y+h),fill,12,BORDER if mode!="primary" else None)
    tt(d,s,x+w/2,y+h/2,15,True,fg,w-10)
def foundation():
    im=Image.new("RGB",(1080,2400),BG);d=ImageDraw.Draw(im)
    rr(d,(0,0,360,24),SUR);tt(d,"9:41",18,12,11,False,SOFT,40,align="lm")
    rr(d,(0,778,360,800),SUR)
    d.line((z(154),z(792),z(206),z(792)),fill=SOFT,width=5)
    return im,d
def driving(kind,can_last=False):
    im,d=foundation()
    tt(d,"DRIVING MODE",20,58,14,True,CYAN,130,align="lm")
    btn(d,"Exit",277,32,67,58,"primary")
    rr(d,(92,126,268,302),SUR,18,BORDER)
    empty=kind in ("NO_TRACK","LAST_VERIFIED")
    tt(d,"NO TRACK" if empty else "ARTWORK",180,215,21)
    title="No current track" if empty else "Aurora Lights"
    note="Exit to browse local music" if empty else "Local artist"
    tt(d,title,180,337,23,max_width=320)
    tt(d,note,180,374,15,False,SOFT,325)
    status={"PLAY":"PLAYING","PAUSE":"PAUSED","NO_TRACK":"NO TRACK","LAST_VERIFIED":"NO TRACK","RECONNECT":"RECONNECTING","PENDING":"PENDING","ROUTE":"PAUSED"}[kind]
    tt(d,status,180,426,16,True,AMBER if kind in ("PENDING","RECONNECT","ROUTE") else CYAN)
    aux={"RECONNECT":"Checking playback state","ROUTE":"Resume manually when safe","PENDING":"Waiting for playback"} .get(kind)
    if aux:tt(d,aux,180,456,12,False,SOFT)
    labels=["Prev","Pause" if kind=="PLAY" else "Play","Next","-10s"]
    if empty:labels=["Prev","Play last" if can_last else "","Next","-10s"]
    for i,s in enumerate(labels):
        if not s:continue
        disabled=(empty and s!="Play last") or (kind in ("RECONNECT","PENDING","ROUTE") and s not in ("Play","Pause"))
        btn(d,s,12+i*86,480,78,72,"disabled" if disabled else "primary" if s in ("Pause","Play","Play last") else "secondary")
    btn(d,"Mic · Voice",93,602,174,78,"primary")
    if empty:tt(d,"No typing, no lists, no automatic playback",180,722,12,False,SOFT,330)
    copy=["DRIVING MODE","Exit","NO TRACK" if empty else "ARTWORK",title,note,status]
    if aux:copy.append(aux)
    copy.extend([s for s in labels if s]);copy+=["Mic · Voice"]
    if empty:copy+=["No typing, no lists, no automatic playback"]
    return im,copy
VOICE={
"LISTEN1":("Listening","Six-second attempt","Listening"),
"LISTEN2":("Listening","Final six-second attempt","Listening"),
"PROCESS":("Processing","Mapping one of six intents","Processing"),
"PENDING":("Processing","Awaiting player acknowledgement","Processing"),
"CONFIRM1":("Which artist?","Choose one presented candidate","Confirming"),
"CONFIRM2":("Which artist?","Choose one presented candidate","Confirming"),
"DONE":("Done","Next track acknowledged","Executed"),
"TTS_FAILED":("Done","Next track acknowledged","Executed"),
"TIMEOUT":("Voice attempt failed","One Retry, then Manual","Retry"),
"RETRY":("Voice failed","One Retry, then Manual","Retry"),
"TERMINAL":("Voice unavailable","Manual controls only","Failed"),
"NOT_FOUND":("Artist not found","Manual controls only","Failed"),
"UNKNOWN":("Command not understood","Manual controls only","Failed"),
"UNSUPPORTED":("Request not supported","Manual controls only","Failed"),
"MISSING_ARTIST":("Artist is missing","Manual controls only","Failed"),
"CONFIRM_FAILED":("Confirmation failed","Manual controls only","Failed"),
"REJECTED":("Voice command failed","Manual controls only","Failed"),
"PROVIDER":("Voice unavailable","Manual controls only","Failed"),
"PERMISSION":("Voice unavailable","Manual controls only","Failed"),
"MANUAL":("Voice unavailable","Manual controls only","Failed"),
}
def voice(kind):
    im,d=foundation()
    # This is the SAME overlay on the existing Driving origin, not a destination.
    tt(d,"DRIVING MODE",18,55,12,True,CYAN,145,align="lm")
    btn(d,"Exit",279,30,64,57,"primary")
    rr(d,(13,153,347,684),SUR,22,BORDER)
    title,detail,group=VOICE[kind]
    tt(d,title,180,214,24,max_width=312)
    tt(d,detail,180,257,14,False,SOFT,308)
    rr(d,(146,290,214,358),clr["primaryContainer"] if group not in ("Failed","Retry") else clr["errorContainer"],34)
    icon="✓" if group=="Executed" else "!" if group in ("Failed","Retry") else "●" if group=="Listening" else "?" if group=="Confirming" else "…"
    tt(d,icon,180,323,26,True,GREEN if group=="Executed" else ERROR if group in ("Failed","Retry") else CYAN)
    copy=[title,detail,icon]
    if group=="Confirming":
        opts=["1 · Aurora"]+(["2 · Aurora Aksnes"] if kind=="CONFIRM2" else [])
        for j,label in enumerate(opts):btn(d,label,31,378+56*j,298,52,"primary" if j==0 else "secondary")
        btn(d,"Neither",31,506,298,51)
        btn(d,"Cancel",31,569,298,51)
        copy+=opts+["Neither","Cancel"]
    elif group=="Retry":
        btn(d,"Retry once",31,482,298,57,"primary")
        btn(d,"Manual controls",31,554,298,54)
        copy+=["Retry once","Manual controls"]
    elif group=="Executed":
        btn(d,"Dismiss",31,554,298,54)
        copy+=["Dismiss"]
    elif group in ("Listening","Processing"):
        btn(d,"Cancel",31,554,298,54)
        tt(d,"Manual controls remain available",180,643,11,False,SOFT,317)
        copy+=["Cancel","Manual controls remain available"]
    else:
        btn(d,"Manual controls",31,554,298,54,"primary")
        copy+=["Manual controls"]
    # Driving transport remains a service-owned origin; explicit Manual closes overlay.
    tt(d,"Return to manual playback",180,717,11,False,SOFT)
    return im,copy+["Return to manual playback"]
DATA=[
("DRV_PLAYING","WF-07","CC-05","PLAY","manual"),
("DRV_PAUSED","WF-07","CC-06","PAUSE","manual"),
("DRV_NO_TRACK","WF-07","CC-07","NO_TRACK","manual"),
("DRV_LAST_VERIFIED","WF-07","CC-07","LAST_VERIFIED","manual"),
("DRV_RECONNECT","WF-07","CC-03","RECONNECT","manual"),
("DRV_COMMAND_PENDING","WF-07","CC-08","PENDING","manual"),
("DRV_ROUTE_SAFE","WF-07","CC-16","ROUTE","safe_paused"),
("VO_LISTEN_1","WF-08","CD-03","LISTEN1","manual"),
("VO_LISTEN_2","WF-08","CD-04","LISTEN2","manual"),
("VO_PROCESSING","WF-08","CD-05","PROCESS","manual"),
("VO_COMMAND_PENDING","WF-08","CD-06","PENDING","manual"),
("VO_CONFIRM_ONE","WF-08","CD-09","CONFIRM1","manual"),
("VO_CONFIRM_TWO","WF-08","CD-10","CONFIRM2","manual"),
("VO_EXECUTED","WF-08","CD-07","DONE","acknowledged"),
("VO_TTS_UNAVAILABLE","WF-08","CD-07","TTS_FAILED","acknowledged"),
("VO_STT_TIMEOUT","WF-08","CD-17","TIMEOUT","manual"),
("VO_RETRY_AVAILABLE","WF-08","CD-18","RETRY","manual"),
("VO_ATTEMPT2_TERMINAL","WF-08","CD-19","TERMINAL","manual"),
("VO_ARTIST_NOT_FOUND","WF-08","CD-13","NOT_FOUND","manual"),
("VO_UNKNOWN_PHRASE","WF-08","CD-14","UNKNOWN","manual"),
("VO_UNSUPPORTED","WF-08","CD-15","UNSUPPORTED","manual"),
("VO_MISSING_ARTIST","WF-08","CD-16","MISSING_ARTIST","manual"),
("VO_CONFIRMATION_FAILED","WF-08","CD-12","CONFIRM_FAILED","manual"),
("VO_EXECUTION_REJECTED","WF-08","CD-08","REJECTED","manual"),
("VO_PROVIDER_UNAVAILABLE","WF-08","CD-22","PROVIDER","manual"),
("VO_PERMISSION_DENIED","WF-08","CD-21","PERMISSION","manual"),
("VO_MANUAL_ONLY","WF-08","CD-20","MANUAL","manual")
]
assert len(DATA)==27 and len({x[0] for x in DATA})==27
OUT.mkdir(parents=True,exist_ok=True)
records=[]
for name,wf,sid,kind,terminal in DATA:
    assert sid in states and wf in states[sid]["wf"]
    image,copy=driving(kind,kind=="LAST_VERIFIED") if wf=="WF-07" else voice(kind)
    p=OUT/(name+".png")
    image.save(p,"PNG",optimize=True)
    with Image.open(p) as im:im.verify()
    sha=hashlib.sha256(p.read_bytes()).hexdigest()
    before=BASE/wf/"states"/("HF_STATE_"+sid+".png")
    if not before.exists():
        before=BASE/("state-variants" if states[sid]["hifiDisposition"]["type"]=="HIFI_VARIANT_ANNOTATION" else "system-handoffs")/(sid+".md")
    assert before.exists(),before
    md=OUT/(name+".md")
    md.write_text("\n".join([
        "# Issue 17 Phase 6 specialist — "+name,"",
        "Canonical State: "+sid+"; source WF: "+wf+"; UF: "+states[sid]["uf"]+"; Golden Scenario: "+states[sid]["gs"]+".",
        "Source artwork or state contract: "+str(before), "SHA-256 PNG: "+sha+".",
        "Terminal: "+terminal+"; authority: "+states[sid]["ownership"]+".",
        "", "## Exact on-screen UI copy", "",
        *("- "+json.dumps(s,ensure_ascii=False) for s in copy),
        "", "## Binding behavior", "",
        "- Reuses original WF-07 single-layer screen or WF-08 modal on Driving/Player origin; no navigation destination, permission dialog, list, keyboard, Android Auto, account or streaming.",
        "- Source playback is MediaSessionService PlaybackSnapshot; never optimistic success or a second playback owner.",
        "- VoiceSessionCoordinator owns active capture and confirmation; six English deterministic intents, manual push-to-talk, maximum six-second capture, exactly one eligible explicit STT retry.",
        "- One confirmation only, with 1..2 immutable candidate labels from current Confirming.options. Neither and Cancel are recovery actions, not candidate objects.",
        "- Unsupported/unknown/no artist/not found/confirmation-failed/execution-rejected/provider-unavailable return Manual; no approximated command and no additional prompt/Retry loop.",
        "- Executed is shown only after acknowledged service command; a TTS failure does not undo playback success. Optional TTS output is no more than one short sentence.",
        "- Driving Exit or Android Back immediately returns to the origin or Home/Library fallback without stopping service playback; Cancel releases capture/duck and discards stale callbacks.",
        "- The conditional Play last affordance exists only after authoritative validation of one local item; when absent it is hidden and unsupported controls are inert.",
        "- Visual touch bounds: 78dp Driving transport, 78dp Mic, 67dp Exit; all overlay controls at least 51dp high. Reflow and TalkBack require runtime QA.",
        "- No raw audio, transcripts, local media titles, confidence or candidate labels in analytics or runtime diagnostic uploads.",
        "", "## Review status", "",
        "Deterministically generated specialist PNG/spec evidence for review. No human approval, Compose runtime QA or hardware test is claimed.",""
    ]),encoding="utf-8")
    records.append(dict(id=name,wf=wf,stateId=sid,kind=kind,terminal=terminal,image=str(p),spec=str(md),sha256=sha,uiCopy=copy))
# Explicit approved transitions, NOT new routes: one modal/Driving surface reused.
GRAPH={
"DRV_PLAYING":["DRV_PAUSED","DRV_COMMAND_PENDING","VO_LISTEN_1","DRV_ROUTE_SAFE"],
"DRV_PAUSED":["DRV_PLAYING","DRV_NO_TRACK","VO_LISTEN_1"],
"DRV_NO_TRACK":["VO_LISTEN_1"],
"DRV_LAST_VERIFIED":["DRV_COMMAND_PENDING"],
"DRV_RECONNECT":["DRV_PLAYING","DRV_PAUSED","DRV_NO_TRACK"],
"DRV_COMMAND_PENDING":["DRV_PLAYING","DRV_PAUSED","DRV_RECONNECT"],
"DRV_ROUTE_SAFE":["DRV_PAUSED"],
"VO_LISTEN_1":["VO_PROCESSING","VO_STT_TIMEOUT","VO_MANUAL_ONLY"],
"VO_LISTEN_2":["VO_PROCESSING","VO_ATTEMPT2_TERMINAL","VO_MANUAL_ONLY"],
"VO_PROCESSING":["VO_COMMAND_PENDING","VO_CONFIRM_ONE","VO_CONFIRM_TWO","VO_ARTIST_NOT_FOUND","VO_UNKNOWN_PHRASE","VO_UNSUPPORTED","VO_MISSING_ARTIST"],
"VO_COMMAND_PENDING":["VO_EXECUTED","VO_EXECUTION_REJECTED"],
"VO_CONFIRM_ONE":["VO_COMMAND_PENDING","VO_CONFIRMATION_FAILED","VO_MANUAL_ONLY"],
"VO_CONFIRM_TWO":["VO_COMMAND_PENDING","VO_CONFIRMATION_FAILED","VO_MANUAL_ONLY"],
"VO_EXECUTED":["VO_TTS_UNAVAILABLE","VO_MANUAL_ONLY"],
"VO_TTS_UNAVAILABLE":["VO_MANUAL_ONLY"],
"VO_STT_TIMEOUT":["VO_RETRY_AVAILABLE","VO_MANUAL_ONLY"],
"VO_RETRY_AVAILABLE":["VO_LISTEN_2","VO_MANUAL_ONLY"],
"VO_ATTEMPT2_TERMINAL":["VO_MANUAL_ONLY"],
"VO_ARTIST_NOT_FOUND":["VO_MANUAL_ONLY"],
"VO_UNKNOWN_PHRASE":["VO_MANUAL_ONLY"],
"VO_UNSUPPORTED":["VO_MANUAL_ONLY"],
"VO_MISSING_ARTIST":["VO_MANUAL_ONLY"],
"VO_CONFIRMATION_FAILED":["VO_MANUAL_ONLY"],
"VO_EXECUTION_REJECTED":["VO_MANUAL_ONLY"],
"VO_PROVIDER_UNAVAILABLE":["VO_MANUAL_ONLY"],
"VO_PERMISSION_DENIED":["VO_MANUAL_ONLY"],
"VO_MANUAL_ONLY":[]
}
assert len(GRAPH)==27 and set(GRAPH)=={r["id"] for r in records}
assert all(set(e).issubset(GRAPH) for e in GRAPH.values())
assert GRAPH["VO_ATTEMPT2_TERMINAL"]==["VO_MANUAL_ONLY"]
assert GRAPH["VO_UNSUPPORTED"]==["VO_MANUAL_ONLY"]
assert GRAPH["VO_TTS_UNAVAILABLE"]==["VO_MANUAL_ONLY"]
by={r["id"]:r for r in records}
for k,v in by.items():
    if "Retry once" in v["uiCopy"]:assert k in ("VO_STT_TIMEOUT","VO_RETRY_AVAILABLE")
assert len([x for x in by["VO_CONFIRM_ONE"]["uiCopy"] if x.startswith(("1 ·","2 ·"))])==1
assert len([x for x in by["VO_CONFIRM_TWO"]["uiCopy"] if x.startswith(("1 ·","2 ·"))])==2
with Image.open(by["VO_EXECUTED"]["image"]) as a,Image.open(by["VO_TTS_UNAVAILABLE"]["image"]) as b:
    assert a.tobytes()==b.tobytes()
assert "Play last" not in by["DRV_NO_TRACK"]["uiCopy"]
assert "Play last" in by["DRV_LAST_VERIFIED"]["uiCopy"]
assert all("Settings" not in v["uiCopy"] and "Search" not in v["uiCopy"] for v in by.values())
# Contact sheet is review convenience, individual images remain 1080x2400.
TW,TH=210,530;cols=6
board=Image.new("RGB",(cols*TW+24,math.ceil(len(records)/cols)*TH+95),BG)
d=ImageDraw.Draw(board)
d.text((24,18),"ISSUE 17 / PHASE 6 SPECIALIST - DRIVING + VOICE",font=ft(10,True),fill=TEXT)
d.text((24,53),"27 source-locked specialist cases: 7 Driving / 20 Voice",font=ft(7),fill=SOFT)
for i,r in enumerate(records):
    x=12+(i%cols)*TW;y=90+(i//cols)*TH
    with Image.open(r["image"]) as im:
        board.paste(im.resize((196,438),Image.Resampling.LANCZOS),(x,y))
    d.text((x,y+443),r["id"][:24],font=ft(5,True),fill=CYAN)
    d.text((x,y+468),r["stateId"],font=ft(5),fill=SOFT)
board_path=OUT/"PHASE_6_SPECIALIST_BOARD.png";board.save(board_path,"PNG",optimize=True)
v=dict(issue=17,phase=6,date="2026-10-11",status="PASS_STATIC_SPECIALIST_DESIGN_PENDING_HUMAN_AND_RUNTIME_QA",
       archiveSha256=src["sourceArchiveSha256"],drivingCases=7,voiceCases=20,total=27,
       missing=[],duplicate=[],newCanonicalStates=0,newScreenFamilies=0,systemScreensInvented=0,
       maxRetry=1,maxConfirmationRounds=1,maxCandidates=2,captureWindowSeconds=6,
       ttsMaxSentences=1,ttsFailureRetainsExecuted=True,unsupportedApproximation=False,
       manualAlwaysReachableViaCancel=True,drivingExitImmediate=True,verifiedLastItemGuard=True,
       boardSha256=hashlib.sha256(board_path.read_bytes()).hexdigest(),
       approvalClaimed=False,composeRuntimeTested=False)
(OUT/"PHASE_6_REGISTER.json").write_text(json.dumps(dict(validation=v,scenarios=records,transitions=GRAPH),indent=2,ensure_ascii=False)+"\n")
matrix=["# Phase 6 — specialist Driving Mode and Voice UX pass","",
        "Result: PASS on source-locked STATIC reviewable design coverage; human/physical-device sign-off NOT asserted.","",
        "Source: frozen Issue 15 WF-07/08, GitHub UF-07..UF-10, Issue 16 accepted voice confirmation architecture, Phase 5 state IDs.",
        "User reference RAR SHA-256: "+src["sourceArchiveSha256"],"",
        "## Acceptance invariants","",
        "- Driving Playing/Paused/No Track distinct; no screen-depth increase, no automatic entry, immediate Exit at all times.",
        "- Last item absent means Play last hidden and all unsupported transport actions visibly disabled; with verified local item, Play last is conditional.",
        "- Manual controls preserved by return from Voice; Exit remains exposed behind the overlay.",
        "- Listening, Processing, Confirming, Executed and Failed have specialist images; all UF-08/09/10 error classes have dedicated evidence.",
        "- Only eligible first settled STT failure offers one Retry, attempt 2 never offers another; no automatic capture.",
        "- Only one confirmation with max two immutable options; neither/Cancel recover without loops; unsupported never approximated.",
        "- TTS <=1 sentence after acknowledged execution, no success reversal on TTS failure; two executed rasters verified identical.",
        "- No fake Android permission/system screen; no Settings/cloud-consent dialog inside Driving.",
        "- All source IDs preserved; one MediaSession playback authority, one VoiceSessionCoordinator.",
        "","## 27 individually reviewable cases","",
        "| Scenario | WF | Existing State ID | PNG | Paired exact-copy spec |",
        "|---|---|---|---|---|"]
for r in records:
    matrix.append("| "+r["id"]+" | "+r["wf"]+" | "+r["stateId"]+" | ["+Path(r["image"]).name+"]("+Path(r["image"]).name+") | ["+Path(r["spec"]).name+"]("+Path(r["spec"]).name+") |")
matrix+=["","## Confirmed Phase-5 defects and specialist overrides","",
        "- CD-13/14/15/16, CD-08 and CD-12 inherited a blanket Retry once button in generic Failed artwork; this specialist variant correctly returns to Manual and has no STT Retry.",
        "- CC-07 previously rendered active Play last even without a verified last item. Specialist DRV_NO_TRACK omits it; DRV_LAST_VERIFIED shows the separate conditional same-state variant.",
        "- CD-19 is terminal (no third Retry). TTS failure after CD-07 service ack does not change the Executed surface.",
        "- This specialist artwork supersedes those conflicting generic cases in Phase 5; the original 126-state disposition remains unchanged.",
        "","## Important non-visual handoff requirements","",
        "- Cancel/Back/Exit invalidates Voice generation, releases microphone/duck and ignores late STT/TTS events.",
        "- Audio-focus loss, reconnect, stale queue and route interruption are service-owned; safe paused with explicit manual resume if guard fails.",
        "- Confirmation labels and local media metadata are for UI only, not logs, crash reports or analytics.",
        "- P1 Bluetooth is only a dismissible suggestion outside Driving, never an automatic entrance.",
        "- At 200 percent text scaling, Compose must reflow; screenshot review alone cannot prove runtime touch targets, actual TalkBack announcements or eyes-off-road safety.",
        "","## Evidence","",
        "- [Visual review board](PHASE_6_SPECIALIST_BOARD.png)",
        "- [Machine-readable register and state transitions](PHASE_6_REGISTER.json)",
        "- [Automated validation JSON](PHASE_6_VALIDATION.json)",
        "- [Phase-5 parent matrix](../../HIFI_STATE_COVERAGE_MATRIX.md)",""]
(OUT/"PHASE_6_SPECIALIST_MATRIX.md").write_text("\n".join(matrix)+"\n")
(OUT/"PHASE_6_VALIDATION.json").write_text(json.dumps(v,indent=2)+"\n")
(OUT/"PHASE_6_VALIDATION.md").write_text("# Phase 6 validation\n\n**"+v["status"]+"**\n\n- 7/7 Driving variants and 20/20 Voice variants rendered as 1080x2400 PNG and exact-copy spec.\n- Original State IDs traced: 27/27; missing/duplicate/unassigned: 0/0/0.\n- One STT Retry, only one confirmation, 1..2 candidates, at most 6s capture, <=1 sentence TTS.\n- Executed and TTS-unavailable-after-ack raster contents identical.\n- Unsupported/not-found no approximation and no Retry; terminal failure no third Retry.\n- No fake Android OS screens; no extra destination; no unverified Play last.\n\n**Limit:** Static artifact QA is not independent Product/UX approval or physical-device/Compose accessibility testing.\n")
print(json.dumps(v,sort_keys=True))
