# UF-02 - Permission Denial and Settings Recovery

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking and privacy critical  
**Updated:** 04 August 2026  
**Status:** Stage 1 Traceability Matrix — PASS for this flow; TS-01, TS-03, and TS-06 remain open  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-02_PERMISSION_DENIAL_AND_SETTINGS_RECOVERY.md`](mermaid/UF-02_PERMISSION_DENIAL_AND_SETTINGS_RECOVERY.md)  
**Figma reference:** [UF-02 - Permission Recovery](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-27) — synchronization with the corrected repository record remains open

## Authority and boundary

UF-02 defines the user-visible contract for media, microphone, later notification permission, Android Settings recovery, speech-provider availability, and manual fallback. Android owns platform permission state. The application owns contextual explanation, the blocked action, recovery routing, and truthful fallback.

It does not invent version-specific behavior routed to Technical Spikes, choose a speech provider, or treat documentation completeness as implementation evidence.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-02 |
| User goal | Recover safely from media, microphone, and later notification denial without a crash, coercive loop, or loss of manual playback control. |
| Priority | P0 |
| User Story | US-01 |
| Functional Requirement | **FR-01 partial** — media-access precondition/recovery; FR-05 microphone precondition only — Voice execution remains UF-08/UF-10. |
| Screens and states | S-03; CA-02; SYS-01/02/03/04/05; Home / Library Empty/manual fallback. |
| Influencing ADRs | ADR-0001 Android permission surfaces; ADR-0002 playback remains service-owned/truthful; ADR-0003 revocation reconciliation; ADR-0004 provider/consent/failure isolation; ADR-0006 no forced cloud/backend. All remain Proposed pending Issue #2. |
| Failure and recovery | Denied-requestable: Retry/Not now; permanent denial: Open Settings/Not now; Settings return: three outcomes; provider unavailable and consent decline: manual fallback; revocation: reconcile false playability. |
| Data and permissions | Android permission state authoritative; MediaStore existence separated from derived index; microphone requested JIT; notification deferred; cloud consent stored locally only if applicable. |
| Open assumptions or evidence | TS-01 notification behavior; TS-03 provider/cloud behavior; TS-06 storage/revocation behavior; Issue #2 ADR outcomes; Figma synchronization; final reviewer sign-off. |

## Global rules

1. Explain one purpose at a time.
2. Request a permission only when its capability is first needed.
3. Do not treat a permission result as consent for unrelated processing.
4. Keep `Denied - requestable` and `Permanently Denied` distinct.
5. Never open Android Settings automatically or create a coercive retry loop.
6. Recheck the actual condition whenever the app returns from Settings.
7. Preserve a non-crashing recovery or manual fallback after every denial/failure.

## Media access

1. Show a contextual explanation before the first consented scan or explicit retry.
2. `Continue` opens the Android media permission dialog; `Not now` goes to a safe Home / Library Empty state with visible recovery.
3. `Granted` resumes the blocked scan/reconciliation action.
4. `Denied - requestable` offers `Retry` and `Not now`.
5. `Permanently Denied` offers both `Open Android Settings` and `Not now`.
6. Returning from Android Settings rechecks one of three independent outcomes:
   - `Granted` → resume the blocked action;
   - `Denied - requestable` → show Retry/Not now recovery;
   - `Still permanently denied` → show Open Settings/Not now without an automatic loop.

## Microphone, provider, and cloud-processing boundary

Microphone permission may be requested only after an explicit microphone tap or user-selected command practice.

The checks are ordered and distinct:

1. microphone permission;
2. speech-provider availability and capability;
3. cloud-processing determination;
4. separate truthful cloud consent, if applicable;
5. bounded Voice handoff or manual fallback.

`Provider unavailable` is not a permission denial. Microphone denial, provider unavailability, network/provider failure, or cloud-consent decline must leave local browsing, manual controls, the queue, and authoritative playback available. The app must not claim guaranteed offline speech recognition without validated TS-03 evidence.

## Notification boundary

Notification permission remains deferred until successful playback/background-control value exists and the Android version requires the prompt. Exact Android 10, 12, and 14+ behavior belongs to UF-06 and TS-01. Denial must not crash, stop playback, or falsify player state.

## Revocation and reconciliation

When previously granted media access is revoked, recheck Android state, reconcile the derived index with current MediaStore access, mark affected media unavailable, prevent false playability, preserve unaffected service-owned playback state where valid, and route to recovery. A stale index row never proves current accessibility.

## Valid endpoints

- Granted and blocked action continued.
- Denied - requestable with Retry and Not now.
- Permanently Denied with Open Android Settings and Not now.
- Any of the three Settings-return results routed correctly.
- Provider unavailable or cloud consent declined with manual controls preserved.
- Notification denied without playback-state corruption.
- Revoked media reconciled and routed safely.

## Privacy and telemetry

- Raw audio is not stored by default.
- Raw audio, full transcripts, titles, filenames, artists, albums, folders, URIs, paths, library contents, precise location, contacts, and unnecessary identifiers do not enter analytics, crash reports, or logs.
- If cloud speech is used, consent must disclose destination, purpose, and retention before processing.
- This flow approves no permission-result telemetry event.

## Review boundary

- Stage 1 traceability completeness: **PASS**.
- TS-01, TS-03, and TS-06: **Open**.
- Product Design/Figma synchronization: **Open**.
- ADR governance: **Open; all six ADRs remain Proposed**.
- Final Product, Design, Android, QA, Privacy, and Governance sign-off: **Not recorded**.
- This record does not close Issue #3 or authorize implementation.
