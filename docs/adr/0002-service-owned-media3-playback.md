# ADR-0002: Service-owned Media3 playback

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of lifecycle boundaries, consequences and open evidence

## Context

PRD FR-02 and the MVP require playback to continue with the screen off and the UI closed while notification, lock-screen and Bluetooth controls reflect the true player state. Calls, Audio Focus, route changes and headset/Bluetooth disconnects must not create crashes or unintended speaker playback.

## Decision drivers

- Playback must outlive UI recreation and closure
- Android system controls require one authoritative MediaSession state
- Interruption and route behavior must be deterministic
- The design must be testable across OEMs, Android versions and vehicles

## Decision

Use AndroidX Media3/ExoPlayer. Playback and MediaSession ownership reside in an Android service independent of UI lifecycle. The playback boundary owns player state, play/pause/next/previous/seek, queue behavior, MediaSession integration and Audio Focus policy. UI surfaces observe state and send commands; they do not own the player.

Queue and position restoration after UI closure remain P1 under FR-08 and must not silently become a P0 release requirement.

The exact service class, controller contract, process-death restoration policy, coroutine scopes and error hierarchy are intentionally deferred to [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16).

## Consequences

### Positive

- One source of truth for app and system playback surfaces
- Background and screen-off behavior are independent of screen lifecycle
- Better isolation for service/UI synchronization and interruption tests

### Costs and risks

- Service lifecycle, notification and controller connection handling add complexity
- OEM background restrictions and route behavior require field validation
- Poor command ordering or restoration can create stale UI or duplicate playback

## Alternatives considered

- **UI-owned ExoPlayer:** rejected because UI closure or recreation must not terminate or duplicate playback.
- **Multiple player instances:** rejected because they fragment state and risk simultaneous playback.
- **Custom playback engine:** rejected because Media3 supplies required Android media/session integration.
- **Making queue restoration P0:** rejected because FR-08 is P1.

## Validation and open evidence

- Screen-off and UI-closure playback
- Notification, lock-screen and Bluetooth command/state synchronization
- Calls, Audio Focus, headset/Bluetooth disconnect and route-change scenarios
- No unintended speaker playback and no duplicate player instance
- Issue #16 spike: service/controller lifecycle, process-death limits and command serialization
