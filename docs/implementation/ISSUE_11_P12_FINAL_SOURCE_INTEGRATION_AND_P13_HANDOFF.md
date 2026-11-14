# Issue #11 — P12 Final Source Integration, Compatibility & P13 Handoff

**2026-11-14 | feature/11-play-artist-one-step-ambiguity**  
**Status: SOURCE_INTEGRATED_UNCOMPILED_UNTESTED; no build/APK.** No tests (JVM, unit, Android, Compose, device), Gradle, lint, Actions workflow, PR/merge, Issue close or owner acceptance were executed in P12.

## 1. Source authority and exact scope

- The attached \`MSM2(1)(1).rar\` was reopened, RAR5 SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`. Twelve PDF sources were extracted and relevant pages inspected, especially ADR-0002 (service-owned Media3 and route/focus safety), ADR-0003 (MediaStore source-of-truth and local-first), ADR-0004 (replaceable local PTT/TTS), ADR-0005 (six intents, deterministic bounded artist matching, single-step confirmation and privacy), ADR-0006 (no backend/streaming), PRD/MVP English and the System Architecture technical port layout.
- GitHub Issue #11's **13 acceptance criteria** and the same-branch \`docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md\` P12/P13 are controlling for implementation and device QA. The attached PDFs prescribe product boundaries; exact implementation thresholds, supplementary test identifiers and the P13 GitHub workflow are project engineering decisions and are not presented as PDF mandates.
- **Voice Contract wording**: \`docs/VOICE_CONTRACT.md\` explicitly approves \`Play songs by [artist]\`, optionally \`musico\` after an explicit Mic tap. No additional artist paraphrases are approved. Issue #11's AC01 refers to "supported equivalents"; P12 correctly treats this as only the already approved optional prefix/case/punctuation/normalization variants. Do not invent new voice synonyms to satisfy an unqualified AC phrase.

## 2. Actual live source chain inspected

1. **Production composition root:** \`MusicCopilotApplication\` creates exactly one \`ApplicationBindings\`. \`ApplicationBindings\` obtains \`RoomLibraryRepository\` via \`DataBindings\`, obtains the **single Media3 controller-backed** \`PlaybackGateway\` via \`PlaybackBindings.controller\`, and installs one \`DefaultVoiceSessionCoordinator\` through \`VoiceBindings.coordinator\`. The local Moonshine+Silero STT and Android offline TTS are selected from that binding. No extra player, backend or cloud STT is introduced.
2. **P02 / Issue #10 isolation:** \`PlayArtistPhrasePolicy.classify\` admits only the bounded approved "Play songs by" form. The five earlier control intents continue through their existing deterministic \`RestrictedVoiceIntentMapper\` and \`VoicePlaybackDispatcher\`. A new artist request cannot enter the old title-search voice-candidate API.
3. **P03/P04:** \`RoomLibraryRepository.lookupArtistGroups\` uses the dedicated Room artist identity projection and source permission epoch/revision guards. \`ArtistResolver\` consumes revision-pinned bounded exact and candidate pages, scores both query and label with the same versioned normalization/scorer, and fails safe for missing/incomplete/stale/over-5k artist indexes. Actual group identity, not song titles or synthetic "Unknown Artist", is resolved. Optional local history priority is *secondary only*; production currently uses \`NoArtistHistoryPriority\` (no unapproved playback-history extraction).
4. **P05:** \`ArtistDecisionPolicy\` uses the **separate** bounded \`BuildConfig.VOICE_ARTIST_DIRECT_CONFIDENCE_THRESHOLD\`. No score synthesis: a \`null\` Moonshine provider confidence yields a one-candidate/two-candidate Confirming even for a clear local match. High calibrated confidence plus an unambiguous artist is needed for Direct.
5. **P06 MediaStore/Media3:** \`PlayArtistPlaybackUseCase\` rechecks the selected artist's revision and membership, pages only this artist's opaque ref list, re-queries live MediaStore per ref, validates the bounded queue and then submits at most one \`PlaybackCommand.ReplaceQueue\`. \`PlaybackControllerConnection\` checks the service-owned, command-ID-correlated ACK; use case validates changed queue revision/leading ref/current index/state. Neither a Resolve decision nor a UI tap is a Playback ACK. It cannot unsend a command already accepted by Media3 after an asynchronous route/permission cancellation; P13 must verify actual service behavior.
6. **P07/P08 one-step UX:** One application-scoped \`DefaultVoiceSessionCoordinator\` owns the ephemeral, one-use token and bounded options. The shared \`VoiceSessionPanel\` is rendered inside **existing** Full Player and Driving screens; both event-time callbacks validate the active origin/token. Neither, Cancel, Back, two rapid taps, timeout, permission loss and Activity recreation cannot silently create another STT session or repeat the same choice.
7. **P09/P10 Voice lifecycle:** A short generic English clarification is optional and never blocks the UI; "Playing" is reserved for a valid P06 service ACK. The P10 gate rechecks current generation, foreground and a fresh OS RECORD_AUDIO grant after suspend points, with cancellation of STT/artist prompt/TTS, current token and duck lease. The original Media3 service retains focus/Bluetooth route safety; no duplicate AudioFocus owner.
8. **P11 privacy:** Typed five-field \`VoiceIntentOutcome\` data is constrained by an existing privacy allowlist, and production still uses \`NoOpTelemetrySink\`: no telemetry recording/export/upload, session token, transcript, filename or artist name. Quality failure is off critical path.

## 3. Concrete P12 source fixes/preparations

### A. Move bounded fuzzy artist CPU work off Compose/Main
- \`DefaultVoiceSessionCoordinator.handleArtistRecognition\` now wraps the potentially 5,000-artist fuzzy resolver in \`withContext(Dispatchers.Default) { resolver.resolve(recognized.slot) }\`.
- The P10 generation/permission gate still runs **after** the suspended resolver returns; a stale result cannot dispatch a new queue.
- **Important:** Media3 controller calls remain on their original application main dispatcher. They were intentionally NOT moved to Default because Media3 APIs can require their registered application Looper.
- This is a **source-level performance/safety improvement**, not measured p95 or a verified timing fix.

### B. Update outdated provisional source comments
- Remove references claiming the P04 resolver/P06 playback use case are still unwired after the P07 injection; clarify P12 verified the composition path in source. No matching, queue or confidence policies changed.

### C. Add strictly P13-triggered build-only workflow (NO P12 execution)
- Source file: \`.github/workflows/issue-11-p13-one-cumulative-apk.yml\`. It has **only** a push path trigger on \`.github/issue11-p13-apk-request.txt\`, restricted to \`feature/11-play-artist-one-step-ambiguity\`. **That request marker is absent in P12.**
- At **P13 only**, on explicit owner approval, a separate marker-file commit (without \`[skip ci]\` on that request) triggers one cumulative \`:app:assembleDebug\` on the request's **exact commit SHA**.
- The workflow installs Java 17 and Android platform 36, obtains the same pinned local Moonshine Tiny English asset, Silero VAD and Sherpa-ONNX 2.0.0 AAR as the existing Issue #10 Moonshine workflow, verifies the documented AAR SHA-256, records input checksums, builds for arm64 test device, and uploads APK, exact source commit, APK SHA-256, size and provenance. **No JVM, Android, Compose, lint, emulator or QA jobs are invoked.**
- Workflow availability, network downloads, model files, Android/Kotlin compilation and artifact production are **NOT_VERIFIED** until P13. If P13 build fails, only P13 can investigate/fix and rebuild. No APK URL or hash is fabricated now.
- This workflow is intentionally separate from Issue #10 P13's older build recipe, which does not fetch the newer Moonshine/Silero AAR and models.

## 4. Complete QA and acceptance mapping (source authoring only)

- \`Issue11Phase12CumulativeWiringContractTest.kt\`: **25 new JVM \`@Test\` methods** authored, covering composition root, exact six-intent grammar, CPU resolver dispatch, Room Artist not-title SQL, permission epoch, bounded fuzzy and tie behavior, actual calibrated confidence, distinct BuildConfig thresholds, MediaStore live recheck, Media3 correlated ACK, one-token confirmation, Player/Driving state & callbacks, voice lifecycle, strict local telemetry and marker-gated P13 workflow.
- Existing \`Issue11Phase2...\` through \`Issue11Phase11...\` source-authored fixtures remain present, including P03 **Android Room instrumentation** and P08 **JVM source/UI contract fixtures** (the latter are not misrepresented as instrumented Android tests). All remain **NOT_RUN**, not silently passed or rewritten. A GitHub source inventory identifies **17 Issue #11 test source files with 351 authored \`@Test\` methods** across P02–P12, counting both JVM and the 4 Android Room tests; **zero have run or passed**. Other Issue #10 integration and regression fixtures are retained.
- \`ISSUE_11_P12_P13_OWNER_PHONE_QA_CHECKLIST_FA.md\` contains the owner's **48** manual handset cases \`M01–M48\`, **10** specialized groups \`T01–T10\`, **six ten-attempt groups** \`L01–L06\` = 60 trials, with every row **NOT_RUN**. Recording instructions explicitly forbid private file/transcript data.
- \`ISSUE_11_P12_ACCEPTANCE_AND_QA_LEDGER.json\` traces all **13 GitHub acceptance criteria** to source paths and future phone/specialist/lab checks. Each is \`NOT_VERIFIED\` pending real P13 evidence. Source inspection alone is NOT a pass.

## 5. Open risks / nonnegotiable P13 gates

| ID | Residual risk / blocker | P13 response |
|---|---|---|
| R01 | Nothing in P00–P12 has been compiled, linted, tested or installed. Kotlin/Room/KAPT/Compose/native-link errors may exist. | Start with the **first cumulative build-only** attempt, record real failures and corrections only in P13 |
| R02 | Moonshine currently returns \`providerConfidence=null\`; M03 high-calibrated-confidence Direct cannot be confirmed through that provider alone. | Validate one-step confirmation; mark M03 BLOCKED unless an actually approved calibrated offline provider exists |
| R03 | Independent P0 [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) STT accuracy remains OPEN; historical Issue #10 owner lab 24/50 = 48% vs 45/50 = 90%. | Do not claim recognition regression PASS; new six-command 60-attempt gate at least 54/60, zero wrong automatic executions |
| R04 | Fuzzy scanning 5k artist groups can be costly even on Default dispatcher. P95 voice target <2.5s and UI responsiveness unmeasured. | Measure actual phone latency and p95 before acceptance |
| R05 | A service command already accepted by Media3 cannot be retroactively rolled back by canceling Voice, even if the late ACK is suppressed. | Race/route/focus physical QA; any wrong autoplay or speaker leakage = release stop |
| R06 | P13 build depends on pinned remote asset URLs/Android SDK and AAR checksum; releases may be inaccessible or updated. | Validate exact download/checksum/build in P13 and capture provenance. Do not fetch unpinned alternatives silently |
| R07 | No actual telemetry provider or consent for exporting private diagnostics. | Keep \`NoOpTelemetrySink\` in APK; verify Logcat/local privacy on device without sharing private text |
| R08 | "Supported equivalents" beyond Voice Contract are not approved. | M04 tests only the approved optional \`musico\` PTT prefix; don't expand grammar in P13 without product decision |
| R09 | Source-owned APK QA and permissions on owner phone cannot be inferred from emulator/provider fakes. | Owner returns M/T/L PASS/FAIL/BLOCKED with device/commit evidence |

## 6. Delivery-state boundary

- Main untouched by P12; feature branch deliberately unmerged. Issue #11 is OPEN and original 13 AC remain unchecked until evidence from P13.
- **Do not** treat source comments, synthetic fakes, test source or the prepared workflow as successful compilation/CI/hardware evidence.
- Next phase **P13 is the only place to create the marker, run first APK build, hand out actual downloadable APK/SHA and ask owner for phone results**. Then register real pass/fail evidence in GitHub and obtain owner approval **before PR, merge or closing #11**.
