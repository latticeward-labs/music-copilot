# UF-01 - First Launch and Education

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking  
**Date:** 01 August 2026  
**Status:** Complete for Cluster A review; final overall User Flow approval remains pending Issue #2 alignment and completion of UF-04 through UF-13  
**Tracking:** GitHub Issue #3  
**Figma:** [UF-01 - First Launch and Education](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-26)

## Authority and boundary

This flow is derived from the attached English PRD v1.0 and English MVP v1.0. The proposed ADRs, System Architecture, Technical Blueprint, GitHub, and Figma narrow technical and workflow interpretation but do not add product scope.

UF-01 defines first-entry behavior, onboarding, handoff to permission and scan flows, optional command education, and valid readiness endpoints. It does not approve low-fidelity Wireframes, final visual design, navigation implementation, ViewModel boundaries, storage representation, or application code.

## Product mapping

- PRD first-use journey: two-slide value introduction -> permissions -> scan -> six-command education -> Home.
- MVP US-01: permissions are requested at the appropriate moment and denial does not crash the app.
- MVP US-02: local files become playable without account creation.
- Phase 0 decisions: D-004, D-005, D-006, D-007, D-008, D-009, D-014, and D-020.

## Start states

1. Fresh installation with no onboarding-completion flag.
2. Returning user whose onboarding was interrupted or remains incomplete.
3. Explicit education replay from Settings.
4. Recoverable Splash preparation or migration failure.
5. Returning user with an existing Home / Library state.

## Primary flow

1. **Launch -> Splash - Loading.**
   - Validate local schema/repositories, Android permission state, service connection, and next destination.
   - Splash never fabricates library or playback state.
2. **Startup preparation decision.**
   - Success continues routing.
   - Recoverable migration/preparation failure shows a concise Retry state.
3. **Onboarding-completion decision.**
   - Completed users route to the appropriate Home / Library state.
   - An active playback service is observed, not restarted.
   - Incomplete users enter the two-slide introduction.
4. **Onboarding Slide 1.**
   - Explain the value of controlling user-authorized local music with fewer manual interactions.
5. **Onboarding Slide 2.**
   - Show the road-attention warning.
   - Explain Push-to-Talk without claiming that the product makes driving safe.
   - Do not imply streaming, account, sync, or supplied/licensed music.
6. **Continue or Skip.**
   - Continue enters the contextual media-access explanation before the first scan.
   - Skip records onboarding completion locally, does not request microphone or notification access, and routes to media setup or Home / Library recovery.
7. **Handoff to UF-02 and UF-03.**
   - UF-02 owns permission outcomes and Android Settings recovery.
   - UF-03 owns Scanning, Empty, Ready, scan error, and revocation reconciliation.
8. **Return from readiness cluster.**
   - A valid Ready or Empty state may continue to six-command education.
9. **Six-command education.**
   - `PLAY`
   - `PAUSE`
   - `NEXT`
   - `PREVIOUS`
   - `SEEK_BACK`
   - `PLAY_ARTIST`
10. **Optional command practice.**
    - Practice is user-selected.
    - Microphone permission is requested just-in-time through UF-02 only after explicit microphone action.
    - Skipping practice does not request microphone access and education remains replayable from Settings.

## Valid terminal states

- **Home / Library - Ready:** verified accessible local media with Songs, Artists, Albums, and Folders internal views.
- **Home / Library - Empty:** valid non-error state with local-file explanation, Rescan, and permission recovery where relevant.
- **Permission recovery:** Denied with Retry or Permanently Denied with explicit Android Settings path.
- **Startup recovery:** recoverable Splash preparation/migration failure with Retry.
- **Manual controls preserved:** microphone declined or unavailable without blocking local browsing or playback.

## Fixed rules

- Onboarding contains two value slides and is both skippable and replayable.
- Media permission is explained and requested before the first scan.
- Microphone permission is never requested blindly at app start.
- Notification permission is deferred until playback/background-control value exists; detailed behavior belongs to UF-06 and TS-01.
- Home / Library is one top-level destination with internal Songs, Artists, Albums, and Folders views.
- No account, authentication, backend, sync, catalog, streaming, upload, download, or forced cloud dependency is introduced.
- User-facing copy must not claim guaranteed offline speech recognition.

## Persistence and ownership

- Onboarding completion is local application state.
- Android permission state is authoritative for access.
- MediaStore is authoritative for accessible device media.
- The local library index is derived and rebuildable.
- Replay from Settings does not reset the library.

## Privacy and analytics

- No raw audio, full transcript, full title, filename, artist, album, folder, URI, path, precise location, contacts, or unnecessary identifier may enter analytics, crash reports, or logs.
- The current Phase 0 analytics allowlist does not define onboarding, permission, or scan events. UF-01 introduces no new telemetry event without separate Privacy review and allowlist update.

## Accessibility

- Scalable text and logical TalkBack reading order.
- Clear Continue, Skip, Retry, and recovery labels.
- No state relies on color alone.
- Road-attention copy is explicit and concise.
- No coercive permission loop.

## Review result

- Product scope and user-visible behavior: approved by the Product Owner through the 01 August 2026 Phase 1 instruction.
- Product Design: flow and terminal-state coverage completed in Figma.
- Android, QA, and Privacy: constraints recorded; routed evidence remains required where identified by Phase 0.
- This flow does not close Issue #3 or authorize implementation.
