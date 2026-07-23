# Publishing
Publishing is configured for Sonatype Central via the OSSRH Staging API compatibility service.

Before publishing, create a Sonatype Central account, verify the `io.github.brainage04` namespace,
generate a Central Portal user token, and create a GPG key for signing releases.

## Create the credentials

1. Sign in to the [Central Publisher Portal](https://central.sonatype.com/), then verify ownership of the `io.github.brainage04` namespace if it is not already verified.
2. Open the [Portal user-token page](https://central.sonatype.com/usertoken), select **Generate User Token**, choose a display name and expiration, and save both generated values. The generated username is `CENTRAL_PORTAL_USERNAME`; the generated password is `CENTRAL_PORTAL_PASSWORD`. These are not the interactive Portal login credentials.
3. Create a passphrase-protected primary signing key:

   ```bash
   gpg --full-generate-key
   gpg --list-secret-keys --keyid-format=long
   ```

   Choose RSA, at least 3072 bits, an expiration that can be maintained, the publisher identity, and a strong unique passphrase. Use the primary key's full fingerprint in the publication command below:

   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys <full-fingerprint>
   gpg --armor --export-secret-keys <full-fingerprint> > /tmp/hudrendererlib-signing-key.asc
   ```

4. In the HudRendererLib GitHub repository, open **Settings → Secrets and variables → Actions → Secrets → New repository secret** and create:
   - `CENTRAL_PORTAL_USERNAME`: generated Portal-token username.
   - `CENTRAL_PORTAL_PASSWORD`: generated Portal-token password.
   - `SIGNING_KEY`: the complete armored contents of `/tmp/hudrendererlib-signing-key.asc`, including the `BEGIN` and `END` lines.
   - `SIGNING_PASSWORD`: the signing-key passphrase.
5. Delete the temporary private-key export after GitHub confirms the secret:

   ```bash
   shred -u /tmp/hudrendererlib-signing-key.asc
   ```

The tag-triggered release workflow uploads both loader coordinates before finalizing one Maven Central deployment. It skips Central only when both coordinates already exist or credentials are missing, and fails safely if only one coordinate exists. GitHub, Modrinth, and CurseForge remain independent.

## Secret handling

Never store publishing secrets in `~/.bashrc`, `$PROFILE`, project files, Gradle command-line arguments, or a repository `.env` file.

### Local workstation

Store the Central Portal token in a password manager. Keep the signing private key in the local GnuPG keyring and let `gpg-agent` provide its passphrase.

Configure only the non-secret GPG selector in `~/.gradle/gradle.properties`:

```properties
signing.gnupg.executable=gpg
signing.gnupg.keyName=<long-signing-key-id>
```

The owner workstation provides `unlock-central-publishing` and `with-central-publishing` helpers. The first creates a short-lived Bitwarden CLI session; the second retrieves the Portal token for one command, enables `useGpgAgentSigning`, locks Bitwarden, and removes the session afterward.

### CI

CI may continue using secret-store-provided environment variables:

- `CENTRAL_PORTAL_USERNAME`
- `CENTRAL_PORTAL_PASSWORD`
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

The build uses in-memory PGP signing when the signing environment variables are present and GPG-agent signing when `useGpgAgentSigning=true`.

## Publish

On the owner workstation:

```bash
unlock-central-publishing
with-central-publishing ./gradlew --no-daemon publishToMavenCentral
```

By default, the staged deployment is uploaded to the Central Portal for manual review (`user_managed` mode). To ask Sonatype to release automatically after validation, run:

```bash
unlock-central-publishing
with-central-publishing ./gradlew --no-daemon publishToMavenCentral -PcentralPublishingType=automatic
```