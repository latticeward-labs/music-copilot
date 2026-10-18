# Issue #5 — Cost-Controlled APK Build and Physical Phone QA

**Approved workflow mode (owner request, 18 October 2026).**
Applies only to branch `feature/5-onboarding-permissions-recovery` of Issue #5.
It does not amend the PRD, privacy gates, technical architecture, acceptance
criteria, or workflows belonging to other issues.

## CI behavior

- All thirteen historical `.github/workflows/issue-5-phase{1..9}*.yml`
  workflows are **manual-only** (`workflow_dispatch`); they no longer auto-run
  on `push` or `pull_request` for this branch. Historical test evidence is retained.
- The new `.github/workflows/issue-5-apk-only.yml` runs one Ubuntu job with
  `./gradlew :app:assembleDebug`. It does not start an emulator, run instrumented
  tests, claim unit/Lint/Detekt coverage, or claim a successful manual test.
- **No APK is requested merely by changing code.** After a phase's changes are
  committed, explicitly create or change
  `.github/issue5-apk-request.txt` in a **separate, follow-up commit**. This
  single-marker change triggers precisely the APK-only workflow, provided no
  unrelated workflows have been added with overlapping triggers.
- A fresh request file should contain the phase ID (e.g. P10), requested source
  commit (the immediately preceding implementation SHA), date and purpose.
  The APK actually builds the **request commit SHA**; use the SHA in
  `BUILD_PROVENANCE.txt` as definitive.
- `workflow_dispatch` becomes selectable in GitHub's Actions UI only after
  the workflow exists on the default branch. Before that, the marker-commit
  trigger above is the supported request method.
- On a successful run, download `issue5-phone-test-apk-<run_number>` from
  the run's Artifacts. It includes `app-debug.apk`, its SHA-256 file and the
  commit/run provenance. Artifact retention is **7 days**.
- A failed build still uses runner minutes. Success proves compilation and
  artifact upload only, **not** Android behavior or 13/13 acceptance.

## Manual QA on owner's HONOR X7d / Android 15

For each feature-relevant phase (P10–P14):
1. Install **the exact APK from the recorded Actions run**. Record whether
   installation was an update or clean install.
2. Execute a phase-specific written checklist with positive, denial, recovery,
   rotation/background/relaunch and privacy cases wherever applicable.
3. Record each test ID, steps, expected/observed result and **PASS/FAIL/NOT RUN**.
   Add screenshot(s), screen recording or ADB evidence where feasible. Never
   record raw voice/audio or personal media in evidence.
4. Report manufacturer, model, Android version, build provenance SHA, APK
   SHA-256, test date and tester attribution **owner-reported/manual**.
5. Record failures without calling them PASS. Fix failures, request a new APK and
   repeat affected and regression cases before accepting the phase.
6. Upload/report the evidence in `docs/implementation/` and reference it in
   `ISSUE_5_EVIDENCE_INDEX.md` and Issue #5. AI/chat testimony is not
   independent device attestation; never fabricate reviewer or test logs.

## Remaining compulsory non-phone checks

Phone testing does not substitute for static security/privacy source review,
Kotlin unit tests, lint/Detekt, architecture validation, automated Compose
accessibility checks, or API29/API32/API33 compatibility. They may be executed
locally using Android Studio/Gradle and documented, or through an explicitly
authorized, limited CI run when local validation is unavailable. Do not mark
unverified technical or Android-version/device criteria as PASS.

P14 requires real evidence for all 13 Issue #5 acceptance criteria, review and
policy exceptions as applicable, plus exact-head required checks before final
merge. No earlier scoped PASS or historical CI proof is erased. This protocol
changes **when CI runs**, not what successful delivery means.

## Cost control / rollback

Normal changes to `app/src/main/**` do not trigger the old phase workflows.
One requested APK means one Linux job (unless GitHub retries or another workflow
is manually invoked). The real duration depends on Gradle caches and availability;
plan approximately 3–6 minutes per successful APK build, not a guaranteed cap.
To restore automatic CI later, update the historical `on:` triggers through
an explicitly reviewed pull request. Do not directly edit `main` here.
