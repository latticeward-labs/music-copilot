# Analytics and Privacy

## Principle

Collect only data that answers a documented product or reliability question. Analytics must not become a shadow user database.

## Initial event allowlist

| Event | Product question | Allowed properties |
|---|---|---|
| `app_open` | Is the test build being used? | app version, platform |
| `onboarding_completed` | Can testers finish onboarding? | flow version |
| `search_submitted` | Is search used? | result-count bucket; never raw query |
| `track_play_started` | Can users start playback? | anonymous catalog item ID, source type |
| `track_play_completed` | Does playback complete? | duration bucket |
| `playlist_created` | Is library organization useful? | none |
| `playback_error` | Where does playback fail? | normalized error code |
| `feedback_opened` | Can testers reach feedback? | entry point |

## Prohibited data

Do not send:

- passwords or authentication tokens,
- email addresses, names, phone numbers, or precise location,
- raw search queries,
- playlist names,
- message content,
- device advertising ID unless explicitly approved,
- audio content or microphone recordings,
- free-form exception text containing user data.

## Governance

Every new event requires:

1. a documented question,
2. an owner,
3. an allowed property list,
4. a retention decision,
5. a verification step,
6. privacy review before release.

Analytics documentation and actual implementation must stay synchronized.
