# Issue 17 Phase 6 specialist — VO_CONFIRM_TWO

Canonical State: CD-10; source WF: WF-08; UF: UF-09; Golden Scenario: — (not on GS-01..05 path).
Source artwork or state contract: docs/high-fidelity/WF-08/states/HF_STATE_CD-10.png
SHA-256 PNG: 36f9c83fa145e399f0c58b425391b9ae3170d6b869b5427e7d6f77052d52f060.
Terminal: manual; authority: App + Voice coordinator.

## Exact on-screen UI copy

- "Which artist?"
- "Choose one presented candidate"
- "?"
- "1 · Aurora"
- "2 · Aurora Aksnes"
- "Neither"
- "Cancel"
- "Manual / Exit remains reachable"

## Binding behavior

- Reuses original WF-07 single-layer screen or WF-08 modal on Driving/Player origin; no navigation destination, permission dialog, list, keyboard, Android Auto, account or streaming.
- Source playback is MediaSessionService PlaybackSnapshot; never optimistic success or a second playback owner.
- VoiceSessionCoordinator owns active capture and confirmation; six English deterministic intents, manual push-to-talk, maximum six-second capture, exactly one eligible explicit STT retry.
- One confirmation only, with 1..2 immutable candidate labels from current Confirming.options. Neither and Cancel are recovery actions, not candidate objects.
- Unsupported/unknown/no artist/not found/confirmation-failed/execution-rejected/provider-unavailable return Manual; no approximated command and no additional prompt/Retry loop.
- Executed is shown only after acknowledged service command; a TTS failure does not undo playback success. Optional TTS output is no more than one short sentence.
- Driving Exit or Android Back immediately returns to the origin or Home/Library fallback without stopping service playback; Cancel releases capture/duck and discards stale callbacks.
- The conditional Play last affordance exists only after authoritative validation of one local item; when absent it is hidden and unsupported controls are inert.
- Visual touch bounds: 78dp Driving transport, 78dp Mic, 67dp Exit; all overlay controls at least 51dp high. Reflow and TalkBack require runtime QA.
- No raw audio, transcripts, local media titles, confidence or candidate labels in analytics or runtime diagnostic uploads.

## Review status

Deterministically generated specialist PNG/spec evidence for review. No human approval, Compose runtime QA or hardware test is claimed.
