# Phase 7 — Privacy, telemetry and evidence minimization

**Authority:** Local-first Music Copilot MVP; accepted ADR-0003/0004/0006; Issue #16 privacy contract; Issue #17 Phase 6. Remote/cloud STT remains blocked until PH10-MIN-002 provider-specific approval.

## Explicitly prohibited data in analytics, QA evidence and crash reports

| Information | Telemetry | GitHub review, screenshots, logs |
|---|---|---|
| Raw microphone audio, waveform/persistent capture, complete utterance | PROHIBITED | PROHIBITED |
| Complete STT transcript, voice ambiguity token, current candidate labels or scores | PROHIBITED | PROHIBITED |
| Real local full file title, artist, path, content URI, album, playlist membership | PROHIBITED | PROHIBITED |
| GPS/precise location or precise position within music | PROHIBITED | PROHIBITED |
| Bluetooth hardware IDs, exact connected device names/addresses | PROHIBITED | PROHIBITED |
| Speech-provider raw payload/identifiers, logcat with media URIs | PROHIBITED | PROHIBITED |

Phase-4/5 images use synthetic example strings. For a real device test, load synthetic local media data before any screenshot/semantics dump. Do not copy real media titles/candidates into ticket bodies, CI logs, attachments, GitHub commits, chat summaries, analytics samples, screenshots or crash reports.

## Allowed only through the approved privacy gate

Aggregate command-success count, typed intent enum among the six supported commands, error/recovery classification, latency bucket and coarse approved device/version category. All events must pass a strict allowlist; never pass raw payload through a general logger, diagnostic event or exception. No identifiers/precise timestamps needed beyond expressly accepted aggregation.

## Lifecycle and consent

- Raw mic audio is transient for the explicit active session; no storage by default. The STT adapter must clean up and release audio on Cancel, Back, Exit and terminal failure.
- No complete transcript or personal candidate label persists in Room/DataStore or analytics, crash or QA evidence.
- Local library remains on the device; MediaStore is truth for files, Room is only rebuildable projection; clear-data confirmation must preserve media files per accepted behavior.
- Offline Voice/permission failure must not force cloud-processing, new sign-in, provider registration or Settings in active Driving Mode.
- Network speech requires separately approved provider/country/purpose/retention/deletion/transfer disclosure, explicit consent decision outside Driving and versioned local record. PH10-MIN-002 is still open.
- Android notification and lock screen are platform surfaces; limit what app shares through media metadata to the accepted MediaSession contract, while sanitizing diagnostic evidence independently.

## QA acceptance

1. Capture synthetic fixtures and verify no prohibited sensitive fields in UI-test screenshots, semantics dumps, instrumentation logs, telemetry event payloads or generated crash traces.
2. Test provider failure and Cancel cleanup under all Voice states. Assert no persisted audio/transcript/candidate and no retry data leak.
3. Simulate analytics failure: playback, voice fallback and Exit must continue, no block or alternative unreviewed logging.
4. Test consent decline, revoke and permanent Android denial: manual controls remain accessible, no forced provider processing.

**Design specification complete; execution/proof pending.**