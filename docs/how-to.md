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

```bash
mvn clean deploy -Dgpg.passphrase="<password>" -Prelease
```
where:
- `password` - GPG key passphrase

## Setup project for publishing

[Official guide](https://central.sonatype.org/publish/publish-portal-maven/)
