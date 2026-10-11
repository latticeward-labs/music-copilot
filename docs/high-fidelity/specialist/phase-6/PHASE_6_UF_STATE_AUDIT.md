# Issue 17 Phase 6 — Complete UF-07..UF-10 State Audit

42 canonical states, all assigned to a specialist rendered case or the existing Phase-5 component/system handoff.
A State ID may legitimately share an approved visual or be an annotation, not a new destination.

| ID | State | UF | Phase-5 disposition | Phase-6 specialist/owner evidence | Review conclusion |
|---|---|---|---|---|---|
| CC-01 | Non-driving source | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-01.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-02 | Manual entry pending | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-02.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-03 | Controller reconnecting | UF-07 | HIFI_VARIANT_ANNOTATION | [DRV_RECONNECT](DRV_RECONNECT.png) | SPECIALIST_VISUAL |
| CC-04 | Entry cancelled/failed | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-04.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-05 | Driving Playing | UF-07 | DEDICATED_HIFI | [DRV_PLAYING](DRV_PLAYING.png) | SPECIALIST_VISUAL |
| CC-06 | Driving Paused/Ended | UF-07 | DEDICATED_HIFI | [DRV_PAUSED](DRV_PAUSED.png) | SPECIALIST_VISUAL |
| CC-07 | Driving No Track | UF-07 | DEDICATED_HIFI | [DRV_NO_TRACK](DRV_NO_TRACK.png) / [DRV_LAST_VERIFIED](DRV_LAST_VERIFIED.png) | SPECIALIST_VISUAL |
| CC-08 | Playback command pending | UF-07 | HIFI_VARIANT_ANNOTATION | [DRV_COMMAND_PENDING](DRV_COMMAND_PENDING.png) | SPECIALIST_VISUAL |
| CC-09 | Command rejected/timeout | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-09.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-10 | Acknowledged refresh | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-10.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-11 | P1 selected-device connected | UF-07 / UF-12 | DEDICATED_HIFI | [Existing Phase-5 owner spec](../../WF-07/states/HF_STATE_CC-11.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-12 | Bluetooth suggestion accepted | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-12.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-13 | Exit/back return | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-13.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-14 | Microphone handoff | UF-07 → UF-08 | DEDICATED_HIFI | [Existing Phase-5 owner spec](../../WF-08/states/HF_STATE_CC-14.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-15 | Item unavailable / access failure | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-15.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-16 | Interruption/route handoff | UF-07 → UF-11 | SYSTEM_ONLY_HANDOFF | [DRV_ROUTE_SAFE](DRV_ROUTE_SAFE.png) | SPECIALIST_VISUAL |
| CC-17 | Suggestion declined/dismissed | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-17.md) | APPROVED_VARIANT_OR_HANDOFF |
| CC-18 | Analytics unavailable | UF-07 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CC-18.md) | APPROVED_VARIANT_OR_HANDOFF |
| CD-01 | Idle/manual surface | UF-08 / UF-10 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CD-01.md) | APPROVED_VARIANT_OR_HANDOFF |
| CD-02 | Capability check | UF-08 / UF-10 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CD-02.md) | APPROVED_VARIANT_OR_HANDOFF |
| CD-03 | Listening attempt 1 | UF-08 | DEDICATED_HIFI | [VO_LISTEN_1](VO_LISTEN_1.png) | SPECIALIST_VISUAL |
| CD-04 | Listening attempt 2 | UF-10 | DEDICATED_HIFI | [VO_LISTEN_2](VO_LISTEN_2.png) | SPECIALIST_VISUAL |
| CD-05 | Processing | UF-08 | DEDICATED_HIFI | [VO_PROCESSING](VO_PROCESSING.png) | SPECIALIST_VISUAL |
| CD-06 | Direct command pending | UF-08 | DEDICATED_HIFI | [VO_COMMAND_PENDING](VO_COMMAND_PENDING.png) | SPECIALIST_VISUAL |
| CD-07 | Executed | UF-08 | DEDICATED_HIFI | [VO_EXECUTED](VO_EXECUTED.png) / [VO_TTS_UNAVAILABLE](VO_TTS_UNAVAILABLE.png) | SPECIALIST_VISUAL |
| CD-08 | Execution failed-safe | UF-08 | DEDICATED_HIFI | [VO_EXECUTION_REJECTED](VO_EXECUTION_REJECTED.png) | SPECIALIST_VISUAL |
| CD-09 | Confirming one candidate | UF-09 | DEDICATED_HIFI | [VO_CONFIRM_ONE](VO_CONFIRM_ONE.png) | SPECIALIST_VISUAL |
| CD-10 | Confirming two artists | UF-09 | DEDICATED_HIFI | [VO_CONFIRM_TWO](VO_CONFIRM_TWO.png) | SPECIALIST_VISUAL |
| CD-11 | Confirmed command pending | UF-09 | DEDICATED_HIFI | [Existing Phase-5 owner spec](../../WF-08/states/HF_STATE_CD-11.md) | APPROVED_VARIANT_OR_HANDOFF |
| CD-12 | Confirmation failed-safe | UF-09 | DEDICATED_HIFI | [VO_CONFIRMATION_FAILED](VO_CONFIRMATION_FAILED.png) | SPECIALIST_VISUAL |
| CD-13 | Artist not found | UF-09 | DEDICATED_HIFI | [VO_ARTIST_NOT_FOUND](VO_ARTIST_NOT_FOUND.png) | SPECIALIST_VISUAL |
| CD-14 | Unknown phrase | UF-09 | DEDICATED_HIFI | [VO_UNKNOWN_PHRASE](VO_UNKNOWN_PHRASE.png) | SPECIALIST_VISUAL |
| CD-15 | Unsupported request | UF-09 | DEDICATED_HIFI | [VO_UNSUPPORTED](VO_UNSUPPORTED.png) | SPECIALIST_VISUAL |
| CD-16 | Missing artist | UF-09 | DEDICATED_HIFI | [VO_MISSING_ARTIST](VO_MISSING_ARTIST.png) | SPECIALIST_VISUAL |
| CD-17 | Attempt-one failure | UF-10 | DEDICATED_HIFI | [VO_STT_TIMEOUT](VO_STT_TIMEOUT.png) | SPECIALIST_VISUAL |
| CD-18 | Retry available | UF-10 | DEDICATED_HIFI | [VO_RETRY_AVAILABLE](VO_RETRY_AVAILABLE.png) | SPECIALIST_VISUAL |
| CD-19 | Attempt-two terminal failure | UF-10 | DEDICATED_HIFI | [VO_ATTEMPT2_TERMINAL](VO_ATTEMPT2_TERMINAL.png) | SPECIALIST_VISUAL |
| CD-20 | Manual fallback | UF-08 / UF-09 / UF-10 | HIFI_VARIANT_ANNOTATION | [VO_MANUAL_ONLY](VO_MANUAL_ONLY.png) | SPECIALIST_VISUAL |
| CD-21 | Permission denied/revoked | UF-10 / UF-12 | HIFI_VARIANT_ANNOTATION | [VO_PERMISSION_DENIED](VO_PERMISSION_DENIED.png) | SPECIALIST_VISUAL |
| CD-22 | Provider/language unavailable | UF-10 / UF-12 | HIFI_VARIANT_ANNOTATION | [VO_PROVIDER_UNAVAILABLE](VO_PROVIDER_UNAVAILABLE.png) | SPECIALIST_VISUAL |
| CD-23 | Cloud consent required/declined | UF-10 / UF-12 | DEDICATED_HIFI | [Existing Phase-5 owner spec](../../WF-10/states/HF_STATE_CD-23.md) | APPROVED_VARIANT_OR_HANDOFF |
| CD-24 | Cancelled/Exit | UF-08 / UF-09 / UF-10 | HIFI_VARIANT_ANNOTATION | [Existing Phase-5 owner spec](../../state-variants/CD-24.md) | APPROVED_VARIANT_OR_HANDOFF |

## Non-visual approved specialties

- CC-01/02/04/09/10/12/13/15/17/18 reuse the existing Driving entry/snapshot/Exit variants and do not create intermediate destinations.
- CC-11 is optional P1 Bluetooth suggestion, must occur **outside** Driving and never enter automatically.
- CC-14 maps to explicit Mic → Listening; CC-16 is owned by Android route/focus and MediaSession service, not an app/system fake page.
- CD-01/02/11/24 reuse manual/capability/pending/cancel variants and preserve source generation ownership.
- CD-21/22 are capability-failure variants with immediate Manual, not Settings/consent inside Driving.
- CD-23 cloud consent is outside Driving in WF-10; never in Voice/Driving modal.

## Acceptance and review boundary

0 missing, 0 duplicate, 0 unassigned across 18 CC states + 24 CD states.
Static review/artifact gate PASSED. Device-level proof of manual touch reachability, immediate Back/Exit, provider behavior, focus, TTS, TalkBack, 200% text and driving distraction still requires manual QA.
