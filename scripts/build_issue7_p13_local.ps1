# Issue #7 P13 — one combined debug APK built locally, NEVER via GitHub Actions.
# Invoke in Windows PowerShell from a full clone of feature/7-media3-playback-queue-session:
# powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_issue7_p13_local.ps1 -AndroidSdk "C:\Android\Sdk" -JavaHome "C:\Path\To\JDK17"
[CmdletBinding()]
param(
    [string] $AndroidSdk = "C:\Android\Sdk",
    [string] $JavaHome = $env:JAVA_HOME
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Fail([string] $Why) { throw "ISSUE7 P13 BUILD BLOCKED: $Why" }
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$wrapper = Join-Path $root "gradlew.bat"
if (-not (Test-Path -LiteralPath $wrapper -PathType Leaf)) {
    Fail "Missing Gradle wrapper (gradlew.bat); download a complete repository clone."
}
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Fail "git is required to fingerprint the exact original source commit."
}
$branch = (& git -C $root branch --show-current).Trim()
if ($LASTEXITCODE -ne 0 -or $branch -ne "feature/7-media3-playback-queue-session") {
    Fail "Expected feature/7-media3-playback-queue-session; actual: $branch."
}
$sourceCommit = (& git -C $root rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceCommit -notmatch "^[0-9a-f]{40}$") {
    Fail "Could not establish an exact 40-character Git source commit."
}
# A dirty tracked source tree makes an APK SHA no longer attributable to Git.
$dirty = @(& git -C $root status --porcelain --untracked-files=no)
$dirtyCount = @($dirty | Where-Object { $_ -and $_.Trim().Length -gt 0 }).Count
if ($LASTEXITCODE -ne 0 -or $dirtyCount -ne 0) {
    Fail "Tracked source files differ from Git HEAD; commit or restore them first."
}
$AndroidSdk = [IO.Path]::GetFullPath($AndroidSdk)
if (-not (Test-Path -LiteralPath (Join-Path $AndroidSdk "platforms\android-36\android.jar"))) {
    Fail "Android SDK platform 36 missing under $AndroidSdk. Install SDK Platform 36 via Android Studio."
}
if (-not $JavaHome -or -not (Test-Path -LiteralPath (Join-Path $JavaHome "bin\java.exe"))) {
    Fail "Specify -JavaHome pointing to an installed JDK 17 directory (not a JBR 25 runtime)."
}
$JavaHome = [IO.Path]::GetFullPath($JavaHome)
$javaVersion = (& (Join-Path $JavaHome "bin\java.exe") -version 2>&1) | Out-String
if ($javaVersion -notmatch 'version "17[."]') {
    Fail "JDK 17 required for this pinned build; current: $javaVersion"
}
$env:JAVA_HOME = $JavaHome
$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk

# Explicitly ban the existing Actions workflows and all test tasks.
# :app:clean removes old assembled outputs; :app:assembleDebug creates ONLY
# this checkout's combined full app debug APK (automatic Android debug signing).
$distDir = Join-Path $root "dist\issue7-p13"
$copyApk = Join-Path $distDir "MusicCopilot-Issue7-P13-debug.apk"
if (Test-Path -LiteralPath $copyApk) {
    Fail "One final APK already exists at $copyApk. Do not silently overwrite its evidence."
}
Write-Host "Source commit: $sourceCommit"
Write-Host "Android SDK:   $AndroidSdk"
Write-Host "JDK 17:        $JavaHome"
Write-Host "Executing LOCAL Gradle only, no GitHub Actions / no JUnit / no instrumentation."
Push-Location $root
try {
    & $wrapper --no-daemon --console=plain :app:clean :app:assembleDebug
    $gradleExit = $LASTEXITCODE
} finally {
    Pop-Location
}
if ($gradleExit -ne 0) {
    Fail "Gradle returned $gradleExit. No APK was delivered. Fix compile problems before rerunning."
}
$builtApk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path -LiteralPath $builtApk -PathType Leaf)) {
    Fail "Gradle did not produce the expected app-debug.apk."
}
if ((Get-Item -LiteralPath $builtApk).Length -lt 1024) {
    Fail "The APK output is unexpectedly tiny; refusing to deliver it."
}
$headAfter = (& git -C $root rev-parse HEAD).Trim()
if ($headAfter -ne $sourceCommit) {
    Fail "Git HEAD changed during the build; APK source attribution cannot be trusted."
}
New-Item -Path $distDir -ItemType Directory -Force | Out-Null
Copy-Item -LiteralPath $builtApk -Destination $copyApk
$sha = (Get-FileHash -LiteralPath $copyApk -Algorithm SHA256).Hash.ToLowerInvariant()
$metadata = [ordered]@{
    issue = 7
    phase = "P13"
    source_repository = "latticeward-labs/music-copilot"
    source_branch = $branch
    source_commit = $sourceCommit
    apk_name = "MusicCopilot-Issue7-P13-debug.apk"
    apk_sha256 = $sha
    apk_bytes = (Get-Item -LiteralPath $copyApk).Length
    built_at_utc = (Get-Date).ToUniversalTime().ToString("o")
    toolchain_java_home = $JavaHome
    toolchain_android_sdk = $AndroidSdk
    local_build_command = "gradlew.bat --no-daemon --console=plain :app:clean :app:assembleDebug"
    github_actions_dispatched = $false
    automated_tests_run = $false
    manual_phone_cases = "M01-M36 NOT_RUN pending owner's actual observations"
    technical_cases = "T01-T15 NOT_RUN pending actual evidence"
}
$manifest = Join-Path $distDir "MusicCopilot-Issue7-P13-build.json"
$metadata | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $manifest -Encoding UTF8
Write-Host ""
Write-Host "ONE consolidated local APK built successfully:"
Write-Host $copyApk
Write-Host "SHA-256: $sha"
Write-Host "Commit:  $sourceCommit"
Write-Host "Build manifest: $manifest"
Write-Host "Owner phone checklist: docs\implementation\ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md"
Write-Host "Do not call manual or technical tests PASS before they are actually executed."
