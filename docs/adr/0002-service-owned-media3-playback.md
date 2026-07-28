# ADR-0002: Service-owned Media3 playback

- **Status:** Proposed
- **Date:** 2026-07-27
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Approval:** Pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)

## Proposed decision

Use Media3/ExoPlayer. Keep the player independent of UI lifecycle and manage playback in a service. The playback layer owns play/pause/next/previous/seek, queue, Media Session and Audio Focus.

Background, screen-off, notification, lock-screen and Bluetooth state must remain synchronized. Queue and position persistence after UI closure are P1 under FR-08.

## Validation

Screen-off playback, UI closure, notification controls, calls, headset/Bluetooth disconnect and route changes pass the v1.0 P0 matrix.
