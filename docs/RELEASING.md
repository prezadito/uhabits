# Releasing Habits+

Releases are built and published by GitHub Actions
(`.github/workflows/release.yml`) when a tag starting with `v` is pushed.

## Checklist

1. **Bump the version** in `uhabits-android/build.gradle.kts`:
   - `versionName`, e.g. `"2.4.2-qol"`
   - `versionCode`, which must be **higher than the previous release's**
     (e.g. 20401 -> 20402). Android only treats an APK as an update if its
     version code is higher; otherwise the install is refused.
2. **Add a changelog section** at the top of `CHANGELOG.md`, directly under
   `## [Unreleased]`:

   ```markdown
   ## [2.4.2-qol] -- YYYY-MM-DD

   ### Fixed

   - Describe the change
   ```

   The heading must be exactly `## [<versionName>]`. The workflow finds the
   section by that version and uses it as the release notes.
3. **Open a PR into `dev` and merge it.** Don't tag until it is merged, so the
   tag points at the merged code.
4. **Tag the merge commit and push the tag:**

   ```
   git checkout dev && git pull
   git tag v2.4.2-qol
   git push origin v2.4.2-qol
   ```

   The tag name must be `v` + `versionName`. The workflow does not check that
   they match, so a typo produces a release with the wrong name or empty notes.
5. **Wait for the build** (about 5 minutes) under the repo's Actions tab. The
   run publishes a GitHub Release with `uhabits-v<version>.apk` attached.
6. **Verify the release** (see below).

## What the workflow does

- Builds the app with Gradle on a fresh runner.
- If the signing secrets exist, builds a **signed release APK**
  (`uhabits-v<version>.apk`). Otherwise it builds a debug-signed APK named
  `uhabits-v<version>-debug.apk`. A `-debug` filename means the secrets were
  missing and the APK cannot update a signed install.
- Copies the matching `CHANGELOG.md` section into the release notes.
- Marks the release as a **pre-release** whenever the tag contains `-`, which
  every `-qol` tag does.

## After the build

1. **Check the release page.** It should have the APK attached, with notes from
   the changelog, and no `-debug` in the filename.
2. **Untick "pre-release"** on the release if you want it shown as the latest
   release (Edit -> "Set as the latest release").
3. **Test an in-place update on your phone.** Download the APK and install it
   over the previous version. It should keep your data and show "Habits+".
   If Android reports a signature conflict, don't uninstall; export a backup
   first, then investigate.
4. Optionally verify the signature:

   ```
   apksigner verify --print-certs uhabits-v2.4.2-qol.apk
   ```

   It should show `CN=Aditya Niloi`, not "Android Debug".

## Signing key

The workflow signs with a keystore stored in repository secrets:

| Secret | Contents |
|---|---|
| `KEYSTORE_BASE64` | `release.jks`, base64-encoded |
| `KEY_ALIAS` | `qol` |
| `KEY_PASSWORD` | the key password (same as the store password) |
| `STORE_PASSWORD` | the keystore password |

- Every release must be signed with this same key, or phones will refuse to
  update from the previous version.
- **Keep `release.jks` and its password backed up** (password manager or
  encrypted storage). If they are lost, nobody can update in place any more and
  everyone must uninstall and reinstall under a new key.
- Never commit the keystore or passwords to the repo.

## Rules and gotchas

- **Never reuse or move a tag.** Pushing a `v*` tag triggers a build, and
  re-pushing a name that was already released creates confusion. For a fix,
  bump the version and tag a new one.
- **Version code only goes up.** Never lower it, and never reuse a number that
  was already published.
- **App ID is `org.isoron.uhabits.qol`.** It installs next to the original
  Loop Habit Tracker with separate data. Don't change it, or the app will no
  longer update in place.
- **The release workflow does not run tests.** Check the build and the app
  yourself before tagging.
- **Deleting a release does not delete its tag.** Remove the tag separately
  with `git push origin --delete <tag>` if you want it gone, and remember that
  pushing it again triggers a new build.
- **Moving data between apps:** the original app and Habits+ keep separate
  data. Use backup export/import to move habits across.

## Troubleshooting

- **Build failed:** open the failed run in the Actions tab and read the failing
  step's log.
- **Release has no APK or a `-debug` APK:** the signing secrets are missing or
  misnamed. Check the four secret names above.
- **Release notes are empty:** the changelog heading does not match the tag.
  `v2.4.2-qol` needs a heading of exactly `## [2.4.2-qol]`. Edit the release
  notes by hand, and fix the changelog.
- **Phone says "App not installed":** either the version code is not higher than
  the installed one, or the signing key differs from the installed version.
