# Issue #16 Phase 10 — Reviewed Contract Corrections

- **Version:** MVP v1.0
- **Phase:** 10 — Cross-role review and correction
- **Status:** **BINDING / REVIEW-CORRECTED**
- **Date:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Reviewer:** **OpenAI GPT-5.6 Sol — AI-assisted cross-role architecture review**
- **Source package:** user-attached English `MSM2(2).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Related contract:** [Phase 6 Implementation Structure and Public Contracts](../IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md)
- **Review record:** [Issue #16 Cross-role Review](ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)

## 1. Control rule

Phase 10 found two implementation-facing contract mismatches that do **not** change ADR ownership, process topology, product scope, persistence authority or module strategy. They therefore require no new ADR, but they must be corrected before final approval.

This file is a **binding correction addendum** to the Phase 6 public contracts. For the two declarations below, this file controls if the older Phase 6 snippet differs. The current `docs/SOFTWARE_ARCHITECTURE.md` also records these corrections so an Android engineer does not need to invent a side channel, event bus or new state owner.

No Persian/Farsi PRD or MVP is used as authority.

---

## 2. PH10-MAJ-001 — Voice confirmation presentation contract

### Finding

The approved P0 Wireframe [`WF-08_VOICE_CONFIRMING`](../../wireframes/app-states/WF-08_VOICE_CONFIRMING.md) requires an immutable presented candidate set with one or two user-visible candidate labels, plus Neither/Cancel. The Phase 6 `VoiceSessionState.Confirming` shape exposed only a token and `optionCount`, which was insufficient for the UI to render the approved confirmation without inventing another query/boundary.

### Corrected contract

```kotlin
data class VoiceConfirmationOption(
    val optionIndex: Int,
    val displayLabel: String,
)

sealed interface VoiceSessionState {
    // Idle / Listening / Processing unchanged

    data class Confirming(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val token: VoiceCandidateToken,
        val options: List<VoiceConfirmationOption>,
    ) : VoiceSessionState

    // Executed / Failed unchanged
}
```

### Binding invariants

- `options.size` is **1..2** only.
- Option order is immutable for the lifetime of the current confirmation token.
- `optionIndex` is the value passed back to `VoiceSessionCoordinator.confirm(token, optionIndex)`; the UI does not resolve a candidate through a repository side channel.
- `displayLabel` is presentation-only personal media text. It is **ephemeral**, never persisted, never logged, never included in crash/analytics payloads and never copied into repository evidence.
- Neither/Cancel are recovery actions, not synthetic candidate objects.
- A stale token or index cannot execute.
- `VoiceSessionCoordinator` remains the sole owner of active Voice state and pending confirmation.

### Validation consequence

The existing Voice ambiguity seam must additionally assert that the rendered option labels and order come only from the current `Confirming.options`, and that replacing/cancelling the Voice generation invalidates the old options/token.

**Resolution:** **RESOLVED.** No new boundary or owner is required.

---

## 3. PH10-MAJ-002 — Playback asynchronous recovery error projection

### Finding

The Phase 6 `PlaybackSnapshot.lastError` field was typed only as `AppError.Playback?`, while the approved runtime and test architecture explicitly require asynchronous corrupt/unplayable media to be represented as `AppError.MediaItem` and safe interruption/route recovery to be representable as `AppError.Interruption`. Without a service-observable typed projection, implementation could invent a second error event bus.

### Corrected contract

```kotlin
data class PlaybackSnapshot(
    val revision: Long,
    val queueRevision: Long,
    val playbackState: PlaybackState,
    val queue: List<MediaRef>,
    val currentIndex: Int?,
    val currentItem: MediaRef?,
    val positionMs: Long,
    val durationMs: Long?,
    val canSeek: Boolean,
    val canSkipPrevious: Boolean,
    val canSkipNext: Boolean,
    val lastError: AppError? = null,
)
```

### Binding invariants

Only these error families may be projected in `PlaybackSnapshot.lastError`:

- `AppError.MediaItem` — missing/corrupt/unplayable current queue item;
- `AppError.Playback` — Player/command/runtime playback failure;
- `AppError.Interruption` — call/focus/route state that results in a typed Safe-Paused recovery.

`AppError.Controller` remains connection/`PlaybackGateway.execute` level and is not service playback truth. Permission, Library, Storage, Privacy, Quality and Voice errors remain owned by their existing boundaries and must not be injected into Playback state.

The field contains safe typed codes/recovery only; raw exception messages, titles, paths, content URIs and provider payloads remain prohibited.

### Validation consequence

The existing corrupt-file and interruption tests must assert that observer surfaces receive the service-authored typed runtime error/recovery through the authoritative snapshot and that no UI-local error authority or new event bus is created.

**Resolution:** **RESOLVED.** Existing Playback ownership remains unchanged.

---

## 4. ADR impact

- ADR-0001..0008 remain **Accepted**.
- No Phase 10 finding changes an Accepted ADR decision.
- New ADRs required by these corrections: **0**.
- A future change that alters state ownership, process topology, persistence authority, deterministic Voice scope or module strategy still requires normal ADR/supersession governance.

## 5. Pass effect

After these corrections, the two Phase 10 Major findings are closed. The current architecture retains one Playback authority, one Voice confirmation owner and the same approved P0 recovery families.