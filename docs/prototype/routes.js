/* Issue #17 Phase 8. Repository-native, source-locked, clickable image prototype.
   All product actions originate from approved UF/WF/GS. Events marked "simulate"
   or "system" exist ONLY in the external review controls, not in the app UI. */
(function (root) {
  "use strict";
  const h = name => "../high-fidelity/" + name;
  const frame = (wf, name) => h("WF-"+wf+"/frames/HF_FRAME_WF-"+wf+"_"+name+".png");
  const state = (wf, id) => h("WF-"+wf+"/states/HF_STATE_"+id+".png");
  const voice = name => h("specialist/phase-6/VO_"+name+".png");
  const driving = name => h("specialist/phase-6/DRV_"+name+".png");
  const tap = (label,to,x,y,w,ht,note="") => ({label,to,kind:"tap",rect:[x,y,w,ht],note});
  const simulate = (label,to,note="") => ({label,to,kind:"simulate",note});
  const system = (label,to,note="") => ({label,to,kind:"system",note});
  const node = (id,stateId,wf,image,title,summary,edges,owner="App") =>
    ({id,stateId,wf,image,title,summary,edges,
      owner:owner!=="App"?owner:(wf==="WF-08"?"App + Voice coordinator":
        (wf==="WF-06"||wf==="WF-07")?"App + Playback Service":
        wf==="WF-03"?"App / Android permission boundary":"App"),
      systemOnly:false});
  const boundary = (id,stateId,title,summary,edges,owner) =>
    ({id,stateId,wf:null,image:null,title,summary,edges,owner,systemOnly:true});
  const nodes = [
    node("splash","S-01","WF-01",frame("01","SPLASH_LOADING"),"Verified startup","Startup and migration status is checked before navigation.",[
      simulate("Verified startup → onboarding","welcome"),
      simulate("Migration error","migration-error","Recovery remains inside UF-01")
    ]),
    node("migration-error","S-01","WF-01",frame("01","SPLASH_STARTUP_MIGRATION_ERROR"),"Startup / migration failure","No destructive reset; verified local data is retained.",[
      tap("Retry startup","splash",50,440,260,58),
      simulate("Retry fails; remain on recovery","migration-error")
    ]),
    node("welcome","S-02","WF-02",frame("02","ONBOARDING_SLIDE_1"),"Local-first onboarding","Account-free introduction; optional Voice education is not a prerequisite.",[
      tap("Continue","permission",38,510,284,61),
      simulate("Skip optional Voice education","permission")
    ]),
    node("education","CA-01","WF-02",frame("02","ONBOARDING_SLIDE_2_SKIP_VOICE"),"Six-command education","The optional tutorial explains six deterministic English intents.",[
      tap("Skip optional Voice","library",38,510,284,63),
      simulate("Finish optional education","library")
    ]),
    node("permission","S-03","WF-03",frame("03","PERMISSIONS_RATIONALE_REQUEST"),"Local-media permission rationale","User-controlled request. Android—not the app—owns the resulting dialog.",[
      tap("Continue to Android request","sys-media",40,487,280,57),
      tap("Not now","denied",40,552,280,54)
    ]),
    boundary("sys-media","SYS-01","Android media permission boundary","Platform-native permission dialog deliberately NOT rendered. Select an OS result in the external simulator.",[
      system("Android grants media access","granted"),
      system("Android denies (requestable)","denied"),
      system("Android permanently denies","permanent")
    ],"Android/System"),
    node("denied","S-03","WF-03",frame("03","PERMISSIONS_DENIED"),"Media access unavailable","Manual fallback remains available; no false claim of library readiness.",[
      tap("Retry permission explanation","permission",40,490,280,58),
      simulate("Platform reports permanent denial","permanent"),
      simulate("Not now: remain unavailable","denied")
    ]),
    node("permanent","S-03","WF-03",frame("03","PERMISSIONS_PERMANENTLY_DENIED"),"Permanent media denial","An explicit Settings handoff is available outside Driving.",[
      tap("Open Android Settings","sys-settings",37,482,284,60),
      tap("Not now","denied",40,551,280,56)
    ]),
    boundary("sys-settings","SYS-04","Android Settings boundary","The native Settings page is Android-owned. App returns and rechecks media permission.",[
      system("After return: permission now granted","granted"),
      system("After return: still denied","permanent")
    ],"Android/System"),
    node("granted","S-03","WF-03",frame("03","PERMISSIONS_GRANTED"),"Media access granted","The actual platform grant is used; proceed to local MediaStore scan.",[
      simulate("Begin MediaStore scan","scanning")
    ]),
    node("scanning","S-04","WF-04",frame("04","HOME_LIBRARY_SCANNING"),"Scanning local music","MediaStore/index state is observed, not fabricated.",[
      simulate("Scan completes with library Ready","library"),
      simulate("Scan completes Empty","empty"),
      simulate("Recoverable scan failure","scan-error")
    ]),
    node("scan-error","S-04","WF-04",frame("04","HOME_LIBRARY_RECOVERABLE_ERROR"),"Recoverable scan error","Keep last verified library state; retry only on explicit action.",[
      tap("Retry scan","scanning",44,420,278,60),
      simulate("Return to verified Library","library")
    ]),
    node("empty","S-04","WF-04",frame("04","HOME_LIBRARY_EMPTY"),"Verified empty library","No accessible audio found. This is not a permission-denied state.",[
      tap("Rescan","scanning",42,430,276,62),
      simulate("Return to media permission recovery","permission")
    ]),
    node("library","CB-01","WF-04",state("04","CB-01"),"Ready Library","Songs, Artists, Albums and Folders are the existing manual browse categories.",[
      tap("Search tab","search",91,734,89,60),
      tap("Select verified library track","selection",160,167,188,65),
      tap("Open current mini-player","player",12,682,334,52),
      tap("Settings tab","settings",273,734,84,60),
      simulate("Replay optional six-command education","education"),
      simulate("Local library revoked / rescan","scan-error")
    ]),
    node("search","CB-09","WF-05",state("05","CB-09"),"Local Search / empty query","Search is strictly local. The sample query and results are synthetic.",[
      tap("Enter scripted local query","results",23,112,310,62),
      tap("Return to Library","library",1,29,50,58),
      simulate("Index query fails","search-error"),
      simulate("Query returns no results","search-none")
    ]),
    node("results","CB-10","WF-05",state("05","CB-10"),"Local Search results","Only the selected visible collection can be submitted to the one MediaSessionService.",[
      tap("Play local search result","selection",91,281,256,74),
      tap("Clear query","search",264,110,76,58),
      simulate("Selection stale or access revoked","stale"),
      simulate("Search index error","search-error"),
      simulate("No search result","search-none")
    ]),
    node("search-none","CB-11","WF-05",state("05","CB-11"),"Search: no results","Preserve query; no online-catalog fallback.",[
      tap("Clear preserved query","search",258,116,81,62),
      simulate("Edit query and find local results","results"),
      simulate("Return to Library","library")
    ]),
    node("search-error","CB-12","WF-05",state("05","CB-12"),"Search error","The verified library remains available and the entered query is preserved.",[
      tap("Retry Search","results",30,334,295,60),
      simulate("Return to Library","library")
    ]),
    node("stale","CB-13","WF-05",frame("05","SEARCH_RESULTS"),"Stale selection","Re-read MediaStore/access; never play a vanished item or replace prior service queue.",[
      simulate("Reconciliation succeeds; retry selection","selection"),
      simulate("Permission revoked; explain outside Driving","permission"),
      simulate("Return to preserved results","results")
    ]),
    node("selection","CB-14","WF-06",frame("06","FULL_PLAYER_PLAYING"),"Queue start pending","This is a reviewer annotation of the pending selected-collection handoff; no new loading screen.",[
      simulate("Playback Service acknowledges queue","player"),
      simulate("Service rejects selection; preserve results","results"),
      simulate("Selected file invalid","stale")
    ]),
    node("player","CB-15","WF-06",h("WF-06/HF-06_FULL_PLAYER_PLAYING.png"),"Full Player — Playing","Playback truth belongs to the single service-owned MediaSession.",[
      tap("Enter Driving Mode","driving",111,668,137,58),
      tap("Pause playback","player-paused",142,516,75,88),
      tap("Queue","queue",289,27,67,59),
      tap("Next track (acknowledged)","player",244,516,78,86),
      tap("Back to preserved Library","library",1,30,58,57),
      simulate("Corrupt local item encountered","corrupt"),
      simulate("Controller disconnects","reconnect"),
      simulate("Background app / screen off","background"),
      simulate("Call, focus or route interruption","route-event")
    ]),
    node("player-paused","CB-16","WF-06",state("06","CB-16"),"Full Player — Paused","Manual play only; no automatic restart.",[
      tap("Play after service acknowledgement","player",129,510,95,92),
      tap("Back to Library","library",1,32,57,59),
      simulate("Background while paused","background"),
      simulate("Route change remains Safe Paused","safe-paused")
    ]),
    node("queue","CB-18","WF-06",state("06","CB-18"),"Current service queue","Queue view is within the already-approved Player owner and visible source collection.",[
      tap("Back to Full Player","player",0,34,68,56),
      simulate("Current item becomes unavailable","corrupt")
    ]),
    node("corrupt","CB-19","WF-06",frame("06","FULL_PLAYER_CORRUPT_FILE_RECOVERY"),"Corrupt / inaccessible media","Bounded forward skip/reconcile; no delete, shuffle, wrap or fabricated online match.",[
      simulate("Verified next playable item exists","player"),
      simulate("No playable queue item remains","player-paused"),
      simulate("Return to Library/reconcile","library")
    ]),
    node("reconnect","CB-21","WF-06",frame("06","FULL_PLAYER_PLAYING"),"Controller reconnect","UI does not construct another player; it waits for complete service snapshot.",[
      simulate("Service snapshot confirms Playing","player"),
      simulate("Service snapshot confirms Paused","player-paused"),
      simulate("Service unavailable; no valid P1 restore","library")
    ]),
    boundary("background","CB-23","Android background / screen off","UI absent: playback remains service-owned. No fake notification or lockscreen is drawn.",[
      system("Compatible system controls are present","sys-controls"),
      system("Return to app; reconnect controller","reconnect"),
      system("OEM lifecycle destroyed service; valid P1 paused restore","restore-paused"),
      system("OEM lifecycle; no valid restore","library")
    ],"Android/System + Playback Service"),
    boundary("sys-controls","CB-26","Native MediaSession controls","Android owns notification, lock-screen and vehicle media-key surfaces. No mock system UI.",[
      system("OS Media Key command sent","sys-command"),
      system("OS surface unavailable / permission denied","notification-denied"),
      system("Return to app","reconnect")
    ],"Android/System + MediaSession"),
    boundary("sys-command","CB-29","System command awaiting service","Android/controller command is pending; no UI-owned optimistic status.",[
      system("Service acknowledges once","reconnect"),
      system("Command rejected; refresh verified state","reconnect")
    ],"Android/System + MediaSession"),
    node("notification-denied","CB-31","WF-06",frame("06","FULL_PLAYER_PLAYING"),"Notification unavailable","Android may hide the system controller; music/player remain truthful and manually controllable.",[
      simulate("Return to working player","player"),
      simulate("Use explicit OS Settings outside Driving","sys-settings-notification")
    ]),
    boundary("sys-settings-notification","CB-30","Notification permission/visibility boundary","An Android-owned notification-setting handoff. No in-app imitation.",[
      system("Return; refresh actual notification visibility","reconnect")
    ],"Android/System"),
    node("restore-paused","CB-32","WF-06",frame("06","FULL_PLAYER_PAUSED"),"P1 Paused Restore","Only validated persisted snapshot may restore an explicitly Paused state. This does not imply live P0 continuity.",[
      simulate("User explicitly requests Play; revalidate item","player"),
      simulate("Restore invalid; return Home","library")
    ]),
    node("driving","CC-05","WF-07",driving("PLAYING"),"Driving — Playing","Manual entry only; immediate Exit; no keyboard, Queue, Search or Settings.",[
      tap("Exit Driving now","player",270,27,86,64),
      tap("Pause","driving-paused",97,472,80,80),
      tap("Mic · Voice","voice-listen",91,595,179,91),
      tap("Next (service acknowledgement)","driving",182,474,80,79),
      simulate("Service reports No Track","driving-none"),
      simulate("Route interrupt during Driving","driving-safe"),
      simulate("Voice provider unavailable","voice-provider")
    ]),
    node("driving-paused","CC-06","WF-07",driving("PAUSED"),"Driving — Paused","Service-confirmed Paused. No automatic playback.",[
      tap("Play explicitly","driving",97,472,80,80),
      tap("Exit immediately","player-paused",270,27,86,64),
      tap("Mic · Voice","voice-listen",91,595,179,91),
      simulate("No verified track","driving-none")
    ]),
    node("driving-none","CC-07","WF-07",driving("NO_TRACK"),"Driving — No Track","No unverified active Play last. Disabled transport remains inert.",[
      tap("Exit to prior player","player",270,27,86,64),
      tap("Mic eligibility check","voice-provider",91,595,179,91),
      simulate("Valid last local track exists (guarded)","driving-last")
    ]),
    node("driving-last","CC-07","WF-07",driving("LAST_VERIFIED"),"Driving — verified Play last","Conditional same-state approved action. Verified local MediaStore identity first.",[
      tap("Play last after recheck","driving",97,472,80,80),
      tap("Exit Driving","player",270,27,86,64),
      simulate("Last item became unavailable","driving-none")
    ]),
    node("driving-safe","CE-06","WF-07",driving("ROUTE_SAFE"),"Driving — Safe Paused","Platform route/focus handoff stays Android/System-owned; UI shows only actual safe paused state.",[
      tap("Exit immediately","player-paused",270,27,86,64),
      tap("Explicit revalidated Play","driving",98,472,78,80),
      simulate("Route remains disconnected; pause","driving-safe")
    ]),
    node("voice-listen","CD-03","WF-08",voice("LISTEN_1"),"Voice — Listening","Explicit mic tap begins one bounded six-second capture. Simulated speech is selected from reviewer panel.",[
      tap("Cancel voice","driving",25,546,307,68),
      tap("Exit Driving","player",275,26,81,68),
      simulate("Supported NEXT recognized","voice-processing"),
      simulate("Six-second STT timeout","voice-timeout"),
      simulate("Mic permission denied","voice-permission"),
      simulate("Provider unavailable","voice-provider")
    ]),
    node("voice-listen-two","CD-04","WF-08",voice("LISTEN_2"),"Voice — final attempt","Explicit Retry consumed; no third attempt.",[
      tap("Cancel voice","driving",24,548,307,65),
      simulate("Second attempt succeeds","voice-processing"),
      simulate("Second attempt fails → terminal","voice-terminal")
    ]),
    node("voice-processing","CD-05","WF-08",voice("PROCESSING"),"Voice — Processing","Restricted deterministic six-intent mapping; no command is approximated.",[
      tap("Cancel","driving",25,545,307,66),
      simulate("Certain NEXT intent","voice-pending"),
      simulate("One artist candidate","voice-confirm-one"),
      simulate("Two artist candidates","voice-confirm-two"),
      simulate("Artist not found","voice-not-found"),
      simulate("Unsupported command","voice-unsupported"),
      simulate("Unknown phrase","voice-unknown")
    ]),
    node("voice-confirm-one","CD-09","WF-08",voice("CONFIRM_ONE"),"Voice — Confirming one candidate","Only immutable option 1 + Neither / Cancel, no new question.",[
      tap("Confirm option 1","voice-pending",30,370,301,62),
      tap("Neither → manual","driving",30,497,301,62),
      tap("Cancel","driving",30,560,301,62)
    ]),
    node("voice-confirm-two","CD-10","WF-08",voice("CONFIRM_TWO"),"Voice — Confirming two candidates","Exactly two immutable labels. No second confirmation.",[
      tap("Confirm option 1","voice-pending",30,370,301,62),
      tap("Confirm option 2","voice-pending",30,427,301,62),
      tap("Neither → manual","driving",30,497,301,62),
      tap("Cancel","driving",30,560,301,62)
    ]),
    node("voice-pending","CD-06","WF-08",voice("COMMAND_PENDING"),"Voice — awaiting playback ack","Command success follows only service acknowledgement.",[
      tap("Cancel and discard stale callbacks","driving",27,549,304,61),
      simulate("Service acknowledges exactly one command","voice-done"),
      simulate("Service rejects execution","voice-rejected")
    ]),
    node("voice-done","CD-07","WF-08",voice("EXECUTED"),"Voice — Executed","TTS <=1 sentence; a TTS failure cannot revoke acknowledged command success.",[
      tap("Dismiss to Driving snapshot","driving",26,542,306,75),
      simulate("TTS becomes unavailable after ack","voice-tts")
    ]),
    node("voice-tts","CD-07","WF-08",voice("TTS_UNAVAILABLE"),"Voice — successful, TTS unavailable","Same Executed surface and service state; optional spoken feedback fails non-fatally.",[
      tap("Dismiss; keep command success","driving",26,542,306,72)
    ]),
    node("voice-timeout","CD-17","WF-08",voice("STT_TIMEOUT"),"Voice — first STT timeout","Exactly one eligible Retry button. Manual fallback always available.",[
      tap("Retry once","voice-listen-two",28,478,303,66),
      tap("Manual controls","driving",28,545,303,70),
      tap("Exit Driving","player",275,26,81,68)
    ]),
    node("voice-terminal","CD-19","WF-08",voice("ATTEMPT2_TERMINAL"),"Voice — second failure terminal","Retry is exhausted. Third capture is prohibited within this session.",[
      tap("Manual controls","driving",28,545,303,70),
      tap("Exit Driving","player",275,26,81,68)
    ]),
    node("voice-not-found","CD-13","WF-08",voice("ARTIST_NOT_FOUND"),"Voice — artist not found","No best-guess artist, no ineligible STT Retry.",[
      tap("Manual controls","driving",28,544,303,73),
      tap("Exit Driving","player",275,26,81,68)
    ]),
    node("voice-unsupported","CD-15","WF-08",voice("UNSUPPORTED"),"Voice — unsupported command","Unsupported intents never execute by approximation.",[
      tap("Manual controls","driving",28,544,303,73)
    ]),
    node("voice-unknown","CD-14","WF-08",voice("UNKNOWN_PHRASE"),"Voice — unknown utterance","Do not invent a missing intent or an extra interview.",[
      tap("Manual controls","driving",28,544,303,73)
    ]),
    node("voice-provider","CD-22","WF-08",voice("PROVIDER_UNAVAILABLE"),"Voice — provider unavailable","Manual only. No forced Settings/cloud consent inside Driving.",[
      tap("Manual controls","driving",28,544,303,73),
      tap("Exit Driving","player",275,26,81,68)
    ]),
    node("voice-permission","CD-21","WF-08",voice("PERMISSION_DENIED"),"Voice — microphone access denied","No simulated Android mic dialog inside active Driving.",[
      tap("Manual controls","driving",28,544,303,73),
      tap("Exit Driving","player",275,26,81,68)
    ]),
    node("voice-rejected","CD-08","WF-08",voice("EXECUTION_REJECTED"),"Voice — command rejected","No false Executed, no ineligible Retry.",[
      tap("Manual controls","driving",28,544,303,73)
    ]),
    node("settings","CE-15","WF-10",state("10","CE-15"),"Settings / Privacy","Existing non-driving privacy and permission surfaces; no cloud STT assumed enabled.",[
      tap("Back to Library","library",1,31,61,59),
      simulate("Read privacy disclosure","privacy"),
      simulate("Existing explicit Android settings handoff","sys-settings")
    ]),
    node("privacy","CE-21","WF-10",state("10","CE-21"),"Privacy details","Raw audio not stored by default. Consent must remain explicit and outside Driving.",[
      tap("Back to Settings","settings",0,29,66,60),
      simulate("See optional consent decision","consent")
    ]),
    node("consent","CE-22","WF-10",state("10","CE-22"),"Optional cloud voice consent","Provider-specific remote STT stays disabled until separate legal/privacy acceptance.",[
      tap("Not now","settings",27,579,144,65),
      simulate("Return to privacy details","privacy")
    ]),
    boundary("route-event","CE-07","Route / focus Android event","Phone, call, headset and route callbacks belong to Android + Playback Service.",[
      system("Wired/Bluetooth disconnect → immediate safe pause","route-paused"),
      system("Transient focus loss → policy pause","focus-paused"),
      system("Permanent focus loss → safe paused","safe-paused")
    ],"Android/System + Playback Service"),
    node("route-paused","CE-06","WF-06",frame("06","FULL_PLAYER_PAUSED"),"Route disconnected: Safe Paused","No audio leaks to speaker and no automatic resume on reconnect.",[
      simulate("External route reconnects","route-return"),
      simulate("Route stays unavailable","route-paused"),
      simulate("Return to Library","library")
    ]),
    boundary("route-return","CE-09","Android route-reconnect handoff","Platform owns the reconnection event. It must not auto-resume service playback.",[
      system("Route valid; return to app Safe Paused","safe-paused"),
      system("Route disconnects again","route-paused")
    ],"Android/System + Playback Service"),
    node("focus-paused","CE-03","WF-06",frame("06","FULL_PLAYER_PAUSED"),"Transient focus/call interruption","Service policy-owned pause; guard evaluation must use real state.",[
      simulate("All four resume guards pass","player"),
      simulate("A resume guard fails: stay Safe Paused","safe-paused")
    ]),
    node("safe-paused","CE-06","WF-06",frame("06","FULL_PLAYER_PAUSED"),"Safe Paused — guard failed","Manual playback survives, but unsafe playback is never auto-resumed.",[
      tap("Explicit Play on revalidated safe route","manual-resume",125,532,110,73),
      simulate("Return to Library","library")
    ]),
    node("manual-resume","CE-06","WF-06",frame("06","FULL_PLAYER_PAUSED"),"Explicit user Resume pending","Request Play only on valid route; awaiting service acknowledgement.",[
      simulate("Service confirms playback on safe route","player"),
      simulate("Still invalid/rejected; remain Paused","safe-paused")
    ])
  ];
  const scenarios = {
    "GS-01":{title:"First launch → Ready Library",start:"splash",success:"library",source:"GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md",
      primary:["splash","welcome","permission","sys-media","granted","scanning","library"],
      recoveries:["migration-error","denied","permanent","sys-settings","empty","scan-error"]},
    "GS-02":{title:"Find local music → Playback",start:"library",success:"player",source:"GS-02_FIND_MUSIC_AND_START_PLAYBACK.md",
      primary:["library","search","results","selection","player"],
      recoveries:["search-none","search-error","stale","corrupt","reconnect"]},
    "GS-03":{title:"Background / system controls",start:"player",success:"player",source:"GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md",
      primary:["player","background","sys-controls","sys-command","reconnect","player"],
      recoveries:["notification-denied","sys-settings-notification","restore-paused","library"]},
    "GS-04":{title:"Driving → direct Voice NEXT",start:"player",success:"driving",source:"GS-04_DRIVE_AND_EXECUTE_DIRECT_VOICE.md",
      primary:["player","driving","voice-listen","voice-processing","voice-pending","voice-done","driving"],
      recoveries:["driving-paused","driving-none","driving-last","driving-safe","voice-confirm-one","voice-confirm-two","voice-timeout","voice-listen-two","voice-terminal","voice-not-found","voice-unsupported","voice-unknown","voice-provider","voice-permission","voice-rejected","voice-tts"]},
    "GS-05":{title:"Interruption → Safe recovery",start:"player",success:"player",source:"GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md",
      primary:["player","route-event","route-paused","route-return","safe-paused","manual-resume","player"],
      recoveries:["focus-paused","safe-paused","route-paused","route-return"]}
  };
  const data={version:"1.0",issue:17,phase:8,viewport:[360,800],archiveSha256:"0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8",nodes,scenarios};
  root.MUSICO_ROUTES=data;
  if (typeof module!=="undefined" && module.exports) module.exports=data;
})(typeof window!=="undefined" ? window : globalThis);
