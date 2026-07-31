# Software Architecture Baseline — MVP v1.0

- **Status:** Not yet approved
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Depends on:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2), [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3) and [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Authority:** [PRD v1.0](PRD.md), [MVP v1.0](MVP.md) and accepted [ADRs](adr/README.md)

This file is the home of the final pre-implementation architecture baseline. The constraints below are provisional until Issue #16 is approved. Missing details must not be invented inside feature implementation.

## Baseline constraints already established by product scope

- Native Android 10+ with Kotlin and Jetpack Compose
- Lawful local audio only; no backend, account, cloud sync, online catalog or streaming
- Media3 playback independent of UI lifecycle and owned by a service
- MediaStore for device media discovery
- Room for indexed/local structured data and DataStore for settings
- Push-to-talk; Android STT/TTS behind a replaceable interface
- Restricted deterministic six-intent mapping
- Raw voice is not stored; prohibited personal media metadata does not enter telemetry
- Android 10/12/14+, Pixel/Samsung/Xiaomi and at least three vehicle/head-unit models in QA

## Architecture views required before approval

### 1. System context

_To be completed: app, Android platform services, local media, Bluetooth/head unit, optional STT processing and privacy-safe quality services._

### 2. Component boundaries

Required logical boundaries:

- UI / Presentation
- Application / Domain
- Playback
- Voice
- Data
- Quality / Diagnostics

Each boundary must define responsibilities, public contracts, allowed dependencies and forbidden dependencies.

### 3. State ownership

The baseline must name a single source of truth for:

- Playback state and current media
- Queue and position
- Voice state and pending confirmation
- Library index and scan status
- Screen/UI state
- Settings, Bluetooth preference and privacy controls
- Analytics session state

### 4. Lifecycle and background behavior

The baseline must define:

- UI recreation and synchronization
- Service lifetime and MediaSession ownership
- Process-death expectations and restoration limits
- Screen-off/background playback
- Notification, lock-screen and Bluetooth command synchronization
- Audio Focus, calls, headset disconnect and route-change recovery

### 5. Critical data flows

Required diagrams or sequence descriptions:

1. MediaStore scan -> normalization/index -> Library UI
2. Track selection -> queue -> service-owned playback -> UI observation
3. Manual playback command -> service -> state update
4. Voice tap -> STT -> restricted mapper -> certainty/confirmation -> playback command -> TTS
5. Call or route interruption -> policy -> playback/UI/notification synchronization
6. Allowed telemetry event -> privacy filter -> quality service

### 6. Concurrency policy

Define coroutine scopes, dispatcher boundaries, cancellation, long-running scan/service work, Flow exposure and error handling. UI scopes must not own work that must survive UI recreation.

### 7. Persistence policy

Define what is authoritative, cached, restorable or intentionally ephemeral across MediaStore, Room and DataStore. Queue/position persistence remains P1 under FR-08 and must not silently become a P0 scope expansion.

### 8. Navigation and overlay ownership

Define destinations, entry/exit rules, back behavior, Driving Mode entry, immediate exit and whether Voice feedback is an overlay/state rather than an independent deep navigation branch.

### 9. Error model

Define typed failures and recovery for permission denial, empty library, corrupt media, STT timeout, ambiguous artist, not found, Audio Focus loss, route loss and service errors. Logging must use non-sensitive codes and coarse context.

### 10. Testing architecture

Define unit, integration, UI/service and field-test seams for Intent mapping, queue/seek behavior, repositories, MediaSession/service synchronization, STT adapters, Audio Focus and route handling.

### 11. Module/package strategy

Start with the smallest structure that enforces approved boundaries. Additional Gradle modules require an explicit benefit such as independent ownership, build isolation, reusable contract or dependency enforcement. Avoid speculative module explosion.

## Approval checklist

- [ ] Approved User Flows and Wireframes are traceable to architecture paths.
- [ ] Context, component, state-ownership and critical data-flow views exist.
- [ ] Dependency direction and public contracts are explicit.
- [ ] Lifecycle, background and restoration behavior are explicit.
- [ ] Navigation and Voice overlay ownership are explicit.
- [ ] Privacy, telemetry and prohibited-data boundaries are explicit.
- [ ] Test seams and technical-spike results are recorded.
- [ ] ADR register is consistent; superseding decisions preserve history.
- [ ] Product, Android, Design and QA approve the baseline.

## Approved diagrams and decisions

_To be completed by Issue #16._
