#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SIGN_DIR="$ROOT/.local-signing"
KEYSTORE="$SIGN_DIR/nimbusdeck-release.jks"
PROPS="$SIGN_DIR/keystore.properties"
CREDS="$SIGN_DIR/SIGNING-CREDENTIALS.txt"
BACKUP="$SIGN_DIR/NimbusDeck-signing-backup-KEEP-PRIVATE.zip"

for tool in openssl zip; do
  command -v "$tool" >/dev/null || {
    echo "Missing required tool: $tool" >&2
    exit 1
  }
done

mkdir -p "$SIGN_DIR"
chmod 700 "$SIGN_DIR"
if [[ -e "$KEYSTORE" ]]; then
  echo "Release key already exists: $KEYSTORE" >&2
  echo "Refusing to replace the permanent update key." >&2
  exit 1
fi

PASSWORD="$(openssl rand -hex 24)"
openssl req -x509 -newkey rsa:4096 -sha256 -days 36500 -nodes \
  -subj '/CN=NimbusDeck Release/O=sj0404-collab/C=UA' \
  -keyout "$SIGN_DIR/private-key.pem" \
  -out "$SIGN_DIR/release-certificate.pem" >/dev/null 2>&1
openssl pkcs12 -export -name nimbusdeck \
  -inkey "$SIGN_DIR/private-key.pem" \
  -in "$SIGN_DIR/release-certificate.pem" \
  -out "$KEYSTORE" -passout "pass:$PASSWORD"

FINGERPRINT="$(openssl x509 -in "$SIGN_DIR/release-certificate.pem" \
  -noout -fingerprint -sha256 | cut -d= -f2)"

cat > "$PROPS" <<EOF
storeFile=.local-signing/nimbusdeck-release.jks
storePassword=$PASSWORD
keyAlias=nimbusdeck
keyPassword=$PASSWORD
EOF

cat > "$CREDS" <<EOF
NIMBUSDECK PERMANENT ANDROID RELEASE KEY
=======================================

Package: com.sj0404.nimbusdeck
Keystore type: PKCS#12
Key algorithm: RSA 4096
Certificate: SHA256withRSA, valid for 100 years
Alias: nimbusdeck
Store password: $PASSWORD
Key password: $PASSWORD
SHA-256 certificate fingerprint: $FINGERPRINT

KEEP THIS FILE AND nimbusdeck-release.jks PRIVATE.
Every future NimbusDeck update must be signed with this same key.
Losing the key means existing installations cannot be updated.
EOF

rm -f "$SIGN_DIR/private-key.pem" "$SIGN_DIR/release-certificate.pem"
chmod 600 "$KEYSTORE" "$PROPS" "$CREDS"
(cd "$SIGN_DIR" && zip -q "$(basename "$BACKUP")" \
  "$(basename "$KEYSTORE")" "$(basename "$PROPS")" "$(basename "$CREDS")")
chmod 600 "$BACKUP"

printf 'Permanent key created.\nSHA-256: %s\nBackup: %s\n' "$FINGERPRINT" "$BACKUP"
