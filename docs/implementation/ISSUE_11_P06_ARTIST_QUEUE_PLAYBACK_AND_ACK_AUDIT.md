# Issue #11 — P06: Verified Local Artist Queue and Service-owned Playback ACK

**Date:** 2026-11-14  
**Branch:** \`feature/11-play-artist-one-step-ambiguity\`  
**Source implementation status:** \`P06_SOURCE_IMPLEMENTED_UNWIRED_UNBUILT\`  
**Test/Build status:** \`NOT_RUN\`; no Gradle, Kotlin/JVM/Android compilation, KAPT, lint, CI, GitHub Actions, emulator, real device, Media3 runtime or APK. The 25 new tests are **source-authored only**. No PR/merge/Issue closure or PASS claims.

## 1. Authoritative source and repo audit

**Exact owner-provided \`MSM2(1)(1).rar\` archive:** SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`, containing 12 English PDFs. The supplied package was verified again and the following **actual extracted contents** inspected, not substituted with a memory-only description:

- \`ADR-0002_Service_Owned_Media3_Playback.pdf\`: §§3–6, §9, §12 — *one MediaSessionService-owned ExoPlayer/MediaSession and queue*, all UI/Voice dispatches use MediaController/gateway, real media-file checks, Audio Focus/call/headset safety, no optimistic duplicate Player or success.
- \`ADR-0003_Local_First_Data_Boundaries_Revised.pdf\`: §§3–6 and §10 — local MediaStore **existence source of truth**, Room derived/rebuildable index, permission denial/revocation and missing-file behavior, no backend/account/cloud transfer.
- \`ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf\`: §§3, 7–10 — validated PLAY_ARTIST slot and confidence, no wrong auto-execution, one confirmation only, typed safe failures, deterministic artist resolution.
- English \`Music_Copilot_PRD_v1_EN_Clarified.pdf\`, \`Music_Copilot_MVP_v1_EN.pdf\` and technical blueprint: offline local player, low-confidence confirmation, Not Found/manual fallback, safety over incorrect playback.

Current GitHub code was also fetched and examined: [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11); [P00–P13 plan](ISSUE_11_PHASED_EXECUTION_PLAN_FA.md); [P01 typed decisions](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/PlayArtistContracts.kt); [P03 typed group/member refs](../../app/src/main/java/com/latticeward/musiccopilot/data/api/ArtistLookupContracts.kt); [P04 resolver](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistResolver.kt); [P05 admission](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistDecisionPolicy.kt); the real [LibraryRepository](../../app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryRepository.kt), [RoomLibraryRepository](../../app/src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt), [PlaybackGateway](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackGateway.kt), [PlaybackCommand](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackCommand.kt), [PlaybackQueuePolicy](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackQueuePolicy.kt), [VoiceCommandReceiptPolicy](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/VoiceCommandReceiptPolicy.kt), Media3 controller and service implementation.

The existing queue contract already uses \`PlaybackCommand.ReplaceQueue(commandId, items, startIndex, startPositionMs, playWhenReady)\` with max **5,000** items and one service-owned serialized worker. \`PlaybackGateway.execute()\` returns \`AppResult<PlaybackSnapshot>\` whose success promises a **service ACK**, not an optimistic UI prediction. The real controller correlates the private Media3 response using the command ID and decodes the authoritative service snapshot. The service independently probes the media file descriptors, focus and route generation before actual queue replacement.

## 2. P06 delivered files

| File | Source effect |
|---|---|
| [\`voice/session/PlayArtistPlaybackUseCase.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/PlayArtistPlaybackUseCase.kt) | New **internal**, source-only \`PlayArtistDispatchClaim\` one-use capability and \`PlayArtistPlaybackUseCase\`: bounded P03 artist membership, MediaStore live requery, fail-closed revision/access, one service-owned ReplaceQueue command, authoritative ACK postconditions |
| [\`Issue11Phase6ArtistPlaybackUseCaseTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase6ArtistPlaybackUseCaseTest.kt) | **25 authored synthetic JUnit fixtures / 0 executed**; fake LibraryRepository/PlaybackGateway; no real media or Media3 tests |
| [\`ISSUE_11_P06_ARTIST_QUEUE_PLAYBACK_AND_ACK_AUDIT.md\`](ISSUE_11_P06_ARTIST_QUEUE_PLAYBACK_AND_ACK_AUDIT.md) | This code-to-architecture audit and handoff |
| [\`ISSUE_11_P06_SOURCE_EVIDENCE_AND_QA_REGISTER.json\`](ISSUE_11_P06_SOURCE_EVIDENCE_AND_QA_REGISTER.json) | Machine-readable source, invariants, QA NOT_RUN and Phase 07 handoff |

Existing \`Media3\` service, queue engine, controller wire protocol, transport commands, local Room schema and P02/P03/P04/P05 classifiers **were not changed in P06**. P06 does **not** wire these new classes into \`DefaultVoiceSessionCoordinator\` (that is intentionally P07/P12). \`VoicePlaybackDispatcher.commandFor(PLAY_ARTIST)\` remains \`null\` until explicit coordinator integration; the prior five transport intents keep their existing behavior.

## 3. API and single-dispatch capability

New **internal** \`PlayArtistDispatchClaim\` holds:
- \`VoiceSessionId\` > 0 and attempt **1 or 2**;
- exactly **one** selected \`LibraryGroupId\` beginning \`artist:\`;
- the **P05 decision's or P07 selected confirmation's** original \`expectedLibraryRevision\`;
- an opaque, fixed, random-namespaced command ID formatted \`voice-<32 lowercase hex>-<positive session id>-<attempt>\`, recognized by the existing \`VoiceCommandReceiptPolicy\`;
- an \`AtomicBoolean\` that permits exactly **one invocation** of its P06 execution path, even on repeated click/re-entry/callback with the **same claim**.

The factory \`fromDirect()\` accepts only \`PlayArtistDecision.Direct\`; \`fromSelectedConfirmation()\` accepts a P01 \`PlayArtistConfirmationSnapshot\` **plus** a \`Select\` with matching token and an existing 0/1 option. It does not accept \`NotFound\`, \`Cancel\`, \`Neither\`, \`Unsupported\`, \`Unknown\` or an arbitrary textual artist name.

**Security/ownership boundary:** The snapshot/token comparison at the P06 factory is only **structural validation**. P07 must still verify that the confirmation snapshot is **the actual current coordinator-owned token**, in the same live session/generation, unclaimed and unexpired. The \`isCurrentSession: () -> Boolean\` callback is mandatory at P06 entry and checked at each suspension boundary and before dispatch. P06 **cannot** alone prove that two separately minted claims are not for the same Voice generation; that exactly-once authority is explicitly owned by P07. No promise of process-death restoration or durable idempotency is made.

No artist name, file title, MediaRef, URI, confirmation token or command nonce is logged, persisted or emitted to telemetry. \`PlayArtistDispatchClaim.toString()\` is redacted.

## 4. Queue preparation algorithm — one artist, stable, bounded

**P06 PRE-DISPATCH** (no live playback mutation until all succeed):

1. Atomically **claim once**, verify current Voice session predicate, original approved library revision and \`Ready\` index, then require PlaybackGateway \`Ready\`. A stale or disconnected state **fails before any queue command**.
2. Request \`LibraryRepository.artistTrackRefs(ArtistTrackRefsRequest(groupId, expectedRevision, offset, limit=100))\`. This is P03's real SQL artist-group query ordered **normalizedTitle then opaque MediaRef**, not the old song-title \`resolveVoiceCandidates\`. **Count and page are one Room read transaction**.
3. For **every** page require: matching revision; identical total; valid contiguous \`nextOffset\`; no duplicate \`MediaRef\`; all opaque IDs nonblank/≤128 chars; no truncated pages. Cap complete indexed membership at the already approved \`PlaybackQueuePolicy.MAX_ITEMS = 5000\`. More than 5,000 **fails**; it does not silently play an incomplete artist list.
4. For **every** indexed ref call \`LibraryRepository.get(ref)\`, which in production performs a fresh **MediaStore requery**. Require returned \`MediaRef\` equals requested and re-observed artist metadata hashes to **the original selected artist-group identity**; a moved/reclassified track must not cross artist boundaries.
5. Skip a ref **only** when its MediaStore verification returns a typed \`MEDIA_ITEM_MISSING\` or \`MEDIA_ITEM_UNPLAYABLE\` failure, or when its original artist metadata no longer belongs to the selected group. Permission errors, unexpected errors, changed revision or cancellation **abort** without dispatch. The remaining queue preserves P03's stable local order. If every item is gone, return typed \`MEDIA_ITEM_UNPLAYABLE\`, keeping previous playback untouched.
6. After the last live file requery, perform **another** P03 artist-membership read pinned to the same revision (includes P03 permission/epoch checks). Require unchanged group total and original first indexed ref. Recheck active Voice predicate, index status/revision and connected gateway.
7. Create **exactly one** \`PlaybackCommand.ReplaceQueue\` with the saved single-use command ID, \`items=verifiedRefs.toList()\`, \`startIndex=0\`, \`startPositionMs=0\`, \`playWhenReady=true\`; validate using the **existing** \`PlaybackQueuePolicy.validate()\`. No extra \`Play\` command, no parallel player, no independent queue state.
8. Submit through \`PlaybackGateway.execute(command)\` **once**. The MediaSessionService's own \`PlaybackQueueEngine.prepare\` independently reopens files, skips later invalid unselected entries, rejects missing selected ref, checks route/focus generations and then publishes its authoritative snapshot with correlated ACK.

**POST-ACK**: \`AppResult.Success(PlayArtistDecision.Executed)\` is returned only when the gateway successfully returns its **service ACK**, the new queueRevision is strictly newer than baseline, queue is nonempty, first selected ref matches, \`currentIndex=0\`, currentItem=first, its order is a subsequence of submitted refs, playback state is \`Playing\` or \`Buffering\`, \`lastError=null\`, and the live coordinator predicate remains true. Otherwise a typed \`PLAYBACK_FAILED\` or original service/controller failure is returned. A timeout cannot claim Executed; note the Media3 service might complete later after a client timeout, so no UI may optimistically claim success or blindly retry with a **new command ID**.

**Important nuance:** ACKed \`Buffering\` means the service accepted the attempt, **not** proven audible output. Device QA in P13 must separately observe actual playback, interruption/route handling and no late start after cancellation.

## 5. Fail-closed and no-unintended-mutation matrix

| Condition | P06 observable outcome | New PlaybackGateway command? |
|---|---|---:|
| Valid P05 Direct or P07 Select, current session, one or more valid local tracks, service ACK | \`Executed\` only with coherent snapshot | **One ReplaceQueue** |
| NotFound, Unknown, Unsupported, Cancel, Neither | No dispatch claim minted; current queue unaffected | **Zero** |
| Same claim invoked again (concurrent or sequential duplicate) | \`VOICE_STALE_SESSION\` | **Zero additional** |
| Wrong token/option, absent option | No claim minted | **Zero** |
| Invalid/expired session or generation | \`VOICE_STALE_SESSION\` | **Zero** |
| Artist metadata changed to another group | Skip moved item, or \`MEDIA_ITEM_UNPLAYABLE\` if none remain | **Zero unless remaining valid** |
| File deleted/moved or non-selected item corrupt | Skip only safe missing/unplayable | **One only if some valid remain** |
| All files missing/unplayable | \`MEDIA_ITEM_UNPLAYABLE\` | **Zero** |
| Missing MediaStore permission, index no longer Ready, stale revision | Typed permission/stale/library failure | **Zero** |
| Page count/cursor mismatch, duplicate refs or >5,000 members | Typed library or queue failure | **Zero** |
| Controller disconnected before dispatch | \`CONTROLLER_DISCONNECTED\` | **Zero** |
| Gateway typed failure, ACK error or stale/malformed service snapshot | Original failure or \`PLAYBACK_FAILED\`, never Executed | **At most one (already dispatched)** |
| Cancellation before dispatch | Cancellation propagated; no queue mutation by this use case | **Zero** |
| Cancellation/timeouts after dispatch began | No false Executed; service owns the outcome and safety | **At most one** |

**No unintended stop:** P06 sends **no** \`Pause\`, \`Stop\`, \`Seek\` or empty \`ReplaceQueue\` on unsuccessful artist lookup, rejection or cancellation. Existing player queue is preserved until the service chooses to accept an actual new queue. For cases already submitted to the service, actual OS/device outcome remains P13 verification work, not claimed here.

## 6. Tests — SOURCE AUTHORED ONLY

The new [Issue11Phase6ArtistPlaybackUseCaseTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase6ArtistPlaybackUseCaseTest.kt) contains **25** \`@Test\` methods with only synthetic media/labels and fake data/gateway ports:

- Command ID format/nonce, attempt validation, confirmation token+index shape, one-use callback safety.
- Positive single-track ReplaceQueue, pre-entry stale/disconnected rejection, empty/fully missing artist behavior.
- Partial deletion, changed-artist metadata, 5,000 max and >5,000 fail-closed, 100+ item paged lookups.
- Duplicate refs, incomplete page, permission/revision loss during requery, typed P03 error.
- Rejected service reply, bad/stale ACK snapshot, paused state, ACK subsequence after service skips later files.
- Coordinator generation invalidation during requery; zero service submission.

**Every test is \`NOT_RUN\`; no pass/fail tally or performance number is asserted from real execution**. The owner-requested P13 phone checklist M01–M48, technical T01–T10 and command lab L01–L06 remain \`NOT_RUN\`. The previous P02–P05 authored tests also remain \`NOT_RUN\`.

## 7. Known dependencies and handoff

1. **P07:** wire the app-scoped \`DefaultVoiceSessionCoordinator\` to P02 parser + P04 resolver + P05 policy; implement actual \`VoiceSessionState.Confirming\`, coordinator-owned one-shot \`VoiceCandidateToken\`, session/generation binding, exactly one selection, no state restoration after recreation; then call this P06 use case using one coordinator-issued claim and a real live-session predicate.
2. **P08/P09:** render one/two options and short TTS/error/manual fallback. Do not treat P06 preparation or buffered service ACK as confirmation of audible playback.
3. **P10:** cross-check route/focus/phone lifecycle race: aborting a Voice session before dispatch must not start sound after the user leaves Driving.
4. **P11/P12:** privacy allowlist and final wiring with robust owner-controlled command acknowledgements and integration fixtures.
5. **P13 only:** compile, source tests, Android/Media3 instrumented checks (only as approved), produce one cumulative APK and let the owner manually assess playback, queue integrity and latency. Use the actual user results to decide Issue #11 PASS/merge/close.

**Outstanding risk:** A full 5,000-song sequential double-verification path may be slow. The 5,000 limit is a **safety bound inherited from existing PlaybackQueuePolicy**, not a claim that the operation meets voice p95 2.5 s. P13 must measure realistic library sizes; a production cap or batch optimization requires a separately approved change, never silent truncation.

**Independent quality debt:** [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) P0 Voice reliability remains OPEN (prior Issue #10 owner-reported 24/50=48% vs 90% gate). This code does not repair STT or prove an audio success rate.

**P06 disposition:** new internal execution use case and authored regression fixture committed to the Issue #11 feature branch; no runtime wiring, compilation or QA performed. **Next P07.**
