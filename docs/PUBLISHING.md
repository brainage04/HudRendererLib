# Publishing
Publishing is configured for Sonatype Central via the OSSRH Staging API compatibility service.

Before publishing, create a Sonatype Central account, verify the `io.github.brainage04` namespace,
generate a Central Portal user token, and create a GPG key for signing releases.

Export the required secrets like so:

On Linux/macOS, append these commands to the end of `~/.bashrc`:
```bash
export CENTRAL_PORTAL_USERNAME='<central_portal_token_username>'
export CENTRAL_PORTAL_PASSWORD='<central_portal_token_password>'
export SIGNING_KEY="$(cat << 'EOF'
paste output of `gpg --armor --export-secret-keys <your_key_id>` here
EOF
)"
export SIGNING_PASSWORD='<gpg_key_password>'
```

On Windows, append these commands to the end of `$PROFILE`:
```powershell
$env:CENTRAL_PORTAL_USERNAME = "<central_portal_token_username>"
$env:CENTRAL_PORTAL_PASSWORD = "<central_portal_token_password>"
$env:SIGNING_KEY = @"
paste output of `gpg --armor --export-secret-keys <your_key_id>` here
"@
$env:SIGNING_PASSWORD = "<gpg_key_password>"
```

Then publish with:
```bash
./gradlew publishToMavenCentral
```

By default, the staged deployment is uploaded to the Central Portal for manual review (`user_managed` mode).
To ask Sonatype to release automatically after validation, run:
```bash
./gradlew publishToMavenCentral -PcentralPublishingType=automatic
```