# COMPOSE_TOKEN_MAPPING — Exact Night Signal Design Foundation

**Authoritative source:** `docs/design-system/foundations/TOKENS.json` version 1.0.0 / 2026-10-03; attached MSM2 RAR SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Min Android API 29 (Android 10), Jetpack Compose with Material 3 mapping. Fixed dark visual baseline; NO new theme selector.

## All semantic colors (exact HEX, no hand-tuned approximation)

| Design token | Exact hex | Android Compose source target |
|---|---|---|
| `color.semantic.background` | `#090D12` | `MaterialTheme.colorScheme.background` |
| `color.semantic.surface` | `#111720` | `MaterialTheme.colorScheme.surface` |
| `color.semantic.surfaceVariant` | `#18212C` | `MaterialTheme.colorScheme.surfaceVariant` |
| `color.semantic.surfaceRaised` | `#202A36` | `MusicCopilotColors.surfaceRaised` (theme extension) |
| `color.semantic.systemBar` | `#090D12` | `MusicCopilotColors.systemBar` (theme extension) |
| `color.semantic.primary` | `#67D8FF` | `MaterialTheme.colorScheme.primary` |
| `color.semantic.onPrimary` | `#031117` | `MaterialTheme.colorScheme.onPrimary` |
| `color.semantic.primaryContainer` | `#143646` | `MaterialTheme.colorScheme.primaryContainer` |
| `color.semantic.onPrimaryContainer` | `#C7F2FF` | `MaterialTheme.colorScheme.onPrimaryContainer` |
| `color.semantic.secondary` | `#A9A1FF` | `MaterialTheme.colorScheme.secondary` |
| `color.semantic.onSecondary` | `#0E0B1B` | `MaterialTheme.colorScheme.onSecondary` |
| `color.semantic.success` | `#76E0A3` | `MusicCopilotColors.success` (theme extension) |
| `color.semantic.onSuccess` | `#07110B` | `MusicCopilotColors.onSuccess` (theme extension) |
| `color.semantic.warning` | `#FFD166` | `MusicCopilotColors.warning` (theme extension) |
| `color.semantic.onWarning` | `#151008` | `MusicCopilotColors.onWarning` (theme extension) |
| `color.semantic.error` | `#FF7088` | `MaterialTheme.colorScheme.error` |
| `color.semantic.onError` | `#0E0B0D` | `MaterialTheme.colorScheme.onError` |
| `color.semantic.errorContainer` | `#3A1720` | `MaterialTheme.colorScheme.errorContainer` |
| `color.semantic.onErrorContainer` | `#FFD9DF` | `MaterialTheme.colorScheme.onErrorContainer` |
| `color.semantic.textPrimary` | `#F5F7FA` | `MusicCopilotColors.textPrimary` (theme extension) |
| `color.semantic.textSecondary` | `#C8D0DA` | `MusicCopilotColors.textSecondary` (theme extension) |
| `color.semantic.textMuted` | `#9AA5B3` | `MusicCopilotColors.textMuted` (theme extension) |
| `color.semantic.border` | `#2B3746` | `MaterialTheme.colorScheme.outline` |
| `color.semantic.borderStrong` | `#5A6878` | `MusicCopilotColors.borderStrong` (theme extension) |
| `color.semantic.focusRing` | `#A2EBFF` | `MusicCopilotColors.focusRing` (theme extension) |
| `color.semantic.disabledContainer` | `#202A36` | `MusicCopilotColors.disabledContainer` (theme extension) |
| `color.semantic.disabledContent` | `#7C8998` | `MusicCopilotColors.disabledContent` (theme extension) |
| `color.semantic.scrim` | `#000000B8` | `MusicCopilotColors.scrim` (theme extension) |

## Full primitive palette for future tokens and exact overlays

