# keystore

`testkey.p12` is a PKCS#12 wrapping of the **public AOSP testkey** from
[AOSP `build/make/target/product/security`](https://android.googlesource.com/platform/build/+/refs/heads/main/target/product/security/):

- `testkey.x509.pem` — X.509 certificate, subject/issuer `CN=Android`, valid
  until 2035-07-17.
- `testkey.pk8` — matching PKCS#8 (unencrypted) private key.

It is a **public, non-secret** key shipped with AOSP sources for testing. Do not
use it for production or the Play Store.

## Regenerating

```bash
curl -fsSL -o testkey.x509.pem \
  https://raw.githubusercontent.com/aosp-mirror/platform_build/master/target/product/security/testkey.x509.pem
curl -fsSL -o testkey.pk8 \
  https://raw.githubusercontent.com/aosp-mirror/platform_build/master/target/product/security/testkey.pk8

openssl pkcs8 -inform DER -in testkey.pk8 -nocrypt -out testkey.pk8.pem
openssl pkcs12 -export -in testkey.x509.pem -inkey testkey.pk8.pem \
  -name platform -out testkey.p12 -passout pass:android
```

| Field | Value |
| --- | --- |
| store type | PKCS12 |
| store password | `android` |
| key alias | `platform` |
| key password | `android` |
