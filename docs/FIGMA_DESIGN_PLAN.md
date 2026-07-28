# Figma Design Plan - Music Copilot MVP v1.0

- **Status:** In Progress
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Workfile:** [Music Copilot - MVP v1.0](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Product sources:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

## Objective

Design the ten v1.0 screens and the local-music, Driving and push-to-talk flows before implementation expands.

## Required flow

First use: two-slide introduction -> file and microphone permissions -> scan -> six-command education -> Home.

Voice: Driving Mode -> microphone tap -> Listening -> Processing -> direct execution or Confirming -> Executed/Failed.

## Screen inventory

1. Splash - Loading, Migration Error
2. Onboarding - First Run, Skip
3. Permissions - file, microphone and notification; Granted, Denied, Permanently Denied
4. Home / Library - Scanning, Empty, Ready
5. Search - No Result, Result, Error
6. Full Player - Playing, Paused, Corrupt File
7. Driving Mode - Playing, Paused, No Track
8. Voice Overlay - Listening, Confirm, Failed
9. Liked Songs - Empty, Ready
10. Settings - data, Bluetooth and Privacy; Default, Updated

## Driving design rules

- One layer
- At least 48dp touch targets; primary controls preferably larger
- Dark, high-contrast theme
- Readable typography and limited motion
- Four primary controls, microphone, current title and immediate exit
- No typing, nested menu, deep navigation or long results list
- Short TTS and error copy
- Complete TalkBack labels and scalable text
- Do not claim the app makes driving safe; remind users that road attention remains their responsibility

## Required prototype

Vehicle connected -> Driving Mode -> microphone tap -> “Next song” -> next track starts under the 2.5-second P95 target -> title updates -> one short response -> no extra screen.