| Primitive token | Hex |
|---|---|
| `color.primitive.neutral950` | `#090D12` |
| `color.primitive.neutral900` | `#111720` |
| `color.primitive.neutral850` | `#18212C` |
| `color.primitive.neutral800` | `#202A36` |
| `color.primitive.neutral700` | `#2B3746` |
| `color.primitive.neutral500` | `#5A6878` |
| `color.primitive.neutral400` | `#7C8998` |
| `color.primitive.neutral300` | `#9AA5B3` |
| `color.primitive.neutral200` | `#C8D0DA` |
| `color.primitive.neutral100` | `#E4E9EF` |
| `color.primitive.neutral050` | `#F5F7FA` |
| `color.primitive.white` | `#FFFFFF` |
| `color.primitive.black` | `#000000` |
| `color.primitive.cyan500` | `#67D8FF` |
| `color.primitive.cyan300` | `#A2EBFF` |
| `color.primitive.cyan100` | `#C7F2FF` |
| `color.primitive.violet500` | `#A9A1FF` |
| `color.primitive.violet100` | `#E5E1FF` |
| `color.primitive.green500` | `#76E0A3` |
| `color.primitive.amber500` | `#FFD166` |
| `color.primitive.red500` | `#FF7088` |
| `color.primitive.red100` | `#FFD9DF` |

## Typography / `sp` font metrics

Android font family: `Roboto` preferred, fallback `sans-serif` (system `sans-serif`). Do not multiply SP by fontScale manually; Compose accessibility scaling is applied by the system.

| Text role | fontSizeSp | lineHeightSp | fontWeight | letterSpacingSp | Compose declaration |
|---|---:|---:|---:|---:|---|
| `typography.display` | 32 | 40 | 700 | 0 | `MusicCopilotTypography.display` / related MaterialTheme typography |
| `typography.titleLarge` | 24 | 32 | 700 | 0 | `MusicCopilotTypography.titleLarge` / related MaterialTheme typography |
| `typography.titleMedium` | 20 | 28 | 600 | 0 | `MusicCopilotTypography.titleMedium` / related MaterialTheme typography |
| `typography.bodyLarge` | 16 | 24 | 400 | 0 | `MusicCopilotTypography.bodyLarge` / related MaterialTheme typography |
| `typography.bodyMedium` | 14 | 20 | 400 | 0 | `MusicCopilotTypography.bodyMedium` / related MaterialTheme typography |
| `typography.labelLarge` | 14 | 20 | 600 | 0 | `MusicCopilotTypography.labelLarge` / related MaterialTheme typography |
| `typography.labelMedium` | 12 | 16 | 600 | 0 | `MusicCopilotTypography.labelMedium` / related MaterialTheme typography |
| `typography.drivingTitle` | 28 | 36 | 700 | 0 | `MusicCopilotTypography.drivingTitle` / related MaterialTheme typography |
| `typography.drivingMeta` | 18 | 26 | 500 | 0 | `MusicCopilotTypography.drivingMeta` / related MaterialTheme typography |
| `typography.drivingState` | 16 | 24 | 600 | 0 | `MusicCopilotTypography.drivingState` / related MaterialTheme typography |

## Explicit geometry and spacing: DP, not image pixels

360×800 **dp** design reference and 1080×2400 **px** review PNGs are different units. Compose code must use `dp`, `sp`, `WindowInsets`, responsive constraints, vertical growth/scroll at 200% text scaling. Do not set a full screen image as UI.

