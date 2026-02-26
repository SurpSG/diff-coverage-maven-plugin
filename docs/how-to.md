# How-to notes

## Signing

To publish a library to Maven Central, it is required to sign the artifacts with a GPG key.

1. Generate a new GPG key
```bash
gpg --full-generate-key
```
> RSA sign only
> size: 3072
> expiration: 4y (4 years)

Consider the entered password as `<YOUR_PASSPHRASE>`

2. List all keys and get the key id
```bash
gpg --list-keys --keyid-format short
```
> Copy the key id (8 characters) from the output
> consider next as <YOUR_KEY_ID>

3. Publish the key
```bash
gpg --keyserver hkp://pgp.mit.edu --send-keys <YOUR_KEY_ID>
```

4. Export the private key for signing
```bash
# ASCII armored
gpg --armor --export-secret-key <YOUR_KEY_ID>
# Consider next as SIGNING_KEY
```

## Publishing the plugin

### Prerequisites

`~/.m2/settings.xml` must contain Maven Central credentials and GPG config:
```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>MAVEN_CENTRAL_USERNAME</username>
            <password>MAVEN_CENTRAL_TOKEN</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>ossrh</id>
            <properties>
                <gpg.executable>gpg</gpg.executable>
                <gpg.passphrase>${env.GPG_PASSPHRASE}</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>ossrh</activeProfile>
    </activeProfiles>
</settings>
```

`GPG_PASSPHRASE` environment variable must be set (e.g. in `~/.zshrc`).

### Local publish

```bash
./mvnw clean deploy -Prelease -B -ntp -DskipTests -Dinvoker.skip=true
```
where:
- `-Prelease` - activates the `release` profile (GPG signing, javadoc JAR, publishing to Maven Central)
- `-B` - batch (non-interactive) mode
- `-ntp` - no transfer progress, reduces log noise
- `-DskipTests` - skips unit tests
- `-Dinvoker.skip=true` - skips integration tests

### CI publish

Triggered via `workflow_dispatch` on a `release/*` branch. See `.github/workflows/release.yml`.

## Setup project for publishing

[Official guide](https://central.sonatype.org/publish/publish-portal-maven/)
