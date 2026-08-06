#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 || $# -gt 3 ]]; then
  echo "Usage: $0 TS-01|TS-02|TS-06|TS-08 <build-sha> [result-root]" >&2
  exit 2
fi

spike_id="$1"
build_sha="$2"
result_root="${3:-docs/user-flows/evidence/results}"

case "$spike_id" in
  TS-01|TS-02|TS-06|TS-08) ;;
  *) echo "Unsupported spike ID: $spike_id" >&2; exit 2 ;;
esac

if [[ ! "$build_sha" =~ ^[0-9a-fA-F]{7,40}$ ]]; then
  echo "build-sha must be a 7-40 character hexadecimal commit SHA" >&2
  exit 2
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required" >&2
  exit 2
fi

run_date="$(date -u +%Y-%m-%d)"
bundle="$result_root/$spike_id/$run_date-$build_sha"

if [[ -e "$bundle" ]]; then
  echo "Refusing to overwrite existing immutable bundle: $bundle" >&2
  exit 3
fi

mkdir -p "$bundle/artifacts"

device_model="$(adb shell getprop ro.product.model | tr -d '\r')"
manufacturer="$(adb shell getprop ro.product.manufacturer | tr -d '\r')"
api_level="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
oem_release="$(adb shell getprop ro.build.version.incremental | tr -d '\r')"
qemu="$(adb shell getprop ro.kernel.qemu | tr -d '\r')"
device_kind="physical"
if [[ "$qemu" == "1" ]]; then device_kind="emulator"; fi

cat > "$bundle/manifest.md" <<EOF
# $spike_id Result Manifest

- Date (UTC): $run_date
- Repository/build SHA: $build_sha
- Package: com.latticeward.musiccopilot.spike
- Device kind: $device_kind
- Manufacturer: $manufacturer
- Model: $device_model
- API: $api_level
- OEM build: $oem_release
- Synthetic corpus only: REVIEW REQUIRED
- Reviewer (Android): PENDING
- Reviewer (QA): PENDING
- Final decision: PENDING

Before signing, add exact app/SDK/Media3 versions, executed row IDs, steps, expected/actual, artifact checksums, linked defects, and redaction confirmation.
EOF

cat > "$bundle/matrix.md" <<EOF
# $spike_id Execution Matrix

| Row | Expected | Actual | Result | Artifact |
|---|---|---|---|---|
| PENDING | Copy every mandatory protocol row | PENDING | BLOCKED | PENDING |
EOF

adb logcat -d -s MusicCopilotSpike:I '*:S' > "$bundle/artifacts/state-trace.txt"
cp "$bundle/artifacts/state-trace.txt" "$bundle/artifacts/logcat-redacted.txt"
adb exec-out screencap -p > "$bundle/artifacts/screenshot.png"

echo "Draft bundle created at $bundle"
echo "Review/redact every artifact and replace all PENDING/BLOCKED fields before sign-off."
