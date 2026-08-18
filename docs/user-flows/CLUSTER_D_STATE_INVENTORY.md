# Cluster D - Shared State Inventory

**Scope:** UF-08, UF-09, UF-10  
**Date:** 18 August 2026  
**Status:** **PASS - normalized CD-01 through CD-24**

## Global authority and persistence

- Voice session state is transient and owned by one `VoiceSessionCoordinator`-equivalent boundary.
- Playback/queue state is authoritative only in the existing Playback Service/MediaSession.
- STT output/confidence is transient input to the mapper and is never playback authority.
- Artist existence/identity comes from the verified local library projection; a candidate is revalidated before playback.
- Only consent/preferences, a bounded last-valid threshold, and approved coarse telemetry may persist. Raw audio, transcripts, candidates, artist/title metadata, and exact scores never persist in telemetry or evidence.
- Cancel, manual controls, and immediate Driving Mode Exit remain reachable from every non-terminal overlay state.

## Normalized inventory

| ID | Flow | State | Entry trigger / preconditions | Visible information / actions | Work, truth, persistence | Success transition | Failure / recovery | Analytics / accessibility / driving constraint |
|---|---|---|---|---|---|---|---|---|
| CD-01 | 08/10 | Idle/manual surface | Driving Mode or Full Player active | Manual controls, mic, Exit | MediaSession snapshot; no mic work | Mic -> CD-02 | Manual actions stay in UF-05/07 | No voice event; stable TalkBack order |
| CD-02 | 08/10 | Capability check | Explicit mic tap | Short pending cue; Cancel/manual/Exit | Check permission and approved provider mode; no capture yet | Ready -> CD-03 | Denied/unavailable -> CD-21/22 | `voice_button_pressed`; no auto Settings |
| CD-03 | 08/10 | Listening attempt 1 | Permission/provider ready | Listening + Cancel; manual/Exit reachable | Duck; transient capture; six-second timer | Result -> CD-05 | Eligible failure -> CD-17 | Mic state announced; no transcript display/log |
| CD-04 | 10 | Listening attempt 2 | Explicit Retry from CD-18 | Listening + Cancel; no Retry action | Fresh capture, duck, six-second timer; attempt=2 | Result -> CD-05 | Any failure -> CD-19 | Attempt count not a persistent identifier |
| CD-05 | 08/09 | Processing | STT result received; capture ended | Short Processing; Cancel/manual/Exit | Release duck; normalize/map exactly six intents | Certain -> CD-06; ambiguous -> CD-09 | Unknown/unsupported/not-found -> CD-13/14/15 | No transcript or exact score telemetry |
| CD-06 | 08 | Direct command pending | Certain supported result | Short pending; manual/Exit not blocked | Submit exactly one command to service | Ack -> CD-07 | Reject/stale -> CD-08 | `intent_detected` enum/bucket only |
| CD-07 | 08/09 | Executed | Service acknowledgement | Actual refreshed playback + one short result | MediaSession refresh is truth; optional TTS | Dismiss -> CD-01 | TTS fail remains CD-07 | `command_executed` enum/latency bucket; TTS non-authoritative |
| CD-08 | 08/09 | Execution failed-safe | Service rejection/stale item | Short failure; manual/Exit | Refresh actual service/library state | Dismiss -> CD-01 | If access issue, UF-02/03/05 | `intent_failed=execution_rejected`; no optimistic state |
| CD-09 | 09 | Confirming one candidate | Low-confidence supported intent | One short yes/select prompt; Cancel | Presented set is immutable for session | Clear select -> CD-11 | Reject/unclear/timeout -> CD-12 | One question, no keyboard/list |
| CD-10 | 09 | Confirming two artists | Two plausible stable candidates | At most two large choices + Neither/Cancel | Candidate set from deterministic resolver | Presented choice -> CD-11 | Neither/unclear/timeout -> CD-12 | Candidate names never telemetry/evidence |
| CD-11 | 09 | Confirmed command pending | One presented candidate selected | Short pending; no second prompt | Revalidate candidate; submit once | Ack -> CD-07 | Stale/reject -> CD-08 | No duplicate execution |
| CD-12 | 09 | Confirmation failed-safe | Reject, cancel, timeout, unclear answer | Short result; manual controls/Exit | End current session; no candidate expansion | Dismiss -> CD-01 | New voice requires new explicit mic | `intent_failed=confirmation_failed`; no loop |
| CD-13 | 09 | Artist not found | No plausible local match | One short Not Found; manual/Exit | No command; playback untouched | Dismiss -> CD-01 | User may recover manually | `intent_failed=artist_not_found`; never nearest-match |
| CD-14 | 09 | Unknown phrase | No supported grammar | Short not-understood result | No mapping or command | Dismiss -> CD-01 | New mic action optional | `intent_failed=unknown`; no guessed intent |
| CD-15 | 09 | Unsupported request | Known outside-six request | Short unsupported result | No scope expansion or command | Dismiss -> CD-01 | Manual supported controls remain | `intent_failed=unsupported`; no reinterpretation |
| CD-16 | 09 | Missing artist | `PLAY_ARTIST` grammar with blank slot | Short missing-artist result | No search/list/candidate | Dismiss -> CD-01 | Manual library/search after Exit | Coarse failure only; no prompt chain |
| CD-17 | 10 | Attempt-one failure | Timeout/no-match/provider/language/network/audio | Short failure, Retry and Manual/Exit | Release duck; preserve playback; retry budget=1 | Retry -> CD-04 | Manual/dismiss -> CD-20 | Coarse allowlisted reason; Retry large/labeled |
| CD-18 | 10 | Retry available | CD-17 fully settled | Retry once + Manual/Exit | Wait for explicit action; no auto capture | Retry -> CD-04 | Dismiss -> CD-20 | No countdown coercion |
| CD-19 | 10 | Attempt-two terminal failure | Any failure on attempt 2 | One short terminal result; manual/Exit | Release duck; retry budget exhausted | Dismiss -> CD-20 | New session requires new mic | Coarse failure only; no third attempt |
| CD-20 | 08/09/10 | Manual fallback | Any terminal Voice state | Initiating surface with manual controls | Playback/queue unchanged unless a command was acknowledged | Manual use or new mic -> CD-01/02 | Downstream access/route recovery as owned | Manual controls immediately usable |
| CD-21 | 10/12 | Permission denied/revoked | Capability check fails permission | Short permission state; Manual | No capture; Android permission is truth | Outside Driving Mode recovery -> UF-12 | Decline remains Manual | `permission_denied`; no coercion/auto Settings |
| CD-22 | 10/12 | Provider/language unavailable | No approved provider/English capability | Short unavailable state; Manual | No offline claim; no command | Outside-driving provider setup -> UF-12 | Stay Manual | Coarse provider/language reason; no identifiers |
| CD-23 | 10/12 | Cloud consent required/declined | Only network-assisted adapter available | Disclosure/consent only outside active Driving Mode | Local consent preference; provider statement separately reviewed | Consent + complete disclosure may enable future CD-02 | Decline -> Manual Only | No dark pattern; purpose/destination/retention readable |
| CD-24 | 08/09/10 | Cancelled/Exit | Cancel, Back, or Driving Exit at any pending state | Immediate return/exit | Cancel active recognizer, ignore stale callbacks, release duck | Return to initiating or non-driving surface | Playback unchanged | `intent_failed=cancelled` optional; no delay for telemetry |

## Coverage decision

CD-01 through CD-24 cover all required Listening, Processing, Confirming, Executed, Failed, one-retry, not-found, unavailable, manual fallback, privacy, accessibility, and navigation-return states. No state grants STT, the UI, TTS, remote config, or analytics authority over playback.
