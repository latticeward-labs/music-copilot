# Phase 7 — Android/System ownership contract

Canonical source: docs/wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json, precisely 20 system-only canonical IDs across 13 transition artifacts. Individual evidence is in system-only/*.md. The real owner/screen owner/recovery owner are copied exactly from the frozen Phase-5 register, not inferred or replaced.

## Strict control transfer

- Android permission dialogs: app explains one purpose and launches explicit OS request only when permitted by UF. Android owns UI, focus, prompts and result. On return fresh OS permission check governs Granted/requestable Denied/permanent Denied; manual playback always available.
- Android Settings: the user must intentionally open Android Settings outside Driving. OnResume alone is NOT proof of permission change; app rechecks actual state, returns to initiating safe surface and restores focus.
- Android notification permission and system media notification: Android owns permission/visibility/notification surface. MediaSessionService supplies only the accepted media state; if system surface is unavailable app's manual playback remains valid.
- Lock screen/screen off: no in-app copy of lock-screen controls. Session continues service-owned; app reconnect reads full actual PlaybackSnapshot on return, never constructs duplicate player.
- Headset/Bluetooth/external vehicle media keys: Android dispatches to MediaSession. No Android Auto app, no custom media-key screen or auto-launch Driving. Route loss yields Safe Paused where required and no surprising speaker resume.
- Audio focus, call interruption, Bluetooth/wired route failure: platform/focus owner and service adjudicate safe paused/eligible resume; UI merely reflects truth; Back/Exit remain usable.
- Speech provider/cloud-processing boundary: no fabricated provider UI or secret API claims. Consent in WF-10 is an app-owned privacy decision OUTSIDE Driving, whereas external provider processing/OS speech permission are separate system/provider handoffs.

## Handoff evidence for each of the 20 states

Each system-only/{ID}.md records: existing exact owner, screen owner and recovery owner, source transition(s), event trigger, external owner, returnPath, safe terminal, retry policy, platform TalkBack ownership, originating app focus return and privacy/data restrictions. App screenshot field MUST stay null for every system-only State.

## Review/QA gates

Native Android platform control behavior and OS focus cannot be proven by an app PNG or Markdown. Require real Android 10/12/14+ checks including at least Pixel, Samsung and Xiaomi plus approved external media route/vehicle QA. Evidence must be sanitized; consent/provider-specific legal sign-off separate.

**No system-only State becomes a new WF family, app destination, Android Auto, account or streaming feature.**