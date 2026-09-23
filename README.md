# ph-nss

<!-- ph-badge-start -->
[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/com.helger/ph-nss/badge.svg)](https://maven-badges.sml.io/sonatype-central/com.helger/ph-nss/)
[![javadoc](https://javadoc.io/badge2/com.helger/ph-nss/javadoc.svg)](https://javadoc.io/doc/com.helger/ph-nss)

> If this project saved you some time or made your day a little easier, a star would mean a lot — it helps others find it too.
<!-- ph-badge-end -->

Mozilla NSS root certificate trust store for Java applications.

This library ships the root CA certificates of the [Mozilla CA Certificate Program](https://wiki.mozilla.org/CA)
as a ready-to-use Java `KeyStore`, so that a TLS client can verify server certificates against a
well-defined and reproducible set of roots instead of whatever the JVM happens to have in its
`cacerts` file.

The class `MozillaNSSTrustStore` was part of `peppol-commons` up to and including v12.x and moved
here with `peppol-commons` v13.0.0.

# Maven

Replace `x.y.z` with the real version number.

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ph-nss</artifactId>
  <version>x.y.z</version>
</dependency>
```

# Usage

The trust store is a PKCS#12 key store on the class path at `truststore/mozilla-nss-root-certs.p12`
with the password `changeit`. It contains only certificates that Mozilla trusts for TLS server
authentication (`CKT_NSS_TRUSTED_DELEGATOR` for `CKA_TRUST_SERVER_AUTH`).

Create an `SSLContext` that trusts exactly these roots:

```java
final TrustManagerFactory aTMF = TrustManagerFactory.getInstance (TrustManagerFactory.getDefaultAlgorithm ());
aTMF.init (MozillaNSSTrustStore.TRUSTSTORE);

final SSLContext aSSLContext = SSLContext.getInstance ("TLS");
aSSLContext.init ((KeyManager []) null, aTMF.getTrustManagers (), null);
```

Alternatively use `MozillaNSSTrustStore.TRUSTSTORE_DESCRIPTOR` to load the trust store via the
ph-commons `ITrustStoreDescriptor` API.

The list of contained certificates, including their aliases and validity periods, is in
[`src/main/resources/truststore/mozilla-nss-root-certs.md`](src/main/resources/truststore/mozilla-nss-root-certs.md).

# Updating the trust store

The trust store is derived from the Mozilla NSS
[`certdata.txt`](https://hg-edge.mozilla.org/projects/nss/raw-file/tip/lib/ckfw/builtins/certdata.txt)
file. Mozilla changes it every couple of weeks. To create a new version:

1. Run `MainConvertNSSCertData` (in `src/test/java`). It downloads `certdata.txt` and `nssckbi.h`
   (cached for 24 hours in the temp directory), writes `truststore/mozilla-nss-root-certs.p12` and
   regenerates `truststore/mozilla-nss-root-certs.md` with the trust list version and the contained
   certificates.
2. Run `MainCreateTrustStoreHashFiles` to refresh the `.md5` and `.sha256` sidecar files.
3. Add a news entry above and release a new version.

# News and Noteworthy

v1.0.0 - 2026-09-23
* Initial version
* Extracted `MozillaNSSTrustStore` and the `MainConvertNSSCertData` conversion tool from `peppol-commons` v12.4.2
* **Breaking API change** compared to `peppol-commons`: the package changed from `com.helger.peppol.security` to `com.helger.nss`
* The contained trust list is Mozilla NSS v2.90 (2026-09-10)

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
It is appreciated if you star the GitHub project if you like it.
