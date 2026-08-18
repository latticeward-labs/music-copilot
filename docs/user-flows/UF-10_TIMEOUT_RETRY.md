# UF-10 - Voice Timeout, Unavailable Provider, and One Retry

**Cluster:** D - Voice certainty and recovery  
**Priority:** P0  
**Updated:** 18 August 2026  
**Status:** **PASS / COMPLETE at User Flow contract level**  
**Tracking:** Issues #3, #10, #12, #16

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-10 |
| User goal | Recover once from a bounded recognition failure and then continue manually without playback corruption. |
| Priority | P0 |
| User Story | US-07: one retry, then return to manual controls without stopping music. |
| Functional Requirements | FR-05 six-second listening, ducking, typed STT failure; FR-06 safe failure; privacy/consent requirements. |
| Screens and states | Listening attempt 1, Retry Available/Failed, Listening attempt 2, terminal Failed, Manual Fallback; provider/settings handoff outside active Driving Mode. |
| Influencing ADRs | ADR-0004 replaceable provider/fakes/one retry; ADR-0005 fail-safe mapping; ADR-0002 playback isolation. |
| Failure and recovery | First eligible recognition failure offers one retry. Second or non-retryable failure restores volume and manual controls. Playback/queue remain service-owned and unchanged. |
| Data and permissions | Typed coarse failure only; no audio/transcript/title/provider payload. Cloud mode requires prior separate consent/disclosure. |
| Open implementation decisions | Exact Android adapter callback/lifecycle and Settings deep link remain #10/#12/#16; retry count and safe return are fixed. |

## Eligible attempt-one failures

- six-second speech timeout;
- no recognition match;
- temporary provider unavailable/busy/disconnected;
- requested English model/language unavailable;
- network failure for a separately approved network-assisted provider;
- microphone/audio capture failure.

Permission denial is not retried coercively. Unknown/unsupported/not-found mapping does not become a recognition retry loop. Cancellation is terminal for the current session.

## Attempt and retry contract

1. Attempt one starts only after explicit mic action, ducks playback, and expires at six seconds.
2. An eligible failure stops capture, releases ducking, enters Failed with Retry Available, and keeps manual controls/Exit usable.
3. Retry requires one explicit user action; it never begins automatically.
4. Attempt two creates a fresh bounded Listening session with the same six-second maximum.
5. A successful attempt two proceeds to UF-08 or UF-09.
6. Any second failure is terminal: release ducking, give one short result, dismiss to the initiating surface, preserve service playback and queue, and keep manual controls available.
7. No third attempt, modal loop, automatic Settings launch, service restart, queue rebuild, or playback stop is allowed.

## Provider and language decision

| Capability result | Voice behavior |
|---|---|
| Proven on-device provider and English ready | Permit bounded Voice session; do not label every Android recognizer offline. |
| On-device provider exists but English requires download/is unavailable | Fail safely; model setup may be offered only outside active Driving Mode. |
| Generic/network recognizer with complete reviewed disclosure and separate consent | Permit only through the reviewed adapter; network failure follows one-retry rule. |
| Generic/network recognizer without disclosure or consent | Disable that mode; Manual Only. |
| No provider or capability check cannot establish support | Short unavailable result and Manual Only; no guessed provider mode. |
| Permission denied/revoked | Manual controls; recovery through UF-12 outside active driving context. |

## Invariants

- Playback does not pause, stop, advance, seek, restart, or rebuild because recognition failed.
- Ducking always ends on failure, cancellation, transition to confirmation, or completion.
- Retry count is session-scoped and resets only after a new explicit microphone action.
- A stale callback from an earlier attempt cannot execute a command in a later/closed attempt.
- TTS failure does not block manual recovery or change playback.
- Provider setup, model download, consent, and Android Settings are never forced while Driving Mode is active.

## Telemetry and accessibility

`intent_failed` may record a coarse allowlisted reason such as `timeout`, `no_match`, `provider_unavailable`, `language_unavailable`, `permission_denied`, `network`, `audio`, `cancelled`, `unknown`, `unsupported`, or `artist_not_found`. Exact exception text, transcript, audio, provider hypothesis, media metadata, and identifiers are prohibited.

Retry and Manual actions have distinct TalkBack labels and large targets. Countdown/state meaning is not color-only, and no rapid animation or long instruction competes with driving attention.

## Completion boundary

UF-10 closes timeout, typed availability failure, exactly-one-retry, and safe manual fallback at User Flow level. It does not claim a particular OEM provider exists, that speech is offline, or that road-noise/device accuracy has passed.
