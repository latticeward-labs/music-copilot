# Definition of Done — MVP v1.1

A task is Done only when every applicable condition below has evidence in its Issue or pull request.

## Product and scope

- Acceptance criteria and linked PRD/MVP requirement IDs are satisfied.
- No out-of-scope account, backend, streaming or advanced playlist behavior was introduced.
- Loading, empty, success, permission-denied, error and recovery states are handled.
- Driving copy is short and does not claim the app makes driving safe.
- A scope change has explicit Product Owner approval and updates the traceability matrix.

## Engineering

- Reviewed pull request; CI formatting, static analysis, unit tests and build pass.
- Canonical playback state remains owned by Media3 service, not UI lifecycle.
- Relevant Room migration, queue, intent or route logic has automated coverage.
- No secrets, raw voice, transcript, personal media metadata or commercial audio is committed/logged.
- Failure and recovery behavior is deterministic.
- Documentation is updated with behavioral/architectural changes.

## Voice and driving

- Voice follows `Idle → Listening → Processing → Confirming → Executed/Failed`.
- On-device recognition is preferred; any network-capable recognizer requires clear, revocable consent.
- Voice unavailable or consent denial/revocation preserves complete manual control.
- Playback ducks only during listening/response and returns predictably.
- Six-second timeout and no more than one retry are enforced.
- Low-confidence or multiple artist matches require one-step confirmation.
- TTS is at most one short sentence.
- No typing, deep menus or long lists are introduced in Driving Mode.
- Touch targets are at least 48dp, contrast/text scaling/TalkBack are checked.

## Playback and routes

- Screen-off/background/notification/lock-screen state is synchronized.
- Incoming call, headset disconnect, Bluetooth disconnect and Audio Focus scenarios pass.
- Bluetooth disconnect never causes unintended speaker playback.
- Corrupt media is skipped in a controlled way and queue playback can continue.
- Verification covers at least one representative physical device for affected work.

## Privacy and observability

- Only allowlisted events/properties are emitted.
- No raw voice, transcript, filename/title/artist/album or precise location appears in telemetry.
- Crash context is normalized and non-sensitive.
- Consent, data clearing, retention and disable paths are verified when affected.

## Release gates

A Beta release is Done only when:

- zero open P0 defects,
- serious-crash-free internal sessions ≥99%,
- golden voice scenario passes on at least 3 vehicle models,
- PR API 26/33/36 and Nightly API 29/31/34 evidence is complete; physical coverage includes an older Android device, Android 12/13, Android 16 where available, and representative Pixel, Samsung and Xiaomi devices,
- privacy copy and data-safety answers match behavior,
- telemetry dashboard is verified,
- signing custody, rollback, release notes and tester guide exist.