| Token | Exact value | API mapping |
|---|---|---|
| `spacing.xxsDp` | `4` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.xsDp` | `8` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.smDp` | `12` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.mdDp` | `16` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.lgDp` | `24` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.xlDp` | `32` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.xxlDp` | `40` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.xxxlDp` | `48` | `MusicSpacing` / MaterialTheme where appropriate |
| `spacing.hugeDp` | `64` | `MusicSpacing` / MaterialTheme where appropriate |
| `grid.baselineDp` | `4` | `MusicGrid` / MaterialTheme where appropriate |
| `grid.compactColumns` | `4` | `MusicGrid` / MaterialTheme where appropriate |
| `grid.gutterDp` | `12` | `MusicGrid` / MaterialTheme where appropriate |
| `grid.screenMarginDp` | `16` | `MusicGrid` / MaterialTheme where appropriate |
| `grid.drivingMarginDp` | `20` | `MusicGrid` / MaterialTheme where appropriate |
| `grid.referenceViewportDp.width` | `360` | `MusicGrid` reference / `Modifier` |
| `grid.referenceViewportDp.height` | `800` | `MusicGrid` reference / `Modifier` |
| `radius.noneDp` | `0` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.xsDp` | `4` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.smDp` | `8` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.mdDp` | `12` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.lgDp` | `16` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.xlDp` | `24` | `MusicRadius` / MaterialTheme where appropriate |
| `radius.pillDp` | `999` | `MusicRadius` / MaterialTheme where appropriate |
| `elevation.level0Dp` | `0` | `MusicElevation` / MaterialTheme where appropriate |
| `elevation.level1Dp` | `1` | `MusicElevation` / MaterialTheme where appropriate |
| `elevation.level2Dp` | `3` | `MusicElevation` / MaterialTheme where appropriate |
| `elevation.level3Dp` | `6` | `MusicElevation` / MaterialTheme where appropriate |
| `elevation.level4Dp` | `12` | `MusicElevation` / MaterialTheme where appropriate |
| `size.touchTargetMinDp` | `48` | `MusicSize` / MaterialTheme where appropriate |
| `size.appBarHeightDp` | `56` | `MusicSize` / MaterialTheme where appropriate |
| `size.bottomNavigationHeightDp` | `64` | `MusicSize` / MaterialTheme where appropriate |
| `size.trackRowMinHeightDp` | `64` | `MusicSize` / MaterialTheme where appropriate |
| `size.iconDefaultDp` | `24` | `MusicSize` / MaterialTheme where appropriate |
| `size.iconPlayerDp` | `32` | `MusicSize` / MaterialTheme where appropriate |
| `size.iconDrivingDp` | `36` | `MusicSize` / MaterialTheme where appropriate |
| `size.iconVisualStrokeDp` | `2` | `MusicSize` / MaterialTheme where appropriate |
| `size.drivingControlMinDp` | `64` | `MusicSize` / MaterialTheme where appropriate |
| `size.drivingMicMinDp` | `72` | `MusicSize` / MaterialTheme where appropriate |
| `size.drivingExitMinDp` | `56` | `MusicSize` / MaterialTheme where appropriate |
| `size.focusRingWidthDp` | `2` | `MusicSize` / MaterialTheme where appropriate |
| `size.focusRingOffsetDp` | `2` | `MusicSize` / MaterialTheme where appropriate |

## Component-local overrides (every dimension of all 23 CMP IDs)

| CMP ID | Name | Approved field | Value |
|---|---|---|---|
| `CMP-01` | ActionButton | `minHeightDp` | `48` |
| `CMP-01` | ActionButton | `horizontalPaddingDp` | `16` |
| `CMP-01` | ActionButton | `iconGapDp` | `8` |
| `CMP-01` | ActionButton | `radiusDp` | `12` |
| `CMP-02` | IconButton | `targetDp` | `48` |
| `CMP-02` | IconButton | `iconDefaultDp` | `24` |
| `CMP-02` | IconButton | `iconPlayerDp` | `32` |
| `CMP-02` | IconButton | `iconDrivingDp` | `36` |
| `CMP-03` | AppBar | `minHeightDp` | `56` |
| `CMP-03` | AppBar | `actionTargetDp` | `48` |
| `CMP-03` | AppBar | `horizontalPaddingDp` | `16` |
| `CMP-04` | BottomNavigation | `minHeightDp` | `64` |
| `CMP-04` | BottomNavigation | `itemTargetMinDp` | `48` |
| `CMP-05` | PlaybackControls | `standardTargetMinDp` | `48` |
| `CMP-05` | PlaybackControls | `drivingTargetMinDp` | `64` |
| `CMP-05` | PlaybackControls | `playerIconDp` | `32` |
| `CMP-05` | PlaybackControls | `drivingIconDp` | `36` |
| `CMP-06` | MicControl | `standardTargetMinDp` | `48` |
| `CMP-06` | MicControl | `playerPreferredTargetDp` | `56` |
| `CMP-06` | MicControl | `drivingTargetMinDp` | `72` |
| `CMP-06` | MicControl | `standardIconDp` | `24` |
| `CMP-06` | MicControl | `drivingIconDp` | `36` |
| `CMP-07` | TrackMetadata | `maxLinesTitleNonDriving` | `2` |
| `CMP-07` | TrackMetadata | `maxLinesMetaNonDriving` | `2` |
| `CMP-07` | TrackMetadata | `drivingMarquee` | `false` |
| `CMP-08` | Artwork | `aspectRatio` | `1:1` |
| `CMP-08` | Artwork | `radiusDp` | `12` |
| `CMP-08` | Artwork | `rowPreferredDp` | `48` |
| `CMP-08` | Artwork | `miniPlayerPreferredDp` | `48` |
| `CMP-09` | MiniPlayer | `minHeightDp` | `72` |
| `CMP-09` | MiniPlayer | `artworkDp` | `48` |
| `CMP-09` | MiniPlayer | `playPauseTargetDp` | `48` |
| `CMP-09` | MiniPlayer | `horizontalPaddingDp` | `12` |
| `CMP-10` | SearchField | `minHeightDp` | `48` |
| `CMP-10` | SearchField | `horizontalPaddingDp` | `12` |
| `CMP-10` | SearchField | `iconTargetDp` | `48` |
| `CMP-10` | SearchField | `radiusDp` | `12` |
| `CMP-11` | MediaRow | `minHeightDp` | `64` |
| `CMP-11` | MediaRow | `leadingArtworkPreferredDp` | `48` |
| `CMP-11` | MediaRow | `trailingTargetDp` | `48` |
| `CMP-11` | MediaRow | `contentGapDp` | `12` |
| `CMP-12` | EntityRow | `minHeightDp` | `64` |
| `CMP-12` | EntityRow | `leadingVisualPreferredDp` | `48` |
| `CMP-12` | EntityRow | `trailingTargetDp` | `48` |
| `CMP-13` | ProgressSeek | `interactiveBandMinHeightDp` | `48` |
| `CMP-13` | ProgressSeek | `visualTrackDp` | `4` |
| `CMP-13` | ProgressSeek | `thumbDp` | `20` |
| `CMP-14` | PermissionEducationCard | `paddingDp` | `16` |
| `CMP-14` | PermissionEducationCard | `actionMinHeightDp` | `48` |
| `CMP-14` | PermissionEducationCard | `radiusDp` | `16` |
| `CMP-15` | StatusBlock | `paddingDp` | `24` |
| `CMP-15` | StatusBlock | `actionMinHeightDp` | `48` |
| `CMP-15` | StatusBlock | `radiusDp` | `16` |
| `CMP-16` | ConfirmationChoice | `minHeightDp` | `48` |
| `CMP-16` | ConfirmationChoice | `preferredHeightDp` | `56` |
| `CMP-16` | ConfirmationChoice | `verticalGapDp` | `8` |
| `CMP-16` | ConfirmationChoice | `maxCandidateCount` | `2` |
| `CMP-17` | SnackbarStatus | `minHeightDp` | `48` |
| `CMP-17` | SnackbarStatus | `horizontalPaddingDp` | `16` |
| `CMP-17` | SnackbarStatus | `maxActions` | `1` |
| `CMP-18` | SettingsRow | `minHeightDp` | `64` |
| `CMP-18` | SettingsRow | `trailingTargetMinDp` | `48` |
| `CMP-18` | SettingsRow | `horizontalPaddingDp` | `16` |
| `CMP-19` | VoiceFeedbackOverlay | `cancelTargetMinDp` | `48` |
| `CMP-19` | VoiceFeedbackOverlay | `listeningMaxMs` | `6000` |
| `CMP-19` | VoiceFeedbackOverlay | `maxAttempts` | `2` |
| `CMP-19` | VoiceFeedbackOverlay | `maxConfirmationCandidates` | `2` |
| `CMP-20` | LikeToggleP2 | `targetDp` | `48` |
| `CMP-20` | LikeToggleP2 | `iconDp` | `24` |
| `CMP-21` | SystemBoundaryAnnotation | `prefix` | `[ANDROID / SYSTEM]` |
| `CMP-22` | PagerIndicator | `visualDotDp` | `8` |
| `CMP-22` | PagerIndicator | `visualGapDp` | `8` |
| `CMP-23` | ConfirmationPanel | `paddingDp` | `24` |
| `CMP-23` | ConfirmationPanel | `radiusDp` | `16` |
| `CMP-23` | ConfirmationPanel | `actionMinHeightDp` | `48` |

## Motion / state / accessibility

| Role | Exact source settings |
|---|---|
| `motion.instantMs` | `0` |
| `motion.fastMs` | `100` |
| `motion.standardMs` | `160` |
| `motion.deliberateMs` | `220` |
| `motion.maxNonCriticalMs` | `300` |
| `motion.drivingOrdinaryMaxMs` | `160` |
| `motion.standardEasingCubicBezier` | `[0.2,0,0,1]` |
| `motion.restrictions` | `["no-driving-marquee","no-driving-parallax","no-decorative-looping","state-meaning-survives-animation-disabled"]` |
| `state.pressedOverlay` | `"#FFFFFF1A"` |
| `state.focus` | `{"color":"#A2EBFF","widthDp":2,"offsetDp":2}` |
| `state.selected` | `{"container":"#143646","content":"#C7F2FF","requiresNonColorCue":true}` |
| `state.disabled` | `{"container":"#202A36","content":"#7C8998","semanticsRequired":true}` |
| `state.error` | `{"content":"#FF7088","container":"#3A1720","requiresTextOrIconCue":true}` |
| `state.loading` | `{"requiresWrittenState":true,"mustDifferFromDisabled":true}` |
| `accessibility.minimumTouchTargetDp` | `48` |
| `accessibility.textScalePercent` | `200` |
| `accessibility.normalTextContrastMinimum` | `4.5` |
| `accessibility.largeTextAndEssentialGraphicContrastMinimum` | `3` |
| `accessibility.talkBackOrderRequired` | `true` |
| `accessibility.nonColorOnlyRequired` | `true` |
| `accessibility.visibleFocusRequired` | `true` |
| `accessibility.runtimeComposeProofDownstreamFinding` | `PH10-MIN-001` |

## Required Compose mapping and implementation guardrails

- `MaterialTheme.colorScheme` maps standard Material 3 roles; custom semantic tokens remain strongly typed `MusicCopilotColors` extension for `success`, `warning`, `textPrimary/Secondary/Muted`, `focusRing`, `disabledContainer/Content`, `systemBar`, `scrim`; do not invent color aliases that lose meaning.
- `MaterialTheme.typography` plus `MusicCopilotTypography` includes drivingTitle 28sp/36sp, drivingMeta 18sp/26sp, drivingState 16sp/24sp. `MusicSpacing`, `MusicSizes`, `MusicShapes`, `MusicMotion` expose source DP values. Preserve 4dp baseline grid.
- Driving: 20dp margin; primary playback 64dp, mic 72dp, Exit 56dp, other interactive ≥48dp. Non-driving ActionButton min48dp, AppBar 56dp, bottom nav64dp, MediaRow64dp. Art square 1:1 radius12dp. 2dp focus ring + 2dp offset.
- Dynamic state is written as text/icon and TalkBack `stateDescription`; color alone never denotes Error/Selected/Playing. No Driving marquee, parallax or decorative looping. Ordinary Driving motion ≤160ms; reduced motion must retain state meaning.
- Numeric contract: SeekBy(-10_000ms); Previous position >3_000ms restarts; Listening timeout ≤6_000ms; one explicit Retry; Confirmation options 1..2. These belong in domain/service/Voice policy, **not token presentation code**.

## Small illustrative Kotlin mapping (NOT compiled or runtime-tested)

```kotlin
val MusicBackground = Color(0xFF090D12)
val MusicSurface = Color(0xFF111720)
val MusicPrimary = Color(0xFF67D8FF)
val MusicOnPrimary = Color(0xFF031117)
val MusicFocusRing = Color(0xFFA2EBFF)
val MusicError = Color(0xFFFF7088)
object MusicSpacing { val screen=16.dp; val component=12.dp; val control=8.dp; val driving=20.dp }
object MusicSizes { val touch=48.dp; val drivingControl=64.dp; val mic=72.dp; val exit=56.dp }
// Example: Text(style=MusicCopilotTypography.drivingTitle) uses 28.sp/36.sp.
// Use BoxWithConstraints or responsive Column/verticalScroll and correct system insets.
// At system fontScale=2f, labels must wrap, scroll or grow without hiding Exit/Cancel.
```

**Status:** all source token values enumerated; actual Compose @Preview, 200% reflow, TalkBack and device target assertions pending PH10-MIN-001.
