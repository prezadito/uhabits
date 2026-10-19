# Testing the project

Loop Habit Tracker has a fairly large number of automated tests to reduce the chance of bugs being silently introduced in our code base. The tests are divided into three categories:

- **Unit tests:** These tests run very quickly on the developer's computer, inside a JVM, and do not need an Android emulator or device. They typically test the correctness of core functions of the application, such as the computation of scores and streaks.
- **Instrumented tests:** These tests require an Android emulator or device. _Medium_ instrumented tests are still quite fast to run, since only individual classes are tested. The app itself does not need to be launched. Examples include _view tests_, which render our custom views on the device and compare them against prerendered images. _Large_ instrumented tests launch the application on an Android emulator and interact with it by touching the screen, much like a regular user.

## Running unit tests

Unit tests can be launched by running `./gradlew test` or by right-clicking a particular class/method in Android Studio and selecting "Run testMethod()" or "Run ClassTest". An alternative way is to use `build.sh`, the script used by our continuous integration server. By running `./build.sh build`, the script will automatically build and run all small tests.

## Running instrumented tests

To run medium tests, it is recommended to use the `build.sh` script.

1. Run `./build.sh android-setup API` to create the emulator, where `API` is the desired API level.
2. Run `./build.sh android-tests API` to run the tests on a single API.
3. Run `./build.sh android-tests-parallel API API...` to run the tests on multiple APIs in parallel.

Note that instrumented tests are designed to run on a clean install, inside an emulator. They will not work on actual devices. All tests are also designed for a particular screen size, namely the Nexus 4 configuration (4.7" 768x1280 xhdpi), and a particular locale, namely English (US). Furthermore:

- No additional apps should be installed on the device;
- The homescreen must look exactly like it was when the emulator was originally created, with no additional icons or widgets;
- All animations must be manually disabled.

If there are failing view tests (that is, if some custom views do not render exactly like the prerendered images we have), then both the actual and expected images will be automatically downloaded from the device to the folder `uhabits-android/build/outputs/test-screenshots`. After verifying the differences, if you feel that the actual images are actually fine and should replace the prerendered ones, follow [Regenerating the view-test images](#regenerating-the-view-test-images).

## Regenerating the view-test images

Do this once after a change that alters how views look, not once per pull request. These steps are the ones that work for Habits+ (macOS, Apple Silicon).

### One-time setup

1. Install the tools `build.sh` needs: `brew install flock coreutils moreutils`. If `flock` has no formula, use `brew install discoteq/discoteq/flock`. Homebrew installs coreutils' `timeout` as `gtimeout`, so also run `export PATH="$(brew --prefix coreutils)/libexec/gnubin:$PATH"` in the shell you use.
2. Create the emulator: `./build.sh android-setup 36`. Use API 36 (the app's `targetSdk`) and keep using the same level, because text renders slightly differently between Android versions and the images would churn.
3. The emulator wants about 12 GB of free disk for its userdata partition. If it fails with `Not enough space to create userdata partition`, shrink the partition instead of re-running setup (which would reset it): `sed -i '' 's/^disk.dataPartition.size=.*/disk.dataPartition.size=3G/' ~/.android/avd/uhabitsTest36.avd/config.ini`.

### Each time

1. Build the app and the test APK with Gradle, not with `./build.sh build`:
   `./gradlew :uhabits-android:assembleDebug :uhabits-android:assembleAndroidTest`.
   `build.sh build` also runs the Kotlin/JS browser tests, which need Chrome installed.
2. Run `./build.sh android-tests 36`. The view tests fail wherever the images changed. The new images are saved to `uhabits-android/build/outputs/test-screenshots/`: `name.png` is what was rendered and `name.expected.png` is the old reference.
3. **Review the new images before accepting.** Check that each one shows the whole view and looks the way the design intends. A common trap is a test that measures a view at a fixed size from before a redesign, which clips the new layout; the image then looks cut off. Fix the size in the test (see `HabitCardViewTest` and `HintViewTest`) and run again rather than accepting a clipped image.
4. Run `./build.sh android-accept-images`. It deletes the `.expected` files and copies the new images into `uhabits-android/src/androidTest/assets/`.
5. **Rebuild the test APK** with `./gradlew :uhabits-android:assembleAndroidTest`. The reference images are packed into that APK and `android-tests` installs the one that was already built, so without this step the tests still fail against the old images.
6. Run `./build.sh android-tests 36` again. The medium suite should pass. The large suite only starts once the medium suite passes.
7. Commit the images on their own, apart from code changes, so the diff is easy to review.

The core module has its own image tests, run with `./gradlew :uhabits-core:jvmTest` (`./gradlew test` does not run them). When they fail, the rendered images are written to `/tmp/failed/`; review them and copy the good ones into `uhabits-core/assets/test/`.

### Package name

The debug app's application ID is `org.isoron.uhabits.qol.debug`, not `org.isoron.uhabits`. `build.sh` (`PACKAGE_NAME`) and the UI tests (`BaseUserInterfaceTest.PKG`) use it. If tests fail with `Unable to find explicit activity class {org.isoron.uhabits/...}`, a package name has been hardcoded somewhere.

### Known failures

These large tests failed before the redesign too, so they are not caused by a change you made:

- `AboutTest` (both tests): `preferences.xml` hardcodes `targetPackage="org.isoron.uhabits"`, and the test expects the name "Loop Habit Tracker" while the app is called Habits+.
- `BackupTest#shouldExportAndImportBackup` is flaky on the emulator.

When unsure whether a failure is new, check out `dev` in a separate worktree, build it, uninstall the app and the test package from the emulator (`adb uninstall org.isoron.uhabits.qol.debug` and `...debug.test`, since the older version code cannot be installed over a newer one), install the older APKs and run the failing test on its own.

### Debugging a single test

```
adb -s emulator-5554 shell am instrument -w \
  -e class org.isoron.uhabits.acceptance.HabitsTest#shouldCreateHabit \
  org.isoron.uhabits.qol.debug.test/androidx.test.runner.AndroidJUnitRunner
```

- Take screenshots while it runs with `adb exec-out screencap -p > out.png`. The app is closed when the test ends.
- `uiautomator dump` does not work while an instrumentation test is running.
- Google Play services can open system screens on the emulator and take focus from the app, which makes clicks fail. Press Home and try again before blaming the app.
