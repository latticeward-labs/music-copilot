# Issue #16 Phase 6 — Implementation Structure and Public Contracts

- **Status:** **PHASE 6 NORMATIVE IMPLEMENTATION CONTRACT**
- **Date:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Upstream gates:** Phase 4 ADR gate PASS; Phase 5 Architecture Views PASS
- **Attached English source:** `MSM2(2).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Controlling architecture:** [SOFTWARE_ARCHITECTURE.md](../SOFTWARE_ARCHITECTURE.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md)
- **UI/recovery evidence:** approved [Wireframes](../WIREFRAMES.md), including the Phase 7 navigation/behavior audit and recovery terminal matrix
- **Privacy evidence:** [ANALYTICS_AND_PRIVACY.md](../ANALYTICS_AND_PRIVACY.md)

## 1. Source and finalization rule

The attached English package establishes the logical UI/Application/Playback/Voice/Data/Quality structure, public-port style, native Android/Media3/local-first constraints, replaceable Voice provider, typed result/error model, privacy boundary and a deliberately small repository blueprint. It also marks several physical implementation choices as replaceable or intentionally open.

Phase 4 finalized the consequential choices: same-process service runtime, serialized playback mutation, structured concurrency, Safe-Paused P1 restoration, one-`:app` minimal modular monolith, state ownership and extraction criteria. Phase 5 made those decisions visible as architecture views.

**Phase 6 finalizes the exact implementation-facing boundary contracts below.** These signatures are an Issue #16 finalization inside the already-approved source/ADR boundaries; they are not represented as pre-existing verbatim PDF code. A change that alters ownership, process topology, persistence authority, dependency direction or module strategy requires architecture/ADR review. Ordinary implementation details behind these contracts do not.

No Persian/Farsi PRD or MVP is an authority for this phase.

---

## 2. Boundary responsibility table

| Boundary | Owns | Must expose | Must not own / must not do |
|---|---|---|---|
| **UI / Presentation** | Compose screens, destination ViewModels, transient input/presentation state, accessibility semantics, navigation intents | immutable UI state, user intents, lifecycle-aware collection of public Flows/StateFlows | ExoPlayer/MediaSession, Room DAO, MediaStore Cursor, SpeechRecognizer, provider SDKs, permission truth cache, live queue, optimistic playback truth |
| **Application / Domain** | use cases, orchestration, deterministic policy application, recovery selection, P0/P1/P2 guards | use-case functions over public ports; typed `AppResult` | Android UI classes, concrete storage/provider/player SDKs, duplicated state authority, infrastructure exception leakage |
| **Playback** | `MediaSessionService`, Player/MediaSession, live queue/position, command serialization, controller connection, P1 Safe-Paused restore policy, interruption reducer | `PlaybackGateway`, `PlaybackCommand`, `PlaybackSnapshot` and playback support types | UI lifecycle ownership, Room as live queue, parallel player mutation, optimistic success, unconditional route/focus auto-resume |
| **Voice** | STT/TTS ports, provider adapter seam, deterministic mapper/candidate policy, one active session generation, confirmation/retry/cancellation | `SpeechToTextEngine`, `TextToSpeechEngine`, `VoiceSessionCoordinator` and Voice state types | playback authority, open-ended/LLM intent expansion, persistent transcript/audio, direct Player/MediaSession mutation |
| **Data** | MediaStore scan/re-query, normalization/reconciliation, Room projection, DataStore preferences/consent, P1 snapshot store, migrations | `LibraryRepository`, `PreferencesRepository` and persistence support ports | playback authority, UI state, generic backend/account/streaming clients, destructive loss of released user-owned records |
| **Quality / Diagnostics** | typed event schema, privacy filter, NoOp/approved sink adapters, safe diagnostic attributes | `TelemetrySink`, privacy gate and typed telemetry events | product/safety authority, blocking core flows, free-form payloads, prohibited personal/Voice/media data |

### 2.1 UI / Presentation

UI is a rendering and interaction boundary.

**Required behavior**
- Render only state received from destination ViewModels/public architecture ports.
- Send user intent to Application/Domain use cases or the appropriate public boundary.
- Collect Flows/StateFlows with Android lifecycle awareness.
- Preserve approved Entry/Exit/Back behavior and Driving one-layer interaction.
- Voice is rendered as overlay state on Player/Driving origin; it is not a navigation destination.
- Display only acknowledged playback state. A tap may show a neutral pending affordance, but never fabricate `Playing`.
- System permission/Settings/notification/lock-screen/external-control surfaces remain Android-owned handoffs.

**Forbidden**
- Creating ExoPlayer/MediaSession.
- Holding authoritative queue/position.
- Calling Room DAO, MediaStore Cursor, `SpeechRecognizer` or telemetry SDK directly.
- Persisting permission grant as truth.
- Swallowing typed errors and fabricating success.

### 2.2 Application / Domain

Application/Domain is pure orchestration plus deterministic policy.

**Owns**
- use cases for startup, permission recovery, library readiness/search, track selection, Driving Mode entry/exit, Voice entry, settings/privacy actions and recovery routing;
- P0/P1/P2 isolation;
- mapping public `AppError` to approved `RecoveryAction`;
- deterministic PREVIOUS/seek/Voice thresholds where the policy belongs above infrastructure;
- coordination across public ports without taking ownership from those ports.

**Rules**
- Prefer pure functions/reducers for policy.
- Accept injected ports and clocks/dispatchers.
- Infrastructure exceptions are converted to `AppError` at the boundary where they occur.
- No Application/Domain use case may depend on concrete Android/provider/storage/player classes.

### 2.3 Playback

Playback is the only live playback/queue authority.

**Owns**
- same-process `MediaSessionService`;
- exactly one production Player + MediaSession;
- live queue, current item/index, position and playback state;
- one `PlaybackCommandProcessor` with a single consumer and capacity 32;
- controller reconnect and complete snapshot publication;
- call/focus/route safety reducer;
- P1 versioned Safe-Paused restoration orchestration.

**Rules**
- accepted commands are sequential and never silently dropped;
- overflow returns typed `Busy`;
- completion is an authoritative snapshot change or typed rejection/timeout;
- Player/MediaSession mutation stays on application/player looper;
- live MediaSession state always wins over persisted P1 snapshot;
- cold restore validates MediaStore and prepares Paused only;
- route reconnect never auto-resumes.

### 2.4 Voice

Voice is a replaceable provider boundary plus deterministic product logic.

**Owns**
- push-to-talk session lifecycle;
- one active generation token;
- STT/TTS adapter cancellation;
- deterministic six-intent mapper;
- artist/item candidate resolution and one bounded confirmation;
- six-second listening timeout and one retry;
- playback acknowledgement before success TTS.

**Rules**
- stale generation callbacks are ignored;
- Back, Driving exit, permission loss, call/focus/route invalidation or replacement session cancels the active generation;
- playback commands go through `PlaybackGateway`;
- TTS failure after playback success never rolls playback back;
- raw audio/transcript/pending confirmation are never persisted.

### 2.5 Data

Data owns local source/projection/preference persistence, not playback.

**MediaStore**
- canonical authority for whether a local media item exists and can be re-queried.

**Room**
- rebuildable library/search projection and normalized search data;
- optional approved short history;
- optional P2 Likes;
- never live queue authority.

**DataStore**
- onboarding/settings/Bluetooth preference/consent;
- non-authoritative P1 `RestorableQueueSnapshot`.

**Rules**
- permission revocation triggers fresh platform truth + reconciliation;
- projection corruption can rebuild without touching media;
- released user-owned Likes/history need explicit tested migrations;
- no backend/account/cloud-sync/streaming client may hide under Data.

### 2.6 Quality / Diagnostics

Quality is non-authoritative and fail-safe.

**Owns**
- typed allowlisted event schema;
- privacy filtering/redaction;
- NoOp adapter and any later approved crash/analytics/config adapter.

**Rules**
- product flow never waits for telemetry;
- sink failure cannot change playback, permission, Voice, route or recovery outcome;
- no `Map<String, Any?>`, arbitrary JSON or free-form diagnostic payload at the public port;
- raw audio/transcript, title/artist/path/content URI/MediaStore ID, search query/results, queue, precise position/location, contacts, call identity, Bluetooth/vehicle identity and credentials are prohibited.

---

## 3. Shared leaf types

Shared types live under `core/model` or `core/result`. They contain no Android UI, Media3, Room, DataStore, speech-provider or analytics-SDK concrete types.

```kotlin
@JvmInline
value class CommandId(val value: String)

@JvmInline
value class MediaRef(val value: String) // opaque app-internal stable re-query reference

@JvmInline
value class VoiceSessionId(val value: Long)

@JvmInline
value class VoiceCandidateToken(val value: String)

enum class Recoverability {
    RECOVERABLE,
    USER_ACTION_REQUIRED,
    TERMINAL_FOR_ATTEMPT
}

enum class RecoveryAction {
    Retry,
    OpenSettings,
    Rebuild,
    Skip,
    ManualFallback,
    ManualResume,
    None
}

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
```

### 3.1 Typed `AppError`

`Throwable` is never a UI/public architecture result. The public error family is:

```kotlin
sealed interface AppError {
    val code: String
    val recoverability: Recoverability
    val recoveryAction: RecoveryAction

    data class Permission(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Library(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class MediaItem(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
        val mediaRef: MediaRef? = null,
    ) : AppError

    data class Playback(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Controller(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Voice(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Interruption(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Storage(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Privacy(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Quality(
        override val code: String,
        override val recoverability: Recoverability = Recoverability.RECOVERABLE,
        override val recoveryAction: RecoveryAction = RecoveryAction.None,
    ) : AppError
}
```

**Minimum stable P0/P1 codes**

| Category | Required initial codes | Default recovery |
|---|---|---|
| Permission | `PERMISSION_DENIED`, `PERMISSION_PERMANENTLY_DENIED`, `PERMISSION_REVOKED` | Retry / OpenSettings |
| Library | `LIBRARY_SCAN_FAILED`, `LIBRARY_INDEX_CORRUPT`, `LIBRARY_RECONCILE_FAILED` | Retry / Rebuild |
| MediaItem | `MEDIA_ITEM_MISSING`, `MEDIA_ITEM_CORRUPT`, `MEDIA_ITEM_UNPLAYABLE` | Skip |
| Playback | `PLAYBACK_BUSY`, `PLAYBACK_REJECTED`, `PLAYBACK_FAILED` | Retry / ManualResume |
| Controller | `CONTROLLER_DISCONNECTED`, `CONTROLLER_CONNECT_FAILED`, `CONTROLLER_TIMEOUT` | Retry |
| Voice | `VOICE_UNAVAILABLE`, `VOICE_TIMEOUT`, `VOICE_NO_MATCH`, `VOICE_SECOND_ATTEMPT_FAILED`, `VOICE_STALE_SESSION` | Retry once / ManualFallback |
| Interruption | `FOCUS_NOT_RECOVERED`, `ROUTE_INVALIDATED`, `CALL_OR_ROUTE_SAFE_PAUSED` | ManualResume |
| Storage | `STORAGE_READ_FAILED`, `STORAGE_WRITE_FAILED`, `SNAPSHOT_CORRUPT`, `SNAPSHOT_FUTURE_SCHEMA` | Retry / None |
| Privacy | `CONSENT_REQUIRED`, `CLEAR_PARTIAL`, `RETENTION_NOT_APPROVED` | OpenSettings / Retry / None |
| Quality | `QUALITY_DROPPED`, `QUALITY_ADAPTER_UNAVAILABLE` | None |

New codes may be added in implementation when they remain inside the same category/recovery ownership. A code that creates a new authority, persistence behavior or user recovery family requires architecture review.

**Diagnostic rule:** `AppError` public fields must remain non-sensitive. Raw exception messages, paths, titles, transcripts, provider payloads and identifiers remain internal and must be sanitized before any diagnostic event.

---

## 4. Playback public contract

Package: `com.latticeward.musiccopilot.playback.api`

### 4.1 `PlaybackCommand`

```kotlin
sealed interface PlaybackCommand {
    val commandId: CommandId

    data class ReplaceQueue(
        override val commandId: CommandId,
        val items: List<MediaRef>,
        val startIndex: Int,
        val startPositionMs: Long = 0L,
        val playWhenReady: Boolean,
    ) : PlaybackCommand

    data class Play(
        override val commandId: CommandId,
    ) : PlaybackCommand

    data class Pause(
        override val commandId: CommandId,
    ) : PlaybackCommand

    data class Next(
        override val commandId: CommandId,
    ) : PlaybackCommand

    data class Previous(
        override val commandId: CommandId,
    ) : PlaybackCommand

    data class SeekBy(
        override val commandId: CommandId,
        val deltaMs: Long,
    ) : PlaybackCommand

    data class SeekTo(
        override val commandId: CommandId,
        val positionMs: Long,
    ) : PlaybackCommand

    data class Stop(
        override val commandId: CommandId,
    ) : PlaybackCommand
}
```

**Command invariants**
- `ReplaceQueue.items` must be non-empty after MediaStore validation.
- `startIndex` must reference the validated list.
- positions are non-negative and clamped by the service to verified duration.
- `Previous` policy is service/application deterministic: restart current item when position `> 3_000 ms`, otherwise previous item.
- seek-back Voice/manual policy uses `SeekBy(-10_000)`.
- command IDs are used for acknowledgement/deduplication tracing, never analytics identity.
- a command is not successful until the service publishes the authoritative result.

### 4.2 `PlaybackSnapshot`

```kotlin
enum class PlaybackState {
    Idle,
    Buffering,
    Playing,
    Paused,
    Ended,
    Error
}

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
    val lastError: AppError.Playback? = null,
)
```

**Snapshot invariants**
- emitted from service/controller truth, not assembled independently by screens;
- complete replacement on reconnect before connection becomes `Ready`;
- `revision` monotonically changes when authoritative playback-visible state changes;
- `queueRevision` changes when queue identity/order changes;
- `currentIndex/currentItem` agree with queue or are both null;
- UI may derive display metadata by resolving `MediaRef` through Data, but must not mutate snapshot authority;
- persisted P1 snapshot is a different type and never masquerades as live `PlaybackSnapshot`.

### 4.3 `PlaybackGateway`

```kotlin
enum class PlaybackConnectionState {
    Disconnected,
    Connecting,
    Ready,
    RecoverableError
}

interface PlaybackGateway {
    val connectionState: StateFlow<PlaybackConnectionState>
    val snapshot: StateFlow<PlaybackSnapshot>

    suspend fun execute(
        command: PlaybackCommand,
    ): AppResult<PlaybackSnapshot>
}
```

**Contract semantics**
- production implementation is the app-scoped controller connection to the service-owned MediaSession;
- `execute` serializes through the service `PlaybackCommandProcessor`;
- capacity is 32 pending commands; overflow -> `AppError.Playback(code = "PLAYBACK_BUSY", ...)`;
- no caller directly mutates Player;
- timeout/rejection returns the last confirmed snapshot through `snapshot`; it does not fabricate success;
- external Android MediaController commands converge on the same processor/authority even when they do not enter through this Kotlin interface.

### 4.4 P1 restoration support port

Owner package: `com.latticeward.musiccopilot.data.api`; consumed by Playback through the public port only.

This supporting port is required so Playback does not call DataStore directly:

```kotlin
data class RestorableQueueSnapshot(
    val schemaVersion: Int,
    val queueRevision: Long,
    val items: List<MediaRef>,
    val currentIndex: Int,
    val positionMs: Long,
    val capturedAtEpochMs: Long,
)

interface RestorableQueueStore {
    suspend fun read(): AppResult<RestorableQueueSnapshot?>
    suspend fun write(snapshot: RestorableQueueSnapshot): AppResult<Unit>
    suspend fun clear(): AppResult<Unit>
}
```

This store is **non-authoritative**. The Playback boundary performs schema validation, MediaStore re-query, missing-item drop, duration clamp and Paused-only preparation.

---

## 5. Voice public contracts

Package: `com.latticeward.musiccopilot.voice.api`

### 5.1 `SpeechToTextEngine`

```kotlin
data class SpeechRecognitionRequest(
    val sessionId: VoiceSessionId,
    val languageTag: String,
    val maxListeningMs: Long = 6_000L,
)

data class SpeechRecognitionResult(
    val transcript: String,
    val providerConfidence: Double?,
    val detectedLanguageTag: String?,
)

interface SpeechToTextEngine {
    fun isAvailable(): Boolean

    suspend fun recognize(
        request: SpeechRecognitionRequest,
    ): AppResult<SpeechRecognitionResult>

    fun cancel(sessionId: VoiceSessionId)
}
```

**Rules**
- request starts only after explicit push-to-talk and current permission/capability check;
- coroutine/session cancellation must cancel provider work;
- transcript/confidence are ephemeral and never persisted or sent to telemetry;
- missing/invalid confidence never direct-executes;
- no generic offline guarantee is implied by this interface;
- remote/cloud implementation remains disabled until disclosure/consent/provider gates pass.

### 5.2 `TextToSpeechEngine`

```kotlin
data class SpeechOutput(
    val sessionId: VoiceSessionId,
    val textKey: String,
    val shortText: String,
)

interface TextToSpeechEngine {
    fun isAvailable(): Boolean

    suspend fun speak(
        output: SpeechOutput,
    ): AppResult<Unit>

    fun stop(sessionId: VoiceSessionId)
}
```

**Rules**
- TTS is feedback only and never playback authority;
- use short bounded feedback;
- success TTS follows playback acknowledgement;
- TTS failure does not roll back an already-acknowledged playback command;
- `shortText` is not a telemetry field.

### 5.3 `VoiceSessionCoordinator`

```kotlin
enum class VoiceOrigin {
    Player,
    Driving
}

enum class VoiceCancelReason {
    UserBack,
    DrivingExit,
    ReplacedByNewSession,
    PermissionLost,
    CallOrFocusInvalidated,
    RouteInvalidated,
    OriginInvalidated
}

sealed interface VoiceSessionState {
    data object Idle : VoiceSessionState

    data class Listening(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val attempt: Int,
    ) : VoiceSessionState

    data class Processing(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val attempt: Int,
    ) : VoiceSessionState

    data class Confirming(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val token: VoiceCandidateToken,
        val optionCount: Int,
    ) : VoiceSessionState

    data class Executed(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
    ) : VoiceSessionState

    data class Failed(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val error: AppError.Voice,
        val canRetry: Boolean,
    ) : VoiceSessionState
}

interface VoiceSessionCoordinator {
    val state: StateFlow<VoiceSessionState>

    fun start(origin: VoiceOrigin): AppResult<VoiceSessionId>

    fun confirm(
        token: VoiceCandidateToken,
        optionIndex: Int,
    ): AppResult<Unit>

    fun retry(): AppResult<Unit>

    fun cancel(reason: VoiceCancelReason)
}
```

**Coordinator invariants**
- exactly one active generation/session;
- `start` cancels any previous session before creating the next generation;
- attempt values are 1 or 2 only;
- retry is accepted only after first attempt failure and only once;
- confirmation is bounded to the current token and at most two candidates;
- stale session/token callbacks are ignored;
- deterministic mapper executes only the six MVP intents;
- no top-match guessing for ambiguous/not-found;
- playback mutation occurs only via `PlaybackGateway`;
- success state follows authoritative playback acknowledgement;
- Back/Driving Exit cancels Voice before navigation returns to origin.

### 5.4 Deterministic Voice policy constants

```kotlin
object VoicePolicy {
    const val LISTEN_TIMEOUT_MS = 6_000L
    const val MAX_ATTEMPTS = 2

    const val T_DIRECT = 0.90
    const val T_DIRECT_MIN_ALLOWED = 0.80
    const val T_DIRECT_MAX_ALLOWED = 0.98

    const val ARTIST_CLEAR_SIMILARITY = 0.88
    const val ARTIST_PLAUSIBLE_SIMILARITY = 0.72
    const val MIN_TOP_TWO_SEPARATION = 0.08
}
```

Changing these values outside the validated ranges/policies is not an implementation convenience; it requires the downstream evidence gates already named by Phase 3/4.

---

## 6. Data public contracts

Package: `com.latticeward.musiccopilot.data.api`

### 6.1 `LibraryRepository`

```kotlin
enum class LibraryIndexStatus {
    Unavailable,
    Scanning,
    Ready,
    Empty,
    RecoverableError
}

data class LibraryItem(
    val ref: MediaRef,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
)

data class LibraryIndexSnapshot(
    val revision: Long,
    val status: LibraryIndexStatus,
    val itemCount: Int,
    val lastError: AppError.Library? = null,
)

data class LibraryReconcileResult(
    val scanned: Int,
    val insertedOrUpdated: Int,
    val removedStale: Int,
    val skippedInvalid: Int,
    val revision: Long,
)

data class LibraryQuery(
    val text: String,
    val limit: Int,
)

data class LibraryCandidate(
    val ref: MediaRef,
    val displayLabel: String,
    val normalizedKey: String,
    val similarity: Double,
)

interface LibraryRepository {
    val index: StateFlow<LibraryIndexSnapshot>

    fun observeLibrary(): Flow<List<LibraryItem>>

    suspend fun reconcile(): AppResult<LibraryReconcileResult>

    suspend fun search(
        query: LibraryQuery,
    ): AppResult<List<LibraryItem>>

    suspend fun get(
        ref: MediaRef,
    ): AppResult<LibraryItem>

    suspend fun resolveVoiceCandidates(
        normalizedTarget: String,
        limit: Int = 2,
    ): AppResult<List<LibraryCandidate>>
}
```

**Rules**
- MediaStore is canonical existence/identity truth;
- Room backs the derived projection; projection may rebuild;
- `get` revalidates stale/missing references as needed;
- permission revocation never returns cached `Ready` as permission truth;
- UI/Search query state is presentation state, not repository authority;
- personal titles/artist values are app-internal and are prohibited from telemetry.

### 6.2 `PreferencesRepository`

```kotlin
enum class CloudSpeechConsent {
    NotApplicable,
    NotAsked,
    Declined,
    Granted,
    Revoked
}

data class AppPreferences(
    val onboardingComplete: Boolean,
    val selectedBluetoothHint: String?,
    val cloudSpeechConsent: CloudSpeechConsent,
)

sealed interface PreferenceUpdate {
    data class SetOnboardingComplete(val complete: Boolean) : PreferenceUpdate
    data class SetBluetoothHint(val value: String?) : PreferenceUpdate
    data class SetCloudSpeechConsent(val value: CloudSpeechConsent) : PreferenceUpdate
}

interface PreferencesRepository {
    val preferences: StateFlow<AppPreferences>

    suspend fun update(
        update: PreferenceUpdate,
    ): AppResult<AppPreferences>
}
```

**Rules**
- Android permission grant/revocation is **never** stored here as current truth;
- provider capability is external truth and rechecked;
- selected Bluetooth value is only a P1 hint and must not auto-open Driving or auto-resume playback;
- consent is current control state, not an event-history log;
- exact retention durations remain disabled until approved.

### 6.3 Data implementation ownership

Implementation packages:
- `data/mediastore`: MediaStore queries/re-query and permission-aware scanner;
- `data/room`: entities/DAO/database, projection rebuild, released-record migrations;
- `data/datastore`: preferences/consent and `RestorableQueueStore`;
- `data/mapping`: Android/DB models <-> public domain models.

No DAO/Cursor/DataStore concrete type crosses `data.api`.

---

## 7. Quality / Diagnostics public contract

Package: `com.latticeward.musiccopilot.quality.api`

### 7.1 Typed event model

The public event model is closed/typed. It must mirror the allowlist in `ANALYTICS_AND_PRIVACY.md` and may not accept arbitrary property maps.

```kotlin
enum class ConfidenceBucket { Low, Medium, High }
enum class LatencyBucket { Fast, Expected, Slow }
enum class RouteType { BuiltIn, Wired, Bluetooth, Unknown }
enum class TelemetryPlaybackState { Idle, Buffering, Playing, Paused, Ended, Error }

sealed interface TelemetryEvent {
    data class DrivingModeStarted(
        val entrySource: String,
        val bluetoothConnected: Boolean,
    ) : TelemetryEvent

    data class VoiceButtonPressed(
        val playbackState: TelemetryPlaybackState,
    ) : TelemetryEvent

    data class IntentDetected(
        val intentType: String,
        val confidenceBucket: ConfidenceBucket,
    ) : TelemetryEvent

    data class IntentFailed(
        val intentType: String?,
        val failureCode: String,
        val confidenceBucket: ConfidenceBucket?,
        val latencyBucket: LatencyBucket?,
    ) : TelemetryEvent

    data class CommandExecuted(
        val intentType: String,
        val latencyBucket: LatencyBucket,
    ) : TelemetryEvent

    data class ManualCorrection(
        val previousIntent: String,
        val correctionType: String,
    ) : TelemetryEvent

    data class PlayerError(
        val errorCode: String,
        val routeType: RouteType,
    ) : TelemetryEvent

    data class SessionCompleted(
        val durationBucket: String,
        val commandsCount: Int,
    ) : TelemetryEvent
}
```

`String` fields above are restricted to documented enums/stable codes even where represented as String in the reference contract; free-form content is forbidden.

### 7.2 Privacy gate and `TelemetrySink`

```kotlin
@JvmInline
value class SanitizedTelemetryEvent internal constructor(
    internal val encoded: String,
)

interface TelemetryPrivacyGate {
    fun sanitize(
        event: TelemetryEvent,
    ): AppResult<SanitizedTelemetryEvent>
}

interface TelemetrySink {
    /**
     * Non-blocking, best effort. Must not throw.
     * false means dropped/unavailable and MUST NOT alter product behavior.
     */
    fun tryEmit(
        event: SanitizedTelemetryEvent,
    ): Boolean
}
```

**Rules**
- Application/Domain creates typed `TelemetryEvent`.
- `TelemetryPrivacyGate` is mandatory before a non-NoOp sink.
- `TelemetrySink` only receives sanitized output.
- default adapter may be NoOp.
- no telemetry call is awaited before user-visible success/recovery.
- unknown events/properties are rejected, not forwarded.
- Beta completion still requires approved measurement to be enabled/validated; NoOp is only a safe foundation default.

---

## 8. Application / Domain use-case surface

Package: `com.latticeward.musiccopilot.core.application`

Issue #4 may add concrete use-case classes inside this boundary without creating a new architecture boundary. The initial required set is:

```text
BootstrapUseCase
RequestPermissionUseCase / RecheckPermissionUseCase
ReconcileLibraryUseCase
ObserveLibraryUseCase
SearchLibraryUseCase
SelectTrackUseCase
SendPlaybackCommandUseCase
EnterDrivingModeUseCase / ExitDrivingModeUseCase
StartVoiceUseCase / ConfirmVoiceUseCase / RetryVoiceUseCase / CancelVoiceUseCase
UpdatePreferencesUseCase
ClearApprovedLocalDataUseCase
```

**Use-case dependency rule**
- depend only on `core/model`, `core/result`, boundary `api` packages and other pure application/domain policies;
- never instantiate concrete adapters;
- cross-boundary orchestration stays here rather than feature ViewModels.

Pure policies/reducers may live under `core/domain`, including:
- PREVIOUS/seek policy;
- P0/P1/P2 feature guard;
- error -> recovery mapping;
- Driving restrictions;
- interruption guard evaluator when expressed as pure inputs/outputs.

---

## 9. Dependency direction — normative package rules

### 9.1 Leaf core

`core/model`, `core/result`, `core/time`, `core/dispatchers` are leaf shared packages.

They may depend on Kotlin/coroutines primitives only. They **must not** depend on:
- Compose/Activity/ViewModel;
- Media3;
- Room/SQLite;
- DataStore;
- SpeechRecognizer/provider SDK;
- analytics/crash SDK.

### 9.2 Boundary APIs

`playback/api`, `voice/api`, `data/api`, `quality/api` may depend on leaf core types.

They may not depend on their own concrete implementation package.

### 9.3 Application/Domain

`core/application` may depend on:
- leaf core;
- `playback/api`;
- `voice/api`;
- `data/api`;
- `quality/api`.

`core/domain` should remain pure; when possible it depends only on leaf core.

### 9.4 Feature/UI

`feature/*` may depend on:
- `core/application`;
- leaf core;
- read-only public boundary API state required for rendering.

`feature/*` must not depend on:
- another feature's implementation package;
- `playback/media3`, `playback/service`, `playback/controller` concrete classes;
- `voice/android` provider classes;
- `data/room`, `data/mediastore`, `data/datastore`;
- `quality/adapters`;
- ExoPlayer/MediaSession/DAO/Cursor/SpeechRecognizer/analytics SDK.

### 9.5 Playback implementation

Playback implementation may depend on:
- `playback/api`;
- leaf core;
- `data/api.RestorableQueueStore`;
- Android/Media3.

It may not depend on feature/UI or quality adapter.

### 9.6 Voice implementation

Voice implementation may depend on:
- `voice/api`;
- leaf core;
- `playback/api.PlaybackGateway`;
- `data/api.LibraryRepository`;
- selected speech/TTS platform adapter.

It may not depend on Player/MediaSession concrete classes or feature/UI implementation.

### 9.7 Data implementation

Data implementation may depend on:
- `data/api`;
- leaf core;
- Android MediaStore;
- Room;
- DataStore.

It may not depend on feature/UI, Playback concrete implementation, Voice concrete implementation or quality adapter.

### 9.8 Quality implementation

Quality implementation may depend on:
- `quality/api`;
- leaf core;
- approved optional SDK.

It may not make calls back into playback/voice/data to decide behavior.

### 9.9 Composition root

`app/` may depend on every public API and concrete implementation **only for composition/startup/navigation wiring**.

Business logic in `app/` is forbidden; it belongs in Application/Domain or the owning boundary.

---

## 10. DI / Hilt ownership and lifetime

The architecture binds **composition ownership**, not the Hilt library itself. Hilt is an allowed initial implementation and remains replaceable if the same contracts/lifetimes are preserved.

### 10.1 Composition root

Location:

```text
app/
├── MusicCopilotApplication
├── MainActivity
├── navigation/
└── di/
    ├── PlaybackBindings
    ├── VoiceBindings
    ├── DataBindings
    ├── QualityBindings
    └── ApplicationBindings
```

Only `app/di` owns cross-boundary production binding decisions.

Owning implementation packages may expose constructors/factories, but must not create hidden global service locators or feature-local alternate graphs.

### 10.2 Required lifetimes

| Object | Lifetime / owner |
|---|---|
| `PlaybackGateway` / `PlaybackControllerConnection` | application-scoped |
| `LibraryRepository` | application-scoped |
| `PreferencesRepository` | application-scoped |
| Room database | application-scoped |
| DataStore instances | application-scoped |
| `VoiceSessionCoordinator` | application-scoped coordinator; active session child job is ephemeral/cancellable |
| STT/TTS adapter | injected behind Voice ports; provider operation bound to current Voice session cancellation |
| `TelemetryPrivacyGate` | application-scoped |
| `TelemetrySink` | application-scoped NoOp/approved adapter |
| Player | **MediaSessionService-owned**, not UI/application singleton |
| MediaSession | **MediaSessionService-owned** |
| `PlaybackCommandProcessor` | **MediaSessionService-owned** |
| destination ViewModel | destination/ViewModel lifecycle |
| UI coroutine work | `viewModelScope` |
| Playback coroutine work | service `SupervisorJob + Main.immediate` |
| library/repository long work | application/repository scope, restartable after process death |

### 10.3 DI prohibitions

- no Player/MediaSession injection into ViewModel or screen;
- no DAO injection into feature ViewModel;
- no provider SDK injection into feature;
- no `GlobalScope`;
- no service locator;
- no duplicate `PlaybackGateway` graph;
- no quality adapter dependency required for product startup.

---

## 11. Initial package tree

This is the **required initial package tree inside one production `:app` Gradle module**. The seven names are logical packages, not seven Gradle modules.

```text
:app
└── src/main/java/com/latticeward/musiccopilot/
    ├── app/
    │   ├── MusicCopilotApplication.kt
    │   ├── MainActivity.kt
    │   ├── navigation/
    │   │   ├── AppNavHost.kt
    │   │   ├── Destination.kt
    │   │   └── RootNavigationStateHolder.kt
    │   └── di/
    │       ├── ApplicationBindings.kt
    │       ├── PlaybackBindings.kt
    │       ├── VoiceBindings.kt
    │       ├── DataBindings.kt
    │       └── QualityBindings.kt
    │
    ├── core/
    │   ├── model/
    │   │   ├── MediaRef.kt
    │   │   └── Identifiers.kt
    │   ├── result/
    │   │   ├── AppResult.kt
    │   │   ├── AppError.kt
    │   │   └── RecoveryAction.kt
    │   ├── application/
    │   │   ├── bootstrap/
    │   │   ├── permission/
    │   │   ├── library/
    │   │   ├── playback/
    │   │   ├── driving/
    │   │   ├── voice/
    │   │   └── settings/
    │   ├── domain/
    │   │   ├── PlaybackPolicy.kt
    │   │   ├── RecoveryPolicy.kt
    │   │   └── PriorityPolicy.kt
    │   ├── dispatchers/
    │   │   └── DispatcherProvider.kt
    │   └── time/
    │       └── Clock.kt
    │
    ├── feature/
    │   ├── onboarding/
    │   ├── permission/
    │   ├── library/
    │   ├── search/
    │   ├── player/
    │   ├── driving/
    │   ├── settings/
    │   └── liked/                 # P2 removable
    │
    ├── playback/
    │   ├── api/
    │   │   ├── PlaybackGateway.kt
    │   │   ├── PlaybackCommand.kt
    │   │   └── PlaybackSnapshot.kt
    │   ├── controller/
    │   │   └── PlaybackControllerConnection.kt
    │   ├── service/
    │   │   └── MusicPlaybackService.kt
    │   ├── media3/
    │   │   └── PlayerFactory.kt
    │   ├── command/
    │   │   └── PlaybackCommandProcessor.kt
    │   ├── interruption/
    │   │   └── InterruptionPolicy.kt
    │   └── restore/
    │       └── SafePausedRestorer.kt
    │
    ├── voice/
    │   ├── api/
    │   │   ├── SpeechToTextEngine.kt
    │   │   ├── TextToSpeechEngine.kt
    │   │   └── VoiceSessionCoordinator.kt
    │   ├── session/
    │   │   └── DefaultVoiceSessionCoordinator.kt
    │   ├── mapper/
    │   │   ├── DeterministicIntentMapper.kt
    │   │   ├── CandidateResolver.kt
    │   │   └── VoicePolicy.kt
    │   └── android/
    │       ├── AndroidSpeechToTextEngine.kt
    │       └── AndroidTextToSpeechEngine.kt
    │
    ├── data/
    │   ├── api/
    │   │   ├── LibraryRepository.kt
    │   │   ├── PreferencesRepository.kt
    │   │   └── RestorableQueueStore.kt
    │   ├── mediastore/
    │   │   ├── MediaStoreScanner.kt
    │   │   └── MediaStoreQuery.kt
    │   ├── room/
    │   │   ├── MusicCopilotDatabase.kt
    │   │   ├── LibraryDao.kt
    │   │   └── migration/
    │   ├── datastore/
    │   │   ├── PreferencesDataStore.kt
    │   │   └── RestorableQueueDataStore.kt
    │   ├── repository/
    │   │   ├── DefaultLibraryRepository.kt
    │   │   └── DefaultPreferencesRepository.kt
    │   └── mapping/
    │
    └── quality/
        ├── api/
        │   ├── TelemetryEvent.kt
        │   ├── TelemetryPrivacyGate.kt
        │   └── TelemetrySink.kt
        ├── privacy/
        │   └── DefaultTelemetryPrivacyGate.kt
        ├── noop/
        │   └── NoOpTelemetrySink.kt
        └── adapters/               # only approved optional adapters
```

Non-production evidence remains outside the app dependency graph:

```text
spikes/android-validation/
```

### 11.1 Naming flexibility

File/class names for concrete implementations may change during Issue #4 if responsibilities, public contracts and dependency direction remain identical. Package ownership and the public `api` seams above are binding. A rename is not a new architecture decision.

---

## 12. Gradle module extraction criteria and anti-explosion gate

### 12.1 Initial physical modules

Production MVP starts with:

```text
:app
```

The existing `spikes/android-validation` evidence harness is not a production dependency and does not count as a production architecture module.

### 12.2 Default decision

**Do not add a production Gradle module by default.**

A package may become a separate module only when the PR records at least one demonstrated criterion:

1. independent ownership/release responsibility;
2. stable reusable public contract required by more than one module/app target;
3. meaningful measured build isolation/performance benefit;
4. dependency enforcement that package visibility/static checks cannot reliably provide;
5. material test isolation/feedback-time benefit.

### 12.3 Mandatory extraction note in PR

Any proposed new module must document:

```text
Extraction criterion:
Evidence/measurement:
Public contracts moved:
Allowed dependency edges before/after:
Build/test benefit:
Migration cost:
P0/P1/P2 impact:
Why package-level enforcement is insufficient:
Scope check: no v1.1 capability introduced
ADR check: ownership/topology changed? yes/no
```

If the extraction materially changes architecture ownership, process topology or state/data authority, it requires a new ADR. Otherwise it is a repository refactor under ADR-0008.

### 12.4 Module-explosion prohibitions

Do **not** create modules merely for:
- one screen;
- one ViewModel;
- one repository implementation;
- one use case;
- one SDK wrapper;
- abstract “clean architecture” symmetry;
- future team growth that has not occurred;
- speculative reuse;
- a P2 feature that must remain removable.

The MVP timebox prefers a small enforced monolith over a graph of tiny modules.

---

## 13. Implementation sequence for Issue #4

Issue #4 can start from this order without inventing architecture:

1. create the one-`:app` package skeleton exactly by boundary;
2. add leaf `core/model` and `core/result`;
3. add boundary `api` contracts from Sections 4–7;
4. wire the composition root and application-scoped repository/controller/quality objects;
5. implement MediaStore/Room/DataStore adapters behind Data APIs;
6. implement same-process `MediaSessionService`, Player/MediaSession and serialized command processor;
7. implement `PlaybackControllerConnection` / `PlaybackGateway`;
8. implement Application/Domain use cases;
9. implement feature ViewModels/Compose screens against use cases/public state;
10. implement Voice provider adapters + deterministic mapper + `VoiceSessionCoordinator`;
11. implement privacy gate + NoOp telemetry, then approved measurement adapter only when its gate is satisfied;
12. add architecture/dependency tests before any optional module extraction.

---

## 14. Required architecture tests/static checks

Issue #4 foundation must add enforceable checks for at least:

- feature/UI cannot reference ExoPlayer, MediaSession concrete types, Room DAO, MediaStore Cursor, SpeechRecognizer or analytics SDK;
- only Playback service package constructs Player/MediaSession;
- one production Player/MediaSession instance per service lifecycle;
- `PlaybackCommandProcessor` ordering, capacity 32 and Busy behavior;
- controller reconnect complete-snapshot replacement;
- `GlobalScope` absent;
- Voice exactly one active generation, one retry and stale callback suppression;
- Voice playback uses `PlaybackGateway`;
- MediaStore authority / Room rebuild behavior;
- permission revocation invalidates stale index truth;
- P1 snapshot restore is Paused-only and never overrides live session;
- telemetry schema rejects free-form/unknown fields and prohibited attributes;
- NoOp telemetry cannot block product flow;
- P2 `feature/liked` can be disabled/removed without breaking P0 composition.

---

## 15. Phase 6 no-invention checklist

An Android engineer starting Issue #4 has explicit answers to:

- **Who owns playback/queue/position?** Playback `MediaSessionService`.
- **Who creates Player/MediaSession?** Playback service lifecycle.
- **How do UI/Voice/system mutate playback?** serialized command authority; app/Voice through `PlaybackGateway`.
- **What does UI observe?** authoritative `PlaybackSnapshot`.
- **Who owns Voice state?** application-scoped `VoiceSessionCoordinator` with one ephemeral active generation.
- **Where does STT/TTS live?** Voice provider ports/adapters.
- **Who owns media existence?** MediaStore.
- **Who owns the searchable index?** Data/Room rebuildable projection.
- **Who owns settings/consent?** DataStore via `PreferencesRepository`.
- **Where does P1 queue restore live?** DataStore through `RestorableQueueStore`, non-authoritative; Playback validates/restores Paused.
- **Where are errors defined?** `core/result`, typed `AppResult/AppError`.
- **Where does telemetry enter?** typed event -> privacy gate -> `TelemetrySink`.
- **Can telemetry/provider failure block playback?** No.
- **Where are DI bindings?** `app/di` composition root.
- **Is Hilt mandatory forever?** No; lifetime/dependency contract is mandatory.
- **Are app/core/feature/playback/voice/data/quality Gradle modules?** No; packages in one `:app`.
- **When may a new module be extracted?** only under the five ADR-0008 evidence criteria.
- **May a feature invent a new ownership/source of truth?** No.

If implementation needs a materially different answer to any item above, work stops for architecture review rather than silently inventing a new boundary.

---

## 16. Phase 6 result

This document is the normative implementation-facing supplement to the current Software Architecture baseline.

**Phase 6 gate target:** Android foundation work can begin without inventing a new boundary, state owner, persistence authority, DI owner, public port, service owner or physical module strategy.

Formal validation is recorded in [PHASE6_IMPLEMENTATION_READINESS_AUDIT.md](PHASE6_IMPLEMENTATION_READINESS_AUDIT.md).
