# ADR-0002: Service-owned Media3 playback

- **Status:** Proposed
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.0 FR-03, FR-08 and section 09; MVP v1.0 US-03 and weeks 3-4

## Context

MVP playback must continue with the UI closed or the screen off and must remain synchronized with notifications, lock-screen controls, Bluetooth/head-unit commands, calls and audio-route changes.

## Decision

AndroidX Media3/ExoPlayer playback is owned by a `MediaSessionService`.

- The service owns the player instance, queue and canonical playback state.
- Activities, Compose screens and ViewModels never own ExoPlayer.
- UI and Voice send commands through a session/controller boundary and observe canonical state.
- The service owns MediaSession integration, notification behavior and audio-focus coordination.
- Queue/current item/position are checkpointed locally so state can be restored after process recreation where Android permits.
- Route loss must not cause unintended playback through the phone speaker.

## Consequences

### Positive

- Playback lifecycle is independent of UI lifecycle.
- One source of truth serves UI, notification, lock screen and Bluetooth controls.
- Lifecycle and route behavior can be integration-tested at a stable boundary.

### Trade-offs

- Service/controller synchronization adds implementation complexity.
- Process death and restoration require explicit persistence and tests.

## Rejected alternatives

- **Activity-owned player:** fails background and UI-closure requirements.
- **Separate players per surface:** permits state divergence and audio conflicts.
- **Foreground service without MediaSession ownership:** weakens system-control integration.

## Validation

The decision is satisfied only when screen-off playback, UI closure/reconnection, notification controls, call behavior, headset/Bluetooth disconnect and route transitions pass the P0 QA matrix.
