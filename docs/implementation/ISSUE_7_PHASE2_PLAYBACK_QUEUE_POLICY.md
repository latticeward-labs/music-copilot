# Issue #7 — P02: Playback Policy, Queue Semantics & Kotlin Contract Review

**Date:** 2026-10-22  
**Gate:** `P02_PASS_SCOPED_POLICY` = policy source + deterministic JUnit specifications + static/reproducible standalone Kotlin policy smoke. **Does not mean playable Media3, integrated device/JUnit Gradle test, or Issue acceptance criteria PASS.**  
**Repository:** `latticeward-labs/music-copilot` · **Branch:** `feature/7-media3-playback-queue-session`.  
**Dependency gates:** [P00 source audit](ISSUE_7_PHASE0_READINESS_AUDIT.md) PASS_AUDIT_ONLY; [P01 governance](ISSUE_7_PHASE1_EXECUTION_CONTRACT.md) PASS_CONTRACT_ONLY.

## 1. Audited source authority and resolved ambiguity

The user-attached **exact** `MSM2(1)(1).rar` (SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`) was opened as RAR5, all **12 English PDFs** decompressed successfully and verified against P00 per-PDF SHA-256. The relevant English **ADR-0002 PDF**, **PRD FR-02/03/08**, **MVP basic Player**, and **Technical Blueprint pp. 26+** were text-reviewed. The old blueprint shows a *provisional* `position > threshold` example and says the numeric threshold was not yet approved. The subsequently approved [UF-05](../user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) explicitly states **at/after** threshold restarts the current track; [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) and existing Kotlin resolve its numeric value at **3000 ms**.

**Resolved P02 decision:** at **2999ms** Previous moves back if a prior item exists; at **3000ms and later** Previous restarts the current item. At the first item Previous seeks current to **0** and never wraps, regardless of position. This aligns the newer approved user flow instead of copying the historical blueprint's provisional `>`. The preexisting `Phase11PlaybackPoliciesTest` assertions at 3000ms and first-item start were corrected. This is an intentional, documented source-conflict resolution; no Accepted ADR process/service topology is changed.

Other governing contracts: [UF-06](../user-flows/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md), [GS-03](../user-flows/golden-scenarios/GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md), [GS-05](../user-flows/golden-scenarios/GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md) and [#17 handoff](../handoff/SCREEN_SPECS.md).

## 2. Kotlin policy components delivered

| File | Actual outcome, without overstating |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackQueuePolicy.kt` | New **pure** `validate`, `retainPlayable`, nonwrapping `next`, typed structural refusal reasons. Only accepts a proposed value; never touches a real ExoPlayer/queue. |
| `app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackNavigationPolicy.kt` | Correct **>= 3000ms** Previous and first-item restart, `seekTo`, saturating `seekBy`, `seekBack` using the same shared invariant. |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue7Phase2PlaybackPolicyTest.kt` | 13 JUnit test methods for IDs, 5000 bounds, duplicates, filtering/reindexing, selected unavailable, invalid provider shape, Next/no wrap, Previous boundary, seek bounds/overflow/unknown duration. |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Phase11PlaybackPoliciesTest.kt` | Updated two older assertions to match approved UF-05 Previous semantics. No unrelated production code changed. |
| Existing `PlaybackCommand.kt`/`PlaybackWireProtocol.kt` | **UNCHANGED:** existing typed `ReplaceQueue` and transport version 1 already carry queue, index, position and play flag. New policy is compatible; **P03–P06 must wire it into the service**. |

## 3. Frozen queue/request semantics for P03/P04

1. **Authority and queue origin:** `MusicPlaybackService` remains the only live queue owner. Library, Search, Artist, Album and Folder supply the **ordered currently visible collection** and clicked occurrence index. Do not substitute only the clicked track, append to stale queue or re-sort invisibly. Existing #6 selection still submits only one track; that production handoff is **not yet fixed** (P04/P05/P07).
2. **Identity:** Every queue entry is an opaque `MediaRef` stable source reference; occurrence **index**, not unique MediaRef value, determines which duplicate occurrence was clicked. Duplicate occurrences are retained in their provided order; do not deduplicate. No paths/titles/URIs encoded into telemetry or runtime public errors.
3. **Structural validation:** `ReplaceQueue` needs `CommandId` nonblank ≤128 chars, 1–5000 entries, opaque nonblank `MediaRef` ≤128 chars each, `startIndex` within bounds, and nonnegative `startPositionMs`. `playWhenReady` is explicit. Empty request **rejects**, not silently clears, loops or autoplays. More than 5000 entries **reject**; do not partially enqueue an oversized request.
4. **Physical validation:** P02 structural ACCEPT **never** proves a valid local file. P03 independently rechecks OS media access and each MediaStore Content URI; a Room row or prefiltered UI list is not authority. `retainPlayable` accepts a positional Boolean vector **only from the service-side P03 verification**. Size mismatch fails closed; selected item unavailable fails closed with **old active queue untouched**. Unavailable other entries can be dropped without changing order, and selected index is recalculated by counting surviving entries before the requested position. P04 must implement atomic replace only **after** this validation; no optimistic ACK.
5. **Queue revision:** An actual accepted logical queue replacement/removal/index transition updates authoritative snapshots. Distinguish `PlaybackSnapshot.revision` (ordered service-state publication) from `queueRevision` (queue/timeline mutation). UI never writes these counters. P06 tests true monotonicity and avoids duplicate increments for the same semantic queue change; a failed request never claims accepted new queue.
6. **No unapproved editing:** No append, reorder, delete, save-playlist, shuffle, repeat, wrap, random track or autoplay-on-launch in MVP v1. `REPEAT_MODE_OFF`, `shuffleModeEnabled=false`, and unauthorized external toggle requests **must not mutate queue policy**.

## 4. Frozen transport/navigation/stop semantics

| Action/context | Deterministic decision | Responsibility |
|---|---|---|
| `Play` from a valid Paused item | Resume one existing service Player at confirmed position; repeated Play idempotent | P05 |
| `Play` with no current item | Typed refusal / truthful No Track; never fabricate a song | P04/P05 |
| `Play` after natural final-track completion | Restart retained final item from position 0 only after explicit Play | P04/P05 |
| `Pause` | Pause, preserve queue/current ref/index/position; repeated Pause idempotent | P05 |
| `Next` with middle valid index | Advance to next playable ordered occurrence; bounded corrupt skip | P04/P10 |
| `Next` at last index or natural last-item completion | `EndOfQueue`, retain selected last item, terminal Paused/Ended, **no wrap/repeat**. External Next and app Next use same policy | P04/P06 |
| `Next` without valid current item | `NoTrack`; typed reject/recovery, no synthetic player | P04/P05 |
| `Previous` at `0..2999ms` with preceding item | Advance backward once to previous valid occurrence (skip invalid bounded); new position 0 | P04/P10 |
| `Previous` at **≥3000ms** or on first item | Seek current valid item to **0**, no wrap; at first item this applies at any position | P04/P05/P06 |
| `SeekTo(position)` with verified finite duration and supported seek | Clamp into `[0,duration]`; negative UI input becomes 0; do not resume Paused media as side effect | P05 |
| Absolute seek with unknown/invalid duration or unsupported media | Return `null`/typed rejection, preserve verified state | P05 |
| `SeekBy(delta)` | Saturating signed addition (no Long overflow), clamp 0..known duration; without duration only backward/zero relative seeks permitted if seekable | P05 |
| `SeekBack` voice 10 seconds | `SeekBy(-10000)`, lower clamp at 0, never auto-play or move queue | P05/#11 later |
| `Stop` | Explicit stop is not Back/Home/Pause. Terminate audible playback without automatically clearing queue or deleting media; no implicit resume. Lifecycle shutdown only under ADR-0007 | P05/P08 |
| Queue-list item selection | Select requested **occurrence** in the current service queue, revalidate before switching and confirm state. P04/P06 must define service-internal operation or narrowly reviewed command extension; do not use a stale UI queue | P04/P06/P07 |

**P02 implementation note:** existing `PlaybackCommand` lacks a dedicated `SelectQueueIndex` command. P04/P06 must add a compatible, versioned command or restricted service command and tests, rather than misusing external `ReplaceQueue` to quietly truncate/re-sort the existing queue. No new public wire action has been added in P02.

## 5. Ordering, replays, Android controls and reconnect

- One service-owned `PlaybackCommandProcessor` with **32 waiting slots plus 1 in-flight**. No UI/Voice/system helper calls `ExoPlayer` directly. On overflow return typed `PLAYBACK_BUSY`; accepted commands complete with authoritative state or typed failure; nothing silently drops.
- `CommandId` is an idempotency key for an **action submission**, not a track ID. P04/P06 must coalesce same-ID/same-payload pending requests and reuse authoritative completion on retries; reusing an ID with different payload must reject. Use a bounded in-memory completed-ID ledger, no unbounded growth and no durable ID telemetry. Fresh user actions use fresh IDs. System-origin callbacks get service-generated IDs when routed into the common worker.
- Existing Media3 `Player` system controls currently mutate the ExoPlayer through the exposed session and **are not shown to pass the typed processor**. P06 **must address that integration gap** without event loops/deadlocks (e.g. reviewed interception and service-forwarding adapter); do not claim P02 already fixed it.
- Before any controller `Ready`, observe a **complete atomic** authoritative `PlaybackSnapshot`. On reconnect, the whole fresh snapshot wins, even if the session has a new revision sequence. A disconnected UI does not declare the old snapshot `Playing` success. Stale updates after reconnect are ignored; no screen-owned queue, no extra MediaSession.
- Persisted P1 queue is **not** replay protection or runtime authority. Cold restore remains **Safe-Paused** with MediaStore requery and zero autoplay.

## 6. Typed error, permission and privacy mapping for implementation

| Trigger | Machine-readable outcome | Recovery contract |
|---|---|---|
| Empty, invalid length/index/ID/negative queue starting position | `PLAYBACK_REJECTED` / `AppError.Playback` | Keep prior verified queue unchanged; user retries valid selection |
| Processor over capacity | `PLAYBACK_BUSY` / `AppError.Playback` | Retry deliberately, never invisible requeue |
| OS media access revoked | `PERMISSION_REVOKED` / `AppError.Permission` at application boundary; service fails safely with typed sanitized failure | No new playback, explicit Settings/reindex recovery |
| Selected local reference missing or moved | `MEDIA_ITEM_MISSING` / `AppError.MediaItem` | Refuse queue replacement, previous queue remains; reconcile |
| Unreadable/corrupt candidate | `MEDIA_ITEM_UNPLAYABLE` or `MEDIA_ITEM_CORRUPT` | Bounded skip of nonselected / active transition; fail selected closed |
| Player prepare/runtime decode failure | `PLAYBACK_FAILED` or typed media error | Bounded recovery, no false Playing |
| Controller disconnected/timeouts | `CONTROLLER_DISCONNECTED` / `CONTROLLER_TIMEOUT` | Whole snapshot reconnect/retry, no optimistic success |
| Audio route/focus invalidated | `ROUTE_INVALIDATED`, `FOCUS_NOT_RECOVERED`, `CALL_OR_ROUTE_SAFE_PAUSED` | Safe pause + manual resume guards only |

**Existing contract caveat:** the current `PlaybackSnapshot.lastError` allows only `MediaItem`, `Playback` and `Interruption`. Never jam `AppError.Permission` or `AppError.Controller` into that field. Preserve those errors on their correct public port/result boundaries, with explicit sanitized UI mapping. Wire protocol `PlaybackWireProtocol` has a closed allow-list; any necessary new mapping demands reviewed protocol tests in P03/P06. Telemetry contains at most approved coarse error code and route class, **never** user audio, titles, file paths, URIs, queue refs, positions or private command payloads.

## 7. Deterministic test specifications and actual evidence truth

Authored unit source: [`Issue7Phase2PlaybackPolicyTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue7Phase2PlaybackPolicyTest.kt) (**13 `@Test` methods**, not automatically executed). Updated historical 2 Previous assertions in [`Phase11PlaybackPoliciesTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase11PlaybackPoliciesTest.kt).

- **Reproducible source-controlled smoke added:** [`scripts/check_issue7_phase2_policy.sh`](../../scripts/check_issue7_phase2_policy.sh) plus [`scripts/issue7_phase2_policy_smoke.kt`](../../scripts/issue7_phase2_policy_smoke.kt) compile the actual project policy/command/ID sources via `kotlinc` and include **17 check assertions**. Invoke `bash scripts/check_issue7_phase2_policy.sh` on a checkout with Java/Kotlin installed. **This precise 17-check repository script was added, not executed in this turn, and its result is NOT_RUN.** The earlier local 14-check compatible-source standalone smoke is the only actually executed P02 smoke evidence; neither is an Android/JUnit Gradle run.
- **Local P02 policy-only smoke**: compiled pure Kotlin `PlaybackQueuePolicy` + `PlaybackNavigationPolicy` alongside compatible opaque ID/`ReplaceQueue` scaffolding with local `kotlinc`/Java 21; executed 14 explicit `check` assertions successfully. **Result: PASS (standalone policy smoke only)**. It is **not** a Gradle `:app:testDebugUnitTest` run, JUnit engine result, Media3/instrumentation run, or Android application build. The repeatable smoke inputs and expected checks are reflected in these Kotlin tests.
- **Native repository JUnit / Gradle:** **NOT_RUN** in P02: private GitHub source was edited through connector; no full Android build was obtained, no environment linked to its Gradle Android SDK/dependencies. The authored JUnit suite must execute at P12 (or permitted scoped technical run) before `T01/T02` PASS claims.
- **T01** remains `NOT_RUN` (queue engine in P04), **T02** remains `NOT_RUN` (full actual JUnit integration pending), **T03** remains `NOT_RUN` (P06 system-authority/idempotency parity pending). All `M01..M36` remain `NOT_RUN`. P02 is **policy-only**.
- Extend later unit/integration evidence for 32-slot races, duplicate in-flight request, conflicting CommandId, stale revision, exact external-system player parity, permission loss during validation, 5000-item timeout/performance, duration discovery and corrupt-file advancement.

## 8. P02 exit check and remaining implementation dependency

| P02 acceptance check | Recorded result |
|---|---|
| 12 PDF source and current accepted ADR/UF verified | PASS static review; archive SHA recorded |
| Queue identity/order/index/capacity and validation frozen | PASS pure Kotlin contract |
| Previous/Seek/Next/NoTrack/End policy reviewed and captured | PASS pure Kotlin policy and test source |
| 3000ms UF-vs-historical blueprint discrepancy reconciled | PASS and original test updated |
| No streaming, duplicate Player or UI-owned queue introduced | PASS path review of P02 diff |
| Command replay, external controls, reconnect, stop, focus/error rules documented | PASS **contract**, execution deferred to P03–P12 |
| 13 JUnit methods authored, no invented JUnit PASS | PASS code-authorship gate, native JUnit NOT_RUN |
| Local standalone pure Kotlin policy smoke | PASS **14 assertion checks only** |
| Real MediaStore resolver/real player/System Controls/owner APK | **NOT_IMPLEMENTED in P02; explicitly P03–P13** |

**P02 scoped decision:** **PASS_SCOPED_POLICY** after actual GitHub read-back confirms files, policy review and the evidence register. Next **P03: safe local-media URI resolver** using this validated queue request as an input. Do not mark any Issue #7 original AC checkbox PASS, build an owner APK, merge, close Issue or trigger GitHub Actions.
