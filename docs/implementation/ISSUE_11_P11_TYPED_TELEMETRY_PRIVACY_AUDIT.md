# Issue #11 — P11: Typed Privacy-Safe Voice Outcome Telemetry Audit

**Date:** 2026-11-14  
**Repository:** latticeward-labs/music-copilot  
**Branch:** feature/11-play-artist-one-step-ambiguity  
**Status:** SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN — source-only work; no tests, Gradle, lint, CI, APK, device verification, PR, merge or Issue close.

## 1. Verified source authority

Opened the **exact owner-uploaded** \`MSM2(1)(1).rar\`, SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`; reviewed the actual PDF text for these requirements:

- **ADR-0003, Local-First Data Boundaries (Revised), pp. 2–5:** telemetry is non-authoritative; raw voice, full transcripts, file titles, library metadata, precise location, contacts and unnecessary IDs are prohibited; only minimized, consent-governed diagnostic fields; quality-service failures must not block playback. App history/settings remain local; analytics is not a data store.
- **ADR-0004, Replaceable Voice Provider, p. 3:** allow only intent type, confidence/latency buckets and coarse outcome; do not store raw audio or files; voice provider cannot own playback.
- **ADR-0005, Restricted Deterministic Intent Mapping, p. 4 §12:** record supported intent, confidence bucket, latency bucket, outcome; no raw audio, entire recognized text, personal filenames or library contents; telemetry failure cannot alter command execution.
- **Music Copilot MVP v1 EN, p. 10 — Measurement & Dashboard Plan:** \`intent_detected(intent_type, confidence_bucket)\`, \`command_executed(intent_type, latency_bucket)\`, \`manual_correction(previous_intent, correction_type)\`, and the existing coarse quality event families; diagnostic data allowed **with consent**. The P11 typed outcome is an **additive engineering event**; the PDF does not specifically mandate an \`voice_intent_outcome\` event name, nor numeric bucket thresholds.
- **Music Copilot System Architecture MVP v1 EN, pp. 4, 7, 16–17:** strict typed-event → allowlist → nonblocking adapter; no arbitrary domain or media entities at the telemetry boundary.
- **Music Copilot User Flow Phase 1, p. 14:** telemetry excludes titles, artists, albums, folders, filenames, URIs/paths and library contents by default.
- Current GitHub \`docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md\` P11, Issue #11, preexisting Issue #16/Phase9 \`TelemetryEvent\`/AllowlistedTelemetryPrivacyGate/BestEffortQualityEventPublisher/NoOp sink, and previous Issue #10 privacy tests.

**Product/privacy caveat:** consent/retention provider signoff is not recorded. No analytics/crash upload provider or telemetry database is installed, and the production telemetry sink remains \`NoOpTelemetrySink\`; **no data leaves the device** via this P11 path. Enabling a non-no-op sink later is **not authorized** solely by this code; it requires explicit consent, retention and privacy/product approval.

## 2. P11 source changes (no unrelated features)

| Path | Exact change |
|---|---|
| \`quality/api/TelemetryEvent.kt\` | Add a **ninth additive** \`VoiceIntentOutcome\` event family with **exactly five schema fields**: \`intent_type\`, \`confidence_bucket\`, \`latency_bucket\`, \`result_code\`, \`correction_category\`. All values are closed Kotlin enums or optional enums mapped to \`none\`. Six supported intent names only; 11 fixed result classes and five safe manual correction categories. No free-form map, transcript, artist name, candidate array, media URI/ID or session token. Diagnostics \`toString()\` redacted. Manual category is allowed ONLY with \`MANUAL_CORRECTION\` result. |
| \`quality/privacy/AllowlistedTelemetryPrivacyGate.kt\` | Serialize only those five fixed \`VoiceIntentOutcome\` properties, with stable \`v1|voice_intent_outcome\` code. Also add the six exact Issue #11 artist failure codes to the EXISTING \`IntentFailed\` safe-code allowlist; arbitrary provider strings, appended metadata and file paths still fail closed. Original eight families remain unchanged. |
| \`quality/privacy/VoiceIntentDiagnosticPolicy.kt\` | NEW pure, versioned policy; valid finite provider confidence [0,1] → \`Low/Medium/High\`, null/NaN/invalid → \`none\`; nonnegative monotonic elapsed time → \`Fast/Expected/Slow\`, invalid → \`none\`. Maps only enumerated AppErrorCodes to fixed results. Manual correction only from explicitly observed UI category. |
| \`voice/session/DefaultVoiceSessionCoordinator.kt\` | App-scoped Voice marks an accepted \`PLAY_ARTIST\` attempt after P02 phrase admission and retains only a nullable confidence bucket + private monotonic attempt start. Before P06 ACK no \`EXECUTED\` outcome. On P06 ACK followed by Coordinator Executed publication, emits one typed \`EXECUTED\`. On typed failure, Neither, invalid choice, timeout or explicit token Cancel emits at most one sanitized terminal outcome. No runtime transcript/title/name/MediaRef/UUID/candidate/token passes into the callback. Cancel/permission/route/lifecycle invalidates the generation; no stale outcome is claimed afterward. Delivery is **best-effort asynchronously** on Default dispatcher, caught and dropped if transport fails; never awaited by voice/playback. |
| \`app/di/VoiceBindings.kt\` and \`app/di/ApplicationBindings.kt\` | Pass the narrowly typed callback through the EXISTING application-scoped Voice owner to \`qualityEventPublisher.tryRecord\`. Actual production publisher uses the existing Gate and **NoOpTelemetrySink**, with no analytics transport, API key, cloud or persistence. Existing five control commands, app graph ownership and Media3 service untouched. |
| \`app/src/test/.../Phase9PrivacyQualityTest.kt\` | Source-only update of preexisting structural fixture: **nine** allowed event families (the original eight + P11 typed outcome). No test execution. |
| \`app/src/test/.../Issue11Phase11ArtistTelemetryPrivacyTest.kt\` | NEW 34 source-authored JVM test methods, all **NOT_RUN**. |
| This MD and matching P11 JSON QA register | Traceability, source hashes, NOT_RUN case inventory and P12/P13 gates. |

## 3. Exact P11 schema and thresholds

\`\`\`text
v1|voice_intent_outcome
  |intent_type=PLAY_ARTIST
  |confidence_bucket=none|Low|Medium|High
  |latency_bucket=none|Fast|Expected|Slow
  |result_code=EXECUTED|NOT_FOUND|UNSUPPORTED|DECLINED|STALE|
               UNAVAILABLE|PLAYBACK_FAILED|NO_MATCH|TIMEOUT|CANCELED|
               MANUAL_CORRECTION
  |correction_category=none|MANUAL_PLAY|MANUAL_PAUSE|MANUAL_SKIP|
                      MANUAL_SEARCH|MANUAL_SELECT
\`\`\`

Only actual code-chosen one value per field is transmitted; alternatives above describe the closed vocabulary.

**Implementation-defined bucketing v1:**
- \`confidence\`: finite [0.0, 0.60) → Low; [0.60, 0.90) → Medium; [0.90, 1.0] → High. Missing or invalid STT confidence → \`none\` (**Moonshine currently reports null**, never fabricated). These telemetry thresholds do not change P05 direct-play confidence policy.
- \`latency\`: monotonic nonnegative elapsed from the start of this STT attempt to terminal outcome: ≤1000ms Fast; 1001–2500ms Expected; >2500ms Slow. Negative/unknown/clock-overflow → \`none\`. No absolute timestamp, detailed duration, clock source, session identifier or persistent timer.
- \`result_code\`: stable typed status. Unknown arbitrary provider error code is **not** transformed into an allowed personal-data-carrying string.
- \`correction_category\`: explicitly observed manual action only. \`Neither\`/Cancel/retry is **not** silently treated as manual correction. P11 provides the strict type factory, not a new UI event producer.

**Payload example for missing provider confidence and local NotFound (synthetic):**
\`\`\`text
v1|voice_intent_outcome|intent_type=PLAY_ARTIST|confidence_bucket=none|latency_bucket=Expected|result_code=NOT_FOUND|correction_category=none
\`\`\`

This example is NOT a recorded user's data. No data is actually persisted or emitted from the production NoOp sink.

## 4. Privacy boundaries and response safety

1. **Raw voice & recognized transcript:** STT remains in-memory for deterministic intent parsing; \`SpeechRecognitionResult.toString()\` already redacts raw transcript. No audio bytes or transcript are forwarded through telemetry or included in this event.
2. **Personal metadata:** artist names, P08 option labels, search text, titles, album, filename, storage path, URI/MediaRef, derived group IDs, local history, Bluetooth MAC address and token remain outside event types and allowlist output.
3. **Stable diagnostics only:** event family, supported intent enum, nullable confidence/latency buckets, typed outcome and observed category. \`toString()\` of the outcome is redacted.
4. **Cardinality:** per \`PLAY_ARTIST\` attempt, \`diagnosticClaimed\` is atomically set under Coordinator gate; only one outcome is queued. A new explicit retry is a new attempt. A mere pending confirmation emits no \`EXECUTED\`; the first valid P06 ACK + Coordinator terminal state is required. No raw session ID is emitted to deduplicate.
5. **Operational isolation:** \`tryRecordVoiceOutcome\` is an optional typed callback, dispatched off the critical Voice/Media3 thread and failures contained. A dropped quality event cannot alter audio focus, playback ACK, queue, UI, permission state, TTS, confirmation or retry.
6. **No remote behavior:** \`QualityBindings.sink()\` still returns \`NoOpTelemetrySink\`. AndroidManifest has no \`android.permission.INTERNET\` or automatic app-data backup; no cloud STT/analytics/crash sender is introduced by P11.
7. **Existing eight event families:** older \`IntentDetected\`, \`IntentFailed\`, \`CommandExecuted\`, \`ManualCorrection\`, etc., continue their original P9 strict checks. The source-authored Phase9 structural test was updated for the explicitly additive ninth family, **without being run**.

## 5. Source-only QA: NOT_RUN

**34 new JVM test methods** in \`Issue11Phase11ArtistTelemetryPrivacyTest.kt\`: exact six-intent/11-outcome/five-correction vocabularies, null/NaN/out-of-range provider scores, latency edges, error allowlist, injection attempts with fake transcript/path/URI/token, sanitized wire structure, excluded raw metadata, manual correction consistency, redacted diagnostics, privacy gate and sink exception isolation, ACK-only success provenance and app-scoped existing NoOp wiring.

**Original Phase9PrivacyQualityTest** has a source-only schema-structural test update (8 → 9 event families). It is **not counted as a new P11 test** and has not been executed.

**Execution:** Gradle NOT_RUN, compilation NOT_RUN, JVM/Android tests NOT_RUN, lint NOT_RUN, GitHub Actions NOT_RUN, emulator NOT_RUN, handset NOT_RUN, APK NOT_CREATED. Source inspection is not an execution result. All 13 original Issue #11 acceptance criteria remain NOT_VERIFIED; M01–M48/T01–T10/L01–L06 remain NOT_RUN.

## 6. P12/P13 handoff

- **P12:** verify full injection/coordination across P00–P11, author integration scenarios for double-confirmation, source/route changes and late ACK telemetry without executing them. Ensure privacy/consent UI documentation stays honest: local sink currently drops all diagnostics. Prepare complete Persian phone QA and acceptance ledger.
- **P13 ONLY:** first cumulative APK build, compile/test findings and owner-performed on-device checks for privacy/logcat/crash report/Android network/backup, no metadata leakage, real TTS/Focus/BT, and optional sink failures. Report actual PASS/FAIL evidence, not assumptions.
- **No PR/merge/Issue close until owner's approval after P13.**

**Independent debt:** Issue #80 voice recognition reliability remains OPEN (previous owner-reported 24/50=48% vs 90% target). P11 changes neither Moonshine recognition nor its observed accuracy.
