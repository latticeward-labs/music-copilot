# UF-12 - Settings, Bluetooth Selection, Privacy, Consent, and Data Clearing

**Cluster:** E - Recovery and control  
**Priority:** P0 control surface with a separable P1 Bluetooth suggestion setup  
**Updated:** 18 August 2026  
**Status:** **PASS / COMPLETE at User Flow and executable privacy-contract level**  
**Tracking:** Issues #3, #12, #16

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-12 |
| User goal | Understand and control permissions, speech/diagnostic consent, selected Bluetooth suggestion, and clearable app-owned data. |
| Priority | P0 privacy/control; P1 selected-device Driving suggestion setup. |
| Product mapping | Privacy principles; FR-07 setup; Phase-0 D-014/D-016/D-025/D-026/D-028/D-029/D-030; TS-03/TS-10. |
| Screens and states | Settings Default/Updated; Android Settings return; Bluetooth selection; Privacy details; Consent accepted/declined; Clear selection/confirming/result. |
| Influencing ADRs | ADR-0003 ownership/clearability; ADR-0004 speech consent; ADR-0006 external processor boundary. |
| Failure and recovery | Recheck after system Settings; failed preference save remains previous value; partial provider deletion is visible/retryable; manual playback never removed. |
| Source of truth | Preferences repository for settings/consent, app-owned local repositories for clearable data, platform permission/provider state for availability. |
| Analytics | No settings values, device identities, titles, transcripts, or deletion payloads; only coarse allowlisted outcome if approved. |
| Open implementation evidence | Exact retention durations, final provider/legal copy, provider deletion SLA, and production repository wiring remain #12/#16. |

## Settings information architecture

1. **Bluetooth suggestion (P1):** select/remove one eligible device identity locally; connection may show a suggestion only and requires explicit acceptance. Device address/name is never telemetry.
2. **Permissions and speech mode:** show current media/microphone/notification/provider readiness. Android Settings opens only by explicit user action outside active Driving Mode; return always rechecks truth.
3. **Cloud speech consent:** show provider/destination, purpose, retention, and network use before a separate consent action. Decline or revoke means Manual Only for that mode, not loss of player controls.
4. **Diagnostics and privacy:** explain allowlisted coarse events, prohibited content, and non-authoritative/non-blocking behavior.
5. **Clear data:** independently select Short history and Diagnostic data, review exact categories, confirm, then show local/provider outcomes.

## Update path

1. Load observable current values from the preferences/platform boundaries.
2. User changes one setting.
3. Validate the setting and persist atomically; UI shows Updated only after acknowledgement.
4. On failure, retain the previous value and offer Retry without affecting playback.
5. For Android permission/provider changes, refresh on return rather than assuming success.

## Clear-data path

1. User opens Clear data and sees only approved clearable categories.
2. User selects one or more categories; protected categories are never implied or preselected.
3. A confirmation names the selected categories and explicitly states what remains.
4. Confirm executes an idempotent plan:
   - clear selected app-owned short history and/or queued local diagnostics;
   - request deletion from an approved external diagnostic processor only when its contract applies;
   - preserve Likes, Settings, Consents, library projection, queue/live playback, and user media files.
5. Report `Cleared`, `Partially cleared - action required`, or `Failed - retry available` per boundary. Never claim remote deletion from local success alone.
6. Cancel/back performs no deletion.

## Retention and consent rules

- The UI names data categories and source/owner; it does not invent days/months.
- A category needing retention cannot be collected until an approved positive duration is configured.
- Consent version, decision, and time may persist locally only as needed to prove the selected processing mode.
- Revocation disables future processing immediately and routes existing external data to the approved deletion contract.
- Raw audio, full transcript, personal media titles, complete library contents, precise location, contacts, and unnecessary identifiers are never eligible diagnostic fields.

## Accessibility and driving boundary

Settings has logical headings, stable TalkBack order, scalable text, 48dp targets, explicit selection/confirmation, and no color-only meaning. Consent, provider setup, data clearing, and Bluetooth selection are not performed inside active Driving Mode; Exit returns to the last non-driving surface first.

## Completion boundary

UF-12 closes visible states, selection/confirmation, ownership, safe failure, consent, privacy, and deletion semantics. It does not approve exact retention periods, a cloud processor, legal copy, or production telemetry implementation.
