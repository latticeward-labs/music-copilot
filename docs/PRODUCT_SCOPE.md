# Product Scope — MVP

## Product promise

Music Copilot should help a listener discover and organize music with less friction while providing a foundation for useful, transparent intelligent assistance.

## Target user

For the first validation cycle, the target user is a mobile listener who:

- regularly searches for music,
- saves favorites and playlists,
- wants lightweight recommendations or assistance,
- accepts an early Android test build.

This is a validation target, not a permanent market limitation.

## In scope

1. Onboarding and basic account access
2. Home surface with approved sample content
3. Search across the available MVP catalog
4. Player controls: play, pause, seek, next, previous
5. Like and unlike
6. Create, rename, and view playlists
7. Add and remove tracks from playlists
8. Basic profile and settings
9. Crash reporting
10. Minimal anonymous product analytics
11. Android internal test distribution

## Out of scope

- A Spotify-scale commercial catalog
- Unlicensed playback, downloading, or redistribution
- Social feeds and direct messaging
- Artist payouts and royalty accounting
- Advertising marketplace
- Public creator uploads
- Advanced generative music
- Desktop and iOS production releases
- Full subscription billing

## MVP success signals

Before implementation, the product owner must assign numeric targets and a test window to:

- onboarding completion,
- successful first playback,
- search-to-play conversion,
- playlist creation,
- crash-free sessions,
- tester retention,
- qualitative tester satisfaction.

## Product constraints

- Use only owned, licensed, public-domain, or explicitly authorized audio.
- Collect the minimum telemetry required to answer a documented product question.
- Never log passwords, access tokens, email addresses, raw search text, or playlist names.
- Every MVP feature needs acceptance criteria and a measurable reason to exist.

## Exit criteria

The MVP is ready for internal testing when:

- the core playback journey works on supported Android devices,
- critical and high-severity defects are resolved,
- privacy disclosures match actual telemetry,
- Crashlytics and analytics are verified in a non-production environment,
- release notes and a tester feedback channel exist.
