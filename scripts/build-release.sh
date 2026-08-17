#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .local-signing/keystore.properties ]] || \
   [[ ! -f .local-signing/nimbusdeck-release.jks ]]; then
  echo "Permanent release key is missing." >&2
  echo "Restore .local-signing from the private backup or run:" >&2
  echo "  ./scripts/create-release-key.sh" >&2
  exit 1
fi

./gradlew --no-daemon clean :app:assembleRelease
mkdir -p output
cp app/build/outputs/apk/release/app-release.apk \
  output/NimbusDeck-1.0.0-release.apk
sha256sum output/NimbusDeck-1.0.0-release.apk \
  | tee output/NimbusDeck-1.0.0-release.apk.sha256

echo "Release ready: output/NimbusDeck-1.0.0-release.apk"
