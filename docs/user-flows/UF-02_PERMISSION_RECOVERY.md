# UF-02 - Permission Denial and Settings Recovery

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking and privacy critical  
**Date:** 01 August 2026  
**Status:** Complete for Cluster A review; exact notification behavior remains routed to TS-01 and UF-06  
**Tracking:** GitHub Issue #3  
**Figma:** [UF-02 - Permission Recovery](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-27)

## Authority and boundary

This flow defines the user-visible contract for media, microphone, and notification permission handling. Android owns the platform permission state. The application owns the contextual explanation, recovery path, Settings handoff, and safe fallback.

It does not invent Android-version behavior that Phase 0 routed to technical evidence. It does not approve provider selection, storage mechanics, or implementation code.

## Product mapping

- MVP US-01: the user understands why access is needed; denial does not crash the app and a Settings path is available.
- PRD privacy principles: microphone permission is requested when needed; raw audio is not stored by default.
- Phase 0 decisions: D-006, D-007, D-008, D-009, D-014, D-016, D-020, D-028, and D-030.
- Technical spikes: TS-01, TS-03, and TS-06.

## Global permission rules

1. One purpose is explained at a time.
2. No permission is treated as consent for unrelated data collection.
3. No coercive request loop is allowed.
4. `Denied` and `Permanently Denied` are separate states.
5. Returning from Android Settings always rechecks the relevant condition.
6. A denial must preserve a non-crashing recovery path or manual fallback.
7. Deep Android Settings are never opened automatically.

## A. Media access

### Trigger

- The first consented local-library scan is about to begin.
- A previously denied request is retried by the user.
- Access was restored or revoked and the app must re-evaluate the blocked action.

### Flow

1. **Media-access explanation.**
   - Explain that Music Copilot accesses supported audio already present on the device.
   - Do not imply that the app supplies, licenses, verifies ownership of, uploads, or synchronizes the files.
2. **User selects Continue.**
3. **Android media permission dialog.**
4. **Granted.**
   - Continue the blocked action: initial scan, incremental scan, or media resolution.
5. **Denied.**
   - Return to a concise app explanation.
   - Offer `Retry` and `Not now`.
   - `Not now` routes to Home / Library Empty with a visible media-access recovery path.
6. **Permanently Denied.**
   - Offer `Open Android Settings` only after explicit user action.
7. **Android Settings.**
   - The user may grant access or leave it denied.
8. **Return to app.**
   - Re-read Android permission state.
   - Granted resumes the blocked action.
   - Still denied returns to explanation, Retry, or the safe Empty/manual path.

## B. Microphone access

### Trigger

Microphone permission may be requested only after:

- the first explicit microphone tap; or
- user-selected controlled command practice.

It is not requested blindly during startup or merely because onboarding is displayed.

### Flow

1. Explicit microphone action.
2. Voice-purpose explanation.
3. Android microphone permission dialog.
4. **Granted:** begin the bounded Voice session.
5. **Denied:** return immediately to manual controls; local browsing and playback remain available.
6. **Permanently Denied:** offer Android Settings only outside active Driving Mode.
7. Declining microphone or cloud speech consent never removes manual player controls.

### Voice privacy boundary

- Raw microphone audio is not stored by default.
- Raw audio and full transcripts do not enter analytics, crash reports, logs, or persistence.
- If cloud speech processing is used, separate consent must disclose destination, purpose, and retention.
- The product must not claim guaranteed offline STT without validated provider evidence.

## C. Notification permission boundary

Notification permission is deferred until:

- playback has succeeded; and
- background/system-control value exists; and
- the Android version actually requires the applicable prompt.

The Phase 1 contract is limited to:

1. show an app-level explanation after value exists;
2. request the Android permission only where applicable;
3. ensure denial does not crash, stop playback, or falsify player state;
4. keep manual in-app controls available; and
5. route exact Android 10, 12, and 14+ behavior to UF-06 and TS-01.

## D. Permission revocation

### Trigger

A permission that was previously granted is revoked while:

- the app resumes;
- a scan begins;
- a media item is resolved; or
- an existing library state is reconciled.

### Required behavior

1. Recheck fails.
2. Reconcile the derived index against current MediaStore access.
3. Mark inaccessible media unavailable.
4. Do not treat a Room/index row as proof that the media is still accessible.
5. If the current item is affected, do not crash or leave it falsely playable.
6. Route to media permission recovery.
7. Reconnect or restored access does not bypass a fresh state check.

## Valid terminal states

- Granted and blocked action continued.
- Denied with Retry and `Not now`.
- Permanently Denied with explicit Android Settings path.
- Still denied after Settings return with explanation/manual fallback.
- Microphone declined with manual controls preserved.
- Notification denied without playback-state corruption.
- Revoked media reconciled and routed to recovery.

## Ownership and persistence

- Android permission state is authoritative.
- App explanation state is temporary UI state.
- MediaStore is authoritative for accessible media existence and platform identity.
- The local index is derived and rebuildable.
- Permission results do not create a user identity or account.

## Privacy and analytics

The approved Phase 0 event allowlist does not include permission-result telemetry. UF-02 adds no permission event or sensitive payload. Any future instrumentation requires an explicit allowlist revision and Privacy review.

Forbidden data includes raw audio, full transcripts, full personal titles, filenames, artists, albums, folders, paths, URIs, library contents, precise location, contacts, and unnecessary persistent identifiers.

## Accessibility and safety

- Purpose copy is short and specific.
- Retry, Not now, Open Settings, and manual fallback are explicitly labeled.
- No state relies on color alone.
- TalkBack order and scalable text are required.
- Android Settings are not opened from active Driving Mode.

## Open evidence gates

- **TS-01:** notification permission and foreground media behavior on Android 10, 12, and 14+.
- **TS-03:** provider availability, network dependence, processing destination, and consent copy.
- **TS-06:** permission changes, scoped storage, moved/deleted files, and reconciliation across Android versions.

## Review result

- Product behavior and recovery policy: approved for this cluster by the Product Owner on 01 August 2026.
- Product Design: all denial, permanent denial, Settings return, revocation, and fallback branches are represented in Figma.
- Android, QA, and Privacy: formal evidence remains required for the routed spikes.
- This flow does not close Issue #3 or authorize implementation.
