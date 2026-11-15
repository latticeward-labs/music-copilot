# Issue #12 — P03 Event Schema v1.0 and Privacy Gate Implementation

**Date:** 2026-11-15  
**Status:** SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN. No test, compile, Gradle, CI, emulator or APK run.  
**Source:** Original user-uploaded MSM2(1)(1).rar, SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8, with all twelve English PDFs extracted and directly inspected.

## 1. Source authority and discrepancies

- Music_Copilot_PRD_v1_EN_Clarified.pdf, printed page 10: exactly **eight** required telemetry event families; no raw audio, data minimization, clear diagnostic history.
- Music_Copilot_MVP_v1_EN.pdf, printed page 10: allowed-properties table, consent, seven *listed* event rows, omitting intent_failed.
- Music_Copilot_System_Architecture_MVP_v1_EN.pdf, printed page 17: eight approved event families, explicitly including intent_failed; typed event → privacy gate → best-effort Quality adapter.
- ADR-0003, pp.2–4: telemetry non-authoritative; local-first data boundary; no audio, library, identity or location upload; consent and clear. ADR-0004/0005: replaceable STT, six deterministic intents, coarse metrics only. ADR-0006: no backend/account/sync.
- GitHub: docs/ANALYTICS_AND_PRIVACY.md, docs/implementation/ISSUE_12_SOURCE_AND_GAP_AUDIT.md, docs/implementation/ISSUE_12_METRIC_DICTIONARY.md, approved #16 architecture and Issue #12.

**D01:** MVP telemetry table lists seven rows, but PRD, Architecture and Issue #12 mandate eight. Keep intent_failed as the eighth, without falsely claiming the MVP table includes it.

**D02:** Issue #11 added an internal ninth typed event called voice_intent_outcome. It is NOT approved by the English product sources as an outbound family. P03 keeps its Kotlin type and Voice coordinator intact but explicitly **rejects** it at the common outbound AllowlistedTelemetryPrivacyGate, prior to any sink. Future P06 may safely project verified results into the original eight events, never double-counting or fabricating an ACK/correction.

## 2. Fixed outbound wire schema, names and fields

The new internal OutboundTelemetrySchemaV1 is a closed, ordered v1 encoding, with one event family followed by exactly the key-value fields below, in the given order. It does not introduce JSON maps, arbitrary properties or any network endpoint.

| Approved event | Exact outbound field names, in order | Authoritative future emission |
|---|---|---|
| driving_mode_started | entry_source, bt_connected | Confirmed Driving transition, P05 |
| voice_button_pressed | playback_state | Accepted explicit PTT, P05 |
| intent_detected | intent_type, confidence_bucket | Accepted deterministic intent, P06 |
| intent_failed | intent_type, failure_code, confidence_bucket, latency_bucket | One typed terminal Voice failure, P06 |
| command_executed | intent_type, latency_bucket | Service ACK-backed Voice execution, P06 |
| manual_correction | previous_intent, correction_type | Observed attributed correction, P06 |
| player_error | error_code, route_type | MediaSessionService error source, P07 |
| session_completed | duration_bucket, commands_count | Legitimate Driving-session completion, P08 |

**session_id:** MVP lists an ephemeral/coarse session_id for voice_button_pressed. P03 intentionally does not emit any session ID; a safe nonidentifying strategy remains subject to Privacy approval. Do not invent or export UUID, installation ID, hardware/device identifier, call/Bluetooth identity or a participant key.

The schema encoder requires an approved event name and exact property keys in exact order, permits ASCII A–Z/a–z/0–9/underscore tokens only, refuses empty/unknown/added/reordered keys, caps each token at 64 characters and caps the entire v1 envelope at 512 characters. It is an **internal serializer**, not a standalone externally callable validator. The authoritative AllowlistedTelemetryPrivacyGate performs all semantic value checks before calling it, and only the privacy-gate result may enter the sink.

## 3. Fixed value vocabulary and privacy policy

| Property | Existing accepted enum/code/bucket |
|---|---|
| entry_source | home, player, notification, settings (existing engineering allowlist) |
| bt_connected | true or false; no Bluetooth device identity |
| playback_state | Idle, Buffering, Playing, Paused, Ended, Error |
| intent_type, previous_intent | PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST |
| confidence_bucket | Low, Medium, High; none only in optional failure fields |
| latency_bucket | Fast, Expected, Slow; none only in optional failure fields |
| correction_type | manual_play, manual_pause, manual_skip, manual_search, manual_select |
| route_type | BuiltIn, Wired, Bluetooth, Unknown |
| duration_bucket | under_1m, 1_to_5m, 5_to_15m, over_15m |
| commands_count | integer from 0 through 1000 inclusive, engineering bound |
| failure_code | exact existing allowed typed AppErrorCodes only; never arbitrary provider or transcript text |
| error_code | exact existing allowed Player/Controller/Media/Route AppErrorCodes only |
| session_id | **not exported**, pending consent/Privacy representation review |

**Allowed IntentFailed.failure_code:** VOICE_UNAVAILABLE, VOICE_TIMEOUT, VOICE_NO_MATCH, VOICE_SECOND_ATTEMPT_FAILED, VOICE_STALE_SESSION, VOICE_ARTIST_NOT_FOUND, VOICE_ARTIST_UNSUPPORTED, VOICE_ARTIST_DECLINED, VOICE_ARTIST_STALE, VOICE_ARTIST_UNAVAILABLE, VOICE_ARTIST_PLAYBACK_FAILED, PLAYBACK_BUSY, PLAYBACK_REJECTED, PLAYBACK_FAILED, CONTROLLER_DISCONNECTED, CONTROLLER_CONNECT_FAILED, CONTROLLER_TIMEOUT, MEDIA_ITEM_MISSING, MEDIA_ITEM_CORRUPT, MEDIA_ITEM_UNPLAYABLE, PERMISSION_DENIED, PERMISSION_REVOKED, CONSENT_REQUIRED, FOCUS_NOT_RECOVERED, ROUTE_INVALIDATED, CALL_OR_ROUTE_SAFE_PAUSED.

**Allowed PlayerError.error_code:** PLAYBACK_BUSY, PLAYBACK_REJECTED, PLAYBACK_FAILED, CONTROLLER_DISCONNECTED, CONTROLLER_CONNECT_FAILED, CONTROLLER_TIMEOUT, MEDIA_ITEM_MISSING, MEDIA_ITEM_CORRUPT, MEDIA_ITEM_UNPLAYABLE, FOCUS_NOT_RECOVERED, ROUTE_INVALIDATED, CALL_OR_ROUTE_SAFE_PAUSED.

Every value is exact, case sensitive and fails closed if unknown. A provider-supplied exception, path, content URI, transcript, personal title, artist, token, device identity, precise location or arbitrary free-form payload cannot be copied into the outgoing event. Raw audio/transcription and crash data are never part of the schema.

**Important P95 distinction:** inherited VoiceIntentDiagnosticPolicy confidence thresholds (<0.60 Low, <0.90 Medium, >=0.90 High) and STT-attempt-to-terminal latency buckets (<=1000ms Fast; 1001–2500ms Expected; >2500ms Slow) are engineering choices from Issue #11. They cannot prove PRD's strict <2500ms **speech-end to perceptible response** P95. P10 implements the latter separately.

## 4. Actual source changes

| Repository file | P03 action |
|---|---|
| app/src/main/java/com/latticeward/musiccopilot/quality/privacy/OutboundTelemetrySchemaV1.kt | New eight-event, exact-key, versioned bounded outbound encoder |
| app/src/main/java/com/latticeward/musiccopilot/quality/privacy/AllowlistedTelemetryPrivacyGate.kt | Deny internal ninth family before any sink; use bounded schema encoder for original eight; preserve closed code checks and QUALITY_DROPPED behavior |
| app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt | Documentation correction: ninth typed outcome is internal-only, not outbound-authorized |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Phase9PrivacyQualityTest.kt | Author-only update: exactly eight externally allowed, while nine internal Kotlin types remain |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase11ArtistTelemetryPrivacyTest.kt | Author-only reconciliation of legacy ninth-export assertions; internal typed outcomes preserved |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase3TelemetrySchemaTest.kt | New authored test scenarios, all NOT_RUN: eight families, exact keys, ninth denied even with fake sink, bad fields, delimiter injection, count bound, omitted session ID and version |

**Deliberately untouched:** Voice coordinator, playback/Media3, routing and Audio Focus, artist mapper/STT, UI/Settings, Room/DataStore, Android Manifest, app Gradle and dependencies. Production QualityBindings continues to select NoOpTelemetrySink; no new SDK, endpoint, backend, account or crash uploader is installed.

## 5. Future-phase gates (no premature acceptance)

- **P04** must add independent, default-off consent for non-audio diagnostics, separate from microphone/cloud STT consent; sanitizing is NOT the same as obtaining user consent.
- **P05–P06** must add one authoritative emitter per event, deduplicate retries/recompositions, and use actual service ACK and observed correction; ninth-outcome projection remains future work.
- **P07–P09** must implement privacy-safe Player/crash/session lifecycle, bounded local collection, retention/erase contracts and any formally approved provider.
- **P10–P11** must implement real denominators and metric dashboard according to P02, never claim wrong-execution/retention/crash-free solely from the eight names.
- **P12–P13** must deliver Settings consent/clear and full source-authored QA. Tests currently present are NOT_RUN and not proof of Issue #12 AC10.
- **P14 FINAL ONLY** builds one integrated APK and obtains actual owner-phone evidence; PR/merge/close requires explicit approval. Open P0 quality follow-ups #80 (voice 24/50=48%, <90% laboratory gate) and #82 (artist ambiguity) remain independent blockers.

## 6. Phase exit (static source state, no testing)

- [x] Exact source RAR opened and authoritative PDF event table inspected.
- [x] Eight-family event schema and fixed property keys committed to the Issue #12 branch.
- [x] Ninth internal Issue #11 event prohibited at common outbound gate without changing Voice behavior.
- [x] Existing semantic value allowlists preserved; invalid keys, missing/reordered properties and injection-shaped fields rejected at schema boundary.
- [x] Source-authored QA fixtures reconciled to updated export contract, NOT_RUN.
- [x] No third-party SDK, analytics transport, playback/Voice feature change or network handling introduced.
- [ ] NOT COMPILED, NOT TESTED, NOT VERIFIED ON DEVICE.
- [ ] NOT approved by a real Product/Privacy/Legal reviewer; session_id strategy, retention, numeric bucket boundaries and any provider require review.
- [ ] No live event producers, consent, local telemetry store, crash reporter or dashboard yet.

**P03 result: SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN. All Issue #12 acceptance criteria remain NOT_VERIFIED pending real tests and later phase functionality. Issue stays OPEN; next is P04.**
