# Issue #9 — FINAL P13 owner-phone QA checklist (PREPARED; NOT EXECUTED)

> **Status:** DRAFT FOR FINAL CUMULATIVE APK HANDOFF; **ALL 46 CASES = NOT_RUN** as of 2026-10-24. NO APK built in planning/P00–P12. Do not tick PASS until the owner personally reports actual observation on a specifically fingerprinted P13 APK.
> Linked plan: [Issue #9 executable phases](ISSUE_9_PHASED_EXECUTION_PLAN.md). Issue: https://github.com/latticeward-labs/music-copilot/issues/9.

## How the owner will use this list (after P13 only)

1. Install the *single final cumulative* P13 Android 10+ debug APK. Record **exact APK SHA-256**, source commit, phone model, Android version, accessory and location context (stationary; never test while driving).
2. For each test, report **PASS**, **FAIL** (describe actual/expected plus reproduction), **BLOCKED** (missing dependency/vehicle/provider), **N/A** (platform not applicable with reason), or **NOT_RUN** (not yet tried). Do not infer PASS from build success.
3. Bluetooth P1 is separable from P0; inability to grant BT permission/obtain an eligible device does not justify failing P0, but P1 remains unverified.
4. **GS-04 is a Voice + Driving integration journey**; a Mic UI alone does not prove live STT/English NEXT. Until #10 real provider flow is available, M45 is **BLOCKED**, not PASS. **GS-05 vehicle validation requires a parked car/head unit**; testing AirPods is valuable but does not substitute for a car-specific PASS.
5. Executable JVM/Android instrumented/Lint tests may be *authored as source* but **none are run in this Issue's P00–P13 handoff workflow** under owner instruction. Record their status NOT_RUN as separate technical coverage debt.

## Final owner cases (46)

### A. Manual entry/navigation

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M01** | From Home/Library with a track playing, tap Enter Driving | Driving one-layer screen opens with current track; nothing restarts | NOT_RUN |
| **M02** | From Full Player, enter Driving, then Exit | Immediate return to Full Player with same service queue/playback | NOT_RUN |
| **M03** | From Home/Library, enter Driving then Android Back | Back acts exactly like Exit; returns to latest non-driving screen | NOT_RUN |
| **M04** | On a Playing track, inspect metadata, artwork and state label | Actual title/artist/artwork or safe placeholders; Playing reflects service | NOT_RUN |
| **M05** | Pause before entry and inspect Driving | Paused plus Play action, without fake Playing or autoplay | NOT_RUN |
| **M06** | Enter with no playable/current track | No Track and truthful explanation, not a fabricated song | NOT_RUN |
| **M07** | Inspect controls with no track or unavailable controller | Unavailable commands disabled/explained; no crash | NOT_RUN |
| **M08** | With No Track, inspect absence of unverified Play Last and tap Exit | No saved/unverified track playback; Exit to Home/Library without changing queue | NOT_RUN |

### B. Manual transport/service parity

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M09** | Tap Play/Pause inside Driving | Actual sound and screen/service media card converge; one state change | NOT_RUN |
| **M10** | Tap Previous | Queue previous-policy honored once; metadata synchronized | NOT_RUN |
| **M11** | Tap Next | Next local item once; no duplicate queue or service | NOT_RUN |
| **M12** | Tap Seek back | Position goes backwards by approved fixed interval; clamps at zero | NOT_RUN |
| **M13** | Try Previous/Next at queue beginning/end | Bounded correct service behavior, no invalid index or crash | NOT_RUN |
| **M14** | Tap transport quickly multiple times | No duplicate/unintended command receipts or false optimistic state | NOT_RUN |
| **M15** | With an unavailable/stale track or controller issue | Readable error/recovery; no false success or service ownership leak | NOT_RUN |
| **M16** | Inspect/tap separate large Mic | Visible and accessible; handoff only to real approved Voice readiness, otherwise honest unavailable/manual fallback; do NOT mark live STT PASS if #10 incomplete | NOT_RUN |
| **M17** | Exit during active Playing | Exits immediately; audio continues and standard manual UI remains usable | NOT_RUN |

### C. Lifecycle / #7 and #8 regressions

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M18** | Leave app to background then foreground while Driving | Real service snapshot restored; no second Player | NOT_RUN |
| **M19** | Rotate/recreate Activity or change UI configuration | Correct title/queue/state after recreation, stable destination/Exit | NOT_RUN |
| **M20** | Use system media card/notification then return to app | Driving or intended surface reflects authoritative MediaSession | NOT_RUN |
| **M21** | Reopen app after process/UI restart with saved session | No automatic Driving entry or unauthorized autoplay; service/restore truth shown | NOT_RUN |
| **M22** | Pause then Play on Android system media card | Previously fixed Play resumes once, matching app state | NOT_RUN |
| **M23** | Tap Android media card artwork/title | Opens Music Copilot, does not spawn duplicate playback | NOT_RUN |
| **M24** | Connected AirPods/headset: Pause then physical Play | Previously fixed external Play works while route connected; no duplication | NOT_RUN |
| **M25** | During Driving receive transient call/focus event | Playback respects #8 policy and state, safe conditional resume only | NOT_RUN |
| **M26** | During Playing disconnect then reconnect Bluetooth/headset | Immediate safe Pause, no phone speaker leak, no connection-only autoplay | NOT_RUN |

### D. Driving ergonomics / accessibility

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M27** | Inspect four large controls, Mic and Exit at normal font | Reachable high-contrast touch targets: at least 48dp, HF recommended 64/72/56 | NOT_RUN |
| **M28** | Enable TalkBack and navigate Playing, Paused, No Track | Status, metadata, Previous, Play/Pause, Next, Seek back, Mic, Exit read logically | NOT_RUN |
| **M29** | Set system font/display scaling up to 200% | No clipped hidden Exit/primary actions, usable reflow | NOT_RUN |
| **M30** | Inspect contrast, icon shape and error/pending text | No color-only state; meaningful readable labels | NOT_RUN |
| **M31** | Explore all active Driving controls | One layer; no typing, search, long list, settings chooser or nested menus | NOT_RUN |
| **M32** | Read road-attention notice | Concise driver's-responsibility copy, no guaranteed-safe-driving claim | NOT_RUN |

### E. P1 Bluetooth selected-device suggestion

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M33** | Outside Driving open Settings Bluetooth suggestion section | Only eligible paired audio devices can be selected, after contextual permission | NOT_RUN |
| **M34** | Choose and Save a device | Selection acknowledged and remains local after Settings reopen | NOT_RUN |
| **M35** | With app foreground on Library/Full Player, establish VERIFIED selected-device disconnected baseline, then connect selected audio profile | At most one nonblocking optional Driving suggestion only for the new selected-device reconnect; already connected at app launch is not a new invitation | NOT_RUN |
| **M36** | Tap Not now on suggestion | No Driving launch or Play/Voice; current screen unchanged | NOT_RUN |
| **M37** | After five-minute Not now cooldown (or as separate fresh session), disconnect/reconnect selected device, then tap Enter on a NEW offer | Driving opens only from this deliberate in-app tap and reflects actual playback state; no auto-play | NOT_RUN |
| **M38** | Connect a different, unselected Bluetooth audio device | No selected-device Driving suggestion | NOT_RUN |
| **M39** | Connect selected device with app in background | No forced foreground Activity, Driving, sound or microphone; permitted notification only with explicit opt-in | NOT_RUN |
| **M40** | Disconnect/reconnect rapidly; switch A2DP/Headset and reconnect while a prior invite is still visible; rotate or background during a pending offer | One current selected-device offer at most; duplicates and stale/old-epoch events ignored; background or disconnect clears invite | NOT_RUN |
| **M41** | Deny/revoke Nearby devices on Android 12+ (or disable BT) | P1 fails closed with outside-Driving recovery; P0 manual Driving still usable | NOT_RUN |
| **M42** | Remove selected device or disable P1 | Future connection does not suggest; manual Driving and playback still work | NOT_RUN |
| **M43** | Reopen Settings after app restart | Last deliberately saved selection remains; removal remains respected | NOT_RUN |
| **M44** | Disable notification permission if applicable and retry selected connect | No forced background launch or notification-permission coercion; manual Driving works | NOT_RUN |

### F. Parked golden-scenario checks / evidence scope

| ID | Action / condition | Expected observation | Status |
|---|---|---|---|
| **M45** | STATIONARY vehicle/head unit: manually enter Driving, Mic 'Next song' | GS-04 full pass requires real #10 English STT/intent/TTS and one service Next; otherwise BLOCKED (#10), not PASS | NOT_RUN |
| **M46** | STATIONARY vehicle/head unit: call/focus or BT route drop and reconnect | GS-05 safety: service-owned Pause/restricted resume, no unintended speaker output; without real vehicle mark BLOCKED, do not infer from earbuds | NOT_RUN |

## Owner response template

```text
Issue #9 / P13 cumulative APK
APK SHA256: [from published P13 manifest]
Phone / Android: [model + version]
Accessories: [none / wired / AirPods / selected BT audio / parked car head unit]
M01 PASS
M02 PASS
...
M45 BLOCKED (#10 Voice pending)   # example only; replace with actual observation
M46 NOT_RUN                       # example only; replace with actual observation
Any FAIL: ID, action sequence, expected vs actual, screenshots/log description if available
```

## P12 source handoff and priority verification order

**Source-only handoff:** [P12 hardening report](ISSUE_9_PHASE12_CROSS_FEATURE_HARDENING_AUDIT.md), [P13 build manifest (NOT_BUILT)](ISSUE_9_P13_APK_BUILD_MANIFEST.json). These do NOT imply runtime or build PASS. All 46 M cases remain NOT_RUN.

**Phone sequence (stationary only):**

1. First validate manual P0 functionality without Bluetooth permission: M01–M08, M09–M17, M18–M21. Reproduce Paused, No Track, Exit, real Media3 card, and service-owned commands.
2. Check safe focus/call/route/headset state M22–M26 and that Android media card Play, card tap opening app and AirPods Play still work (Issue #8 regression: do NOT infer from only pause/volume).
3. Check TalkBack, 150%/200% text, physical touch geometry and road disclaimer M27–M32. Capture device OS/font-scale settings; avoid actual driving.
4. Only if a real eligible already-paired audio output exists: M33–M44 on Android 12+ use explicit Nearby Devices grant (on Android 10/11 no runtime Bluetooth dialog). Confirm **Save** then **Remove**, persisted preference after restart, selected vs nonselected device, denial/radio-off, no automatic entry/play/mic. To test M35/M37, first observe DISCONNECTED with the app foreground; a device already connected on startup is NOT a new offer. M36 Not now starts a five-minute cooldown: account for it before M37.
5. Test GS-04 M45 only with the actual voice provider/intent flow from the related Voice issues; mark BLOCKED if incomplete. Test GS-05 M46 only with a **parked real car/head unit**; headphones alone cannot validate vehicle criteria.
6. Report per-case **PASS/FAIL/BLOCKED/N/A/NOT_RUN** with the single P13 APK SHA-256, exact phone/Android version, accessory category and reproducible error steps, never raw BT MAC/device name, voice transcript or personal song details.

**Important engineering truth:** An Android build, even a successful P13 APK build, does not execute the 177 previously authored source tests or the 19 P12-authored new test methods, and cannot change the 14 acceptance criteria or 46 manual outcomes to PASS. During P13, record the *actual* build outcome and keep test groups NOT_RUN unless the owner separately authorizes execution.

## Completion controls and separate engineering debt

- M01–M46 handset outcomes are **owner-reported evidence**, not automatic full Issue AC/test proof.
- Issue #9 baseline has **14 acceptance criteria**; reconcile each to real evidence and mark missing automated coverage as **NOT_RUN**. A completed source test file is not a passing executed test.
- Preserve existing #8 regression safeguards (Android media-card Play, media-card tap opens app, AirPods Play and safety pause on disconnect) and the no-auto-resume rule.
- Defer distinct Android 10 / 12 / 14+, Pixel / Samsung / Xiaomi and three parked vehicle/head-unit coverage honestly when only one device is available.
- No forced Bluetooth/Driving entry, no unsafe speaker leak and no unapproved data collection are release-critical.
- Merge/close only on explicit owner acceptance after real results are recorded; separate follow-up debt for any waived field/technical requirements.
