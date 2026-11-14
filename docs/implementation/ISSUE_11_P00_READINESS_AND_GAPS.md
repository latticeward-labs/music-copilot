# Issue #11 — P00 Source Readiness & Gap Audit

**Date:** 2026-11-14  
**Phase:** P00 — references, dependencies, source inventory, scope freeze, implementation gaps  
**Phase status:** `SOURCE_AUDIT_COMPLETE / NO_BUILD / NO_TEST`. This is **not** feature implementation, runtime verification, or release approval.  
**Repository:** [latticeward-labs/music-copilot](https://github.com/latticeward-labs/music-copilot)  
**Issue:** [#11 — PLAY_ARTIST and one-step ambiguity](https://github.com/latticeward-labs/music-copilot/issues/11)  
**Existing work branch:** `feature/11-play-artist-one-step-ambiguity`  
**Baseline main at P00 inspection:** `00ad93363cf773e0afb21953c7a7d324fc1c0404`  
**Branch at P00 entry:** `9ce7ee71ab3fb7aee005954504959048200cf871`; compared with main: **ahead 2 / behind 0**, and the only existing file difference was the [Persian 14-phase plan](ISSUE_11_PHASED_EXECUTION_PLAN_FA.md), not production-code changes.  

## 1. Inputs and authority reconciled

The owner-attached `MSM2(1)(1).rar` was **opened and all 12 PDF documents enumerated and text-reviewed**, not inferred from its filename. Archive SHA-256: **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, identical to the prior approved [Source and Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md).

Archive inventory (12 documents):
1. `ADR-0001_Native_Android_Platform_Revised.pdf`
2. `ADR-0002_Service_Owned_Media3_Playback.pdf`
3. `ADR-0003_Local_First_Data_Boundaries_Revised.pdf`
4. `ADR-0004_Replaceable_Voice_Provider.pdf`
5. `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf`
6. `ADR-0006_No_Backend_Account_or_Streaming.pdf`
7. `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`
8. `Music_Copilot_MVP_v1_EN.pdf`
9. `Music_Copilot_PRD_v1_EN_Clarified.pdf`
10. `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`
11. `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
12. `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf`

Relevant attached-source constraints: ADR-0005 ``3–11, 16–17 defines exactly six deterministic English intents, ordered normalization, versioned artist matching, only one bounded confirmation and safe failure; ADR-0004 ``3–7 defines replaceable STT/TTS and coordinator-owned voice state; ADR-0003 defines MediaStore/Room local ownership; ADR-0002 requires service-owned Media3; ADR-0006 excludes backend/accounts/streaming. The technical blueprint's **Voice & Intent / Artist Resolution** sections align with those decisions. The attached ADR PDFs include historical proposed/approval placeholders; approved GitHub ADRs and Issue #16 supersede those **status** placeholders, without relaxing their applicable behavior rules.

Repository authority reviewed: [PRD](../PRD.md), [MVP](../MVP.md), [Voice Contract](../VOICE_CONTRACT.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [runtime ownership](../architecture/RUNTIME_POLICIES.md), [public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [privacy contract](../ANALYTICS_AND_PRIVACY.md), [Issue #11 acceptance](https://github.com/latticeward-labs/music-copilot/issues/11), and the [existing P00–P13 execution plan](ISSUE_11_PHASED_EXECUTION_PLAN_FA.md).

**Authority resolution:** approved current GitHub implementation/contracts control live implementation details and statuses; PDFs remain the owner-provided product/architecture reference. No new vocabulary, threshold, privacy event, or product feature is authorized by this audit.

## 2. Real GitHub dependency snapshot

| Dependency | Observed GitHub state | P00 disposition |
|---|---|---|
| [#6](https://github.com/latticeward-labs/music-copilot/issues/6) MediaStore/Room local library | **Closed**; current source has artist-group browse/query and revision-aware index | **Code seam exists.** Actual artist-specific Voice resolver must be added; closure alone is not proof of all technical QA |
| [#10](https://github.com/latticeward-labs/music-copilot/issues/10) PTT / five control intents | Implementation **closed and merged**, owner-authorized exception; current main contains Moonshine Tiny v2 + Silero VAD | **Usable implementation base, not a quality PASS** |
| [#16](https://github.com/latticeward-labs/music-copilot/issues/16) architecture / state ownership | **Closed / approved / merged** | Contracts authoritative; no second Player, provider-owned execution or changed persistence authority |
| [#17](https://github.com/latticeward-labs/music-copilot/issues/17) high-fidelity / Confirming design | **Closed** at AI-assisted design-artifact acceptance, human review intentionally deferred until post-MVP | Existing handoff available; do not claim physical/human UI verification |
| [#80](https://github.com/latticeward-labs/music-copilot/issues/80) P0 Voice quality / technical evidence | **OPEN**; #10 owner lab **24/50 = 48%**, gate **45/50 = 90%**, #10 technical T01–T10 **NOT_RUN** | **OPEN P0 release/quality risk.** Do not mark direct-command quality or release readiness passed |

Issue #11 itself is **OPEN** with 13 unchecked acceptance boxes; its legacy body contains `Backlog (blocked)` wording. That text is historical planning metadata, not an authoritative statement that its now-merged #6/#10 dependencies are still absent. **P00 readiness = qualified to proceed to source-only P01 while preserving #80; NOT ready for QA/field/beta release.**

## 3. Contract and behavior baseline (not newly implemented)

- Six and only six English intents: `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST`. The last is #11's new executable behavior; the other five must remain unchanged.
- Required approved example grammar: **“Play songs by [artist]”**. No additional spoken equivalents may be invented unless supported by the approved Voice Contract/phrase policy.
- Explicit Mic tap, **≤6 seconds Listening**, **at most one recognition retry**, replaceable STT/TTS, and **at most one short TTS sentence**. No wake-word, always-on mic, open-ended NLU/LLM dialogue, network catalog or streaming.
- Process: STT text + *real, optional* provider confidence → deterministic grammar/slot extraction → artist lookup → exact normalized match → bounded/versioned fuzzy candidates → direct/one confirmation/not found. Control intents are **not** fuzzy matched.
- Approved stable artist-candidate order: **exact normalized identity → descending fuzzy score → documented local history priority only as secondary tie-break → stable artist group/ID ascending**. Do not use SQLite scan/row order as a tie-break.
- Current [`VoicePolicy`](../../app/src/main/java/com/latticeward/musiccopilot/voice/policy/VoicePolicy.kt) declares direct provider threshold **0.90** (allowed range **0.80…0.98**), artist clear score **0.88**, artist plausible **0.72**, and minimum top-two separation **0.08**. These are distinct policy dimensions, **not a permission to manufacture or fuse provider confidence**.
- Exactly **one bounded confirmation** with an immutable **1–2 option** snapshot, one token and one user response. Invalid/repeated/stale/recreated confirmations must never dispatch twice. A second unclear answer fails safely; explicit Neither/Cancel must preserve prior playback.
- MediaStore permission and re-query are live-file truth; Room is a derived index. Service-owned `PlaybackGateway`/Media3 is the **single** queue/playback authority; report success only after acknowledged playback, not intent mapping or command submission.
- No local artist match / insufficient evidence / permission revoke / stale revision / playback failure → typed safe fallback, **no change to current queue or playback**.
- No raw voice, full transcript, private artist/media name, local file path, URI/MediaStore ID, confirmation token, precise queue position or other prohibited information in analytics/crash/GitHub evidence. Telemetry must use existing typed, non-authoritative allowlists only.

## 4. Source-level baseline and confirmed gaps

All items below are **static inspection findings** against current `main`; no source execution, compile, QA or device behavior has been verified.

| ID | Confirmed seam / gap | Exact implementation locations | Scheduled phase |
|---|---|---|---|
| G01 | Enum has reserved `PLAY_ARTIST`, but current five-control mapper rejects artist phrases and has no artist slot grammar. `VoiceIntentDecision.Direct` already has optional `artist` slot, unused for dispatch. | `voice/intent/RestrictedVoiceIntentMapper.kt`; `voice/intent/DirectControlPhrasePolicy.kt` | P01–P02 |
| G02 | Existing `resolveVoiceCandidates()` produces **song** candidates using `normalizedTitle` equality/substring and `normalizedArtist.contains`; similarity tiers 1.0 / .88 / .72. It materializes `projection.all()`, returns media refs, and is **not** a stable artist-group fuzzy resolver. Must not simply reuse as artist identity. | `data/api/LibraryRepository.kt`; `data/repository/RoomLibraryRepository.kt` | P03–P04 |
| G03 | **Reuse available:** `LibraryBrowseCategory.Artists`, `LibraryGroupId`, `artistGroupId`, `LibraryDao.artists()`, `artistSongs()`, `artistSongCount()`, index `revision`, expected-revision check on browse, and `LibraryRepository.get(ref)` live MediaStore re-query. Need an additive, bounded, permission/revision-aware artist Voice lookup and queue-source seam, without breaking Browse/Search. | `data/api/LibraryBrowseContracts.kt`; `data/room/LibraryDao.kt`; `data/room/LibraryRow.kt`; `data/room/LibraryProjection.kt`; `data/repository/RoomLibraryRepository.kt` | P03, P06 |
| G04 | Current library text normalization is locale-independent with display-vs-key separation; a versioned **artist-command** pipeline, bounded scoring, exact-vs-fuzzy separation, stable ID ordering and approved history tie-break semantics remain to be implemented. Never turn a missing metadata artist into a fake real artist. | `data/api/LibraryTextNormalization.kt`; `data/api/LibraryBrowseContracts.kt`; new pure artist resolver policy in Voice/Application boundary | P04 |
| G05 | `VoicePolicy` has numeric constants; `BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD` exists. **MoonshineSileroSpeechToTextEngine** explicitly returns `providerConfidence = null`. Five direct-control exact phrases already have a narrow no-calibrated-score fallback; it **must not be generalized to arbitrary artist names**. Direct artist dispatch requires a documented safe confidence decision; no fake score. | `voice/policy/VoicePolicy.kt`; `voice/intent/RestrictedVoiceIntentMapper.kt`; `voice/android/MoonshineSileroSpeechToTextEngine.kt`; `app/build.gradle.kts` | P05 |
| G06 | Existing `PlaybackCommand.ReplaceQueue` and `PlaybackGateway.execute` provide the service-owned queue dispatch/ACK seam, but `VoicePlaybackDispatcher.commandFor(PLAY_ARTIST)` returns `null`. Need artist queue preparation and single-command dispatch after admissible recognition or confirmation. | `playback/api/PlaybackCommand.kt`; `playback/api/PlaybackGateway.kt`; `voice/session/VoicePlaybackDispatcher.kt`; additive application use case | P06 |
| G07 | `VoiceSessionState.Confirming` already accepts 1–2 immutable-display options + `VoiceCandidateToken`. Actual `DefaultVoiceSessionCoordinator.confirm(token, optionIndex)` returns `VOICE_STALE_SESSION` unconditionally; `RequiresConfirmation` currently fails/manual. New one-use token/session/generation state and confirmation resolution must be single-owner, fail closed, and preserve existing five-command guards. | `voice/api/VoiceSessionCoordinator.kt`; `voice/session/DefaultVoiceSessionCoordinator.kt`; `voice/session/VoiceTransitionPolicy.kt` | P07 |
| G08 | Current projection maps `Confirming` to `ConfirmationUnavailable` and `VoiceSessionPanel` has no actual artist-choice buttons. Full Player/Driving share a single app coordinator. Existing UI must render option labels privately and pass only token+index to coordinator. | `core/application/voice/VoiceOverlayPresentationPolicy.kt`; `feature/voice/VoiceSessionPanel.kt`; `feature/player/FullPlayerScreen.kt`; `feature/driving/DrivingModeScreen.kt`; `app/navigation/AppNavHost.kt` | P08 |
| G09 | Existing English/Android feedback catalogs support short five-control outcomes, but require specific new confirm/not-found/cancel/artist playback-failure copy. Success requires actual Media3 ACK, TTS failure is non-authoritative. | `voice/feedback/VoiceFeedbackCatalog.kt`; `voice/android/AndroidVoiceFeedbackCatalog.kt`; resource `res/values/strings.xml` | P09 |
| G10 | Current coordinator has cancellation/generation and duck lease handling; #11 adds pending-confirm lifetime/races across Back, background, call, focus, route, permission and recreation. Must never persist pending authority or auto-replay after process loss. | `voice/session/DefaultVoiceSessionCoordinator.kt`; `core/application/voice/VoiceLifecycleSafetyPolicy.kt`; `app/MainActivity.kt`; `app/navigation/AppNavHost.kt` | P10 |
| G11 | Typed privacy gates/events exist, but #11 outcomes need explicit projection into existing allowlisted fields, with no candidate names, media refs or free-form diagnostics. No new external sink or provider allowed. | `quality/api/TelemetryEvent.kt`; `quality/privacy/AllowlistedTelemetryPrivacyGate.kt`; `core/application/voice/VoicePrivacyDiagnosticsPolicy.kt`; [privacy contract](../ANALYTICS_AND_PRIVACY.md) | P11 |
| G12 | Binding is `VoiceBindings` → Moonshine STT / offline TTS / application-scoped coordinator. Integrated wiring, QA fixture source and traceability still pending. | `app/di/VoiceBindings.kt`; `app/di/DataBindings.kt`; `app/di/PlaybackBindings.kt`; affected authored-only `app/src/test`/`androidTest` cases | P12 |

**Important non-equivalence:** artist look-up ≠ searching song titles. Historical play count/history can be used only if a genuine locally approved source/priority rule is available; never invent user history or persist new personal analytics merely to force a tie.

## 5. Frozen change envelope and forbidden changes

### Allowed for P01–P12, only as the named phase requires

- **Voice:** `app/src/main/java/com/latticeward/musiccopilot/voice/{intent,policy,session,api,feedback}/`; provider **interfaces** only if absolutely necessary and architecture-compatible; presentation projection under `core/application/voice/`.
- **Local data:** additive artist-resolution contract/query under `data/{api,repository,room}/`, using existing `LibraryGroupId` / revision / live-access checks, and isolated new pure resolver or application use-case types. Existing Search/Library UI behavior must remain backward-compatible.
- **Playback dispatch:** `voice/session/VoicePlaybackDispatcher.kt` plus **existing public** `playback/api/` contract only when necessary to pass service-owned `ReplaceQueue` / ACK. No direct ExoPlayer mutation.
- **UI:** `feature/voice/VoiceSessionPanel.kt`, Player/Driving **Voice callbacks only**, `core/application/voice/VoiceOverlayPresentationPolicy.kt`, narrowly necessary `app/navigation/AppNavHost.kt` / `MainActivity.kt`, localized strings and accessibility semantics.
- **Wiring/threshold/privacy:** relevant `app/di/`, `VoicePolicy.kt`, local BuildConfig fields in `app/build.gradle.kts` if required, existing typed quality allowlist; test *source* and `docs/implementation/ISSUE_11_*`.
- Any touched file must have a documented #11 behavioral reason; unrelated formatting/refactoring is excluded.

### Explicitly out of scope

- No edits to Moonshine/Silero STT engine/model files, audio capture/VAD thresholds, ABI, or provider selection simply to mask [#80](https://github.com/latticeward-labs/music-copilot/issues/80); its separate reliability fix remains tracked.
- No rework of the five #10 control phrases, the service-owned Media3 player/MediaSession/queue/focus/route internals, #8/#9 behavior, Room-wide schema/migrations, other screens, onboarding or app identity without demonstrable necessity and a recorded dependency decision.
- No streaming, account, backend, cloud STT, remote config service, network catalog, wake-word, LLM, multi-turn conversation, unrestricted artist scanning or new telemetry payloads.
- **P00–P12:** no Gradle, compile, lint, JVM/Compose/instrumentation tests, emulator/phone testing, Actions workflow invocation, app packaging, or APK. Authored, unexecuted test source is permitted for its later phases. **P13:** one cumulative build-only APK, owner-led manual tests, and separately evidenced optional technical QA if authorized.
- No PR, merge, issue closure, PASS checkmark or claims of independently observed tests in P00.

## 6. Acceptance/evidence baseline for P01 traceability

Every original Issue #11 acceptance criterion is **NOT_VERIFIED** today; P00 is a source audit only.

| AC | Owner Issue #11 expectation | P00 state | Delivery owners |
|---|---|---|---|
| AC01 | Approved PLAY_ARTIST phrase/approved equivalents | NOT_VERIFIED | P02 |
| AC02 | Normalization and bounded local fuzzy matching | NOT_VERIFIED | P03–P04 |
| AC03 | History used only per approved tie-break policy | NOT_VERIFIED | P04 |
| AC04 | Certain match executes through playback contract without new screen | NOT_VERIFIED | P05–P06 |
| AC05 | Low confidence/multiple plausible candidates → one confirm | NOT_VERIFIED | P05, P07–P08 |
| AC06 | No duplicates across recreation/events/rapid taps | NOT_VERIFIED | P07, P10 |
| AC07 | Not Found preserves playback and offers manual fallback | NOT_VERIFIED | P03, P09 |
| AC08 | Reject/cancel preserves playback | NOT_VERIFIED | P07, P10 |
| AC09 | Simple BuildConfig/remote-config threshold, no backend | NOT_VERIFIED | P05 |
| AC10 | Private coarse telemetry only | NOT_VERIFIED | P11 |
| AC11 | Unit tests: normalization/ties/threshold/similar/not-found | NOT_RUN | Authored P04/P05/P12; executed evidence P13 only if authorized |
| AC12 | Integration tests: Confirming / playback dispatch | NOT_RUN | Authored P07/P12; executed evidence P13 only if authorized |
| AC13 | Required direct-command lab quality | NOT_RUN for #11 | P13 owner 6×10 trials; #80 remains OPEN |

The current issue's original 13 checkboxes stay unchecked. Do not conflate authoring tests with executing them or issue closure with successful recognition.

## 7. Risks, open questions, decisions deliberately deferred

1. **R-P0-01 / Open #80:** #10 direct-voice quality is 48% on reported 50 attempts. #11 work may proceed on source; **beta/release and total six-command quality cannot be approved** without genuine follow-up evidence. Preserve five-control behavior.
2. **R-P0-02 / Moonshine confidence absent:** provider returns `null` confidence. P05 must define safe behavior in alignment with ADR-0005; **no fake numeric score** or implicit adoption of #10's limited exact control phrase exception for arbitrary artists.
3. **R-P0-03 / history tie-break authority:** confirm that any local history signal is actually implemented, privacy-allowed and stable; otherwise use stable score/ID ordering and **confirm close results**, rather than invent preference.
4. **R-P0-04 / index revision and access:** selection must be tied to a valid snapshot; permission revocation, stale revision, media removal or controller disconnection must fail safely before queue mutation.
5. **R-P1-05 / fuzzy calibration:** the repo's numeric artist thresholds are constants, **not tested score calibration**. P04/P05 must version a reproducible normalized-scoring definition and distinguish exact vs ambiguous vs not found. No threshold or algorithm silently changed.
6. **R-P1-06 / UI and lifecycle:** approved `Confirming` option contract is present but UX implementation is missing; one response, no double dispatch, focus restoration and privacy-safe display must be source-wired.
7. **R-P1-07 / test evidence:** no Issue #11 runtime tests, APK or manual voice trials are available in P00. P13's plan requires M01–M48, T01–T10 and L01–L06 (6×10 owner-trial matrix); all start `NOT_RUN`.

## 8. P00 exit gate and next handoff

| Exit criterion | P00 result |
|---|---|
| Attached original 12-PDF source inspected, SHA matched to approved source register | **SOURCE_CHECK_COMPLETE** |
| #11, #6, #10, #16, #17, #80 and current repository/branch examined | **SOURCE_CHECK_COMPLETE** |
| Architecture/product/privacy guardrails documented without unauthorized expansion | **DOCUMENTED** |
| Existing implementations and missing #11 behavior mapped to files and phases | **DOCUMENTED** |
| No critical dependency unaccounted for (including unresolved #80) | **ACCOUNTED_FOR; risk OPEN** |
| Tests/build/APK/CI invoked | **NONE** |
| New product code changed in P00 | **NONE** |
| Next permitted step | **P01 — typed product/result/confirmation contracts + state/result matrix, source only** |

**Disposition:** **P00 complete as a static source/readiness audit.** P01 implementation planning can start on the existing branch **with explicit #80 quality debt and P05 confidence-policy decision still open**. This record is not QA PASS, release readiness, or permission to merge/close Issue #11.

---
Evidence links: [#11](https://github.com/latticeward-labs/music-copilot/issues/11) · [#80](https://github.com/latticeward-labs/music-copilot/issues/80) · [#10 owner QA exception](ISSUE_10_OWNER_MOONSHINE_QA_ACCEPTANCE_2026-11-14.md) · [P00–P13 plan](ISSUE_11_PHASED_EXECUTION_PLAN_FA.md) · [approved voice state](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt).
