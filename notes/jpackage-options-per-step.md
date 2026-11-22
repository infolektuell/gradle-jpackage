# Jpackage options per build step

Source audit: OpenJDK **25 GA** ([`6c48f4ed707b`](https://github.com/openjdk/jdk/tree/6c48f4ed707bf0b15f9b6098de30db8aae6fa40f/src/jdk.jpackage)), **26 GA** ([`4408cd2a07a1`](https://github.com/openjdk/jdk/tree/4408cd2a07a14243a58cd9d30813302bfbe81133/src/jdk.jpackage)) and **27 GA** ([`815ff4dc327f`](https://github.com/openjdk/jdk/tree/815ff4dc327fe17f2433c7d115a5a503af10f3c4/src/jdk.jpackage)), from `~/git/lib/jdk`. The findings apply only to these releases.

How options were classified:

- **Accepted:** whether jpackage rejects the option for this kind of invocation.
  - **JDK 25:** `ValidOptions` (`ALL` / `LAUNCHER` / `INSTALL` / `SIGN`) plus `Arguments.validateArguments()`.
  - **JDK 26/27:** option scopes in `cli/StandardOption` and `OptionsAnalyzer`. These match the OpenJDK golden test table `test/jdk/tools/jpackage/junit/.../cli/jpackage-options.md`.
- **Considered:** whether the value affects the output. This was traced through `FromParams`/`*FromParams` (25), `FromOptions`/`*FromOptions` (26/27), `LauncherFromOptions`, `OptionsTransformer` and the packagers.

An option is listed under a step only if it is **considered** there. Options that are accepted but have no effect are listed separately for each step.

**Legend:**

- **Versions column:** the releases in which the option is considered at that step. "All" means 25, 26 and 27. A release that is not listed rejects the option.
- **Platforms:** **L** = Linux, **M** = macOS, **W** = Windows. "All" means all three.
- **Summary matrix:** ✓ = considered; ✗ = rejected; — = not applicable.

## Option inventory and version differences

All three releases have the same option set with two exceptions:

| Change | Versions |
| --- | --- |
| `--win-with-ui` (W, msi/exe: forces installer UI) | 27 |
| `--verbose` takes an optional category list (`[-]key,…`: `all`, `console`, `errors`, `progress`, `resources`, `summary`, `tools`, `trace`, `warning`, `log`) | 27 |
| `--mac-app-store` accepted when signing an existing app image (step 3) | 27 |
| `--resource-dir` accepted when signing an existing app image (step 3) | 26, 27 |
| `--license-file`, `--install-dir` and (L/W) `--file-associations` accepted with `--type app-image` (silently ignored) | 26, 27 |
| `--mac-package-identifier` value validated (`error.parameter-not-mac-bundle-identifier`) | 27 |
| macOS `--app-image` must be a valid `.app` bundle (`error.parameter-not-mac-bundle`) | 27 |
| `.jpackage.xml` records a macOS `signed` flag; packaging reads sign state from it | 25, 26 |

JDK 26 replaced the legacy `Arguments`/`ValidOptions`/bundler-parameters CLI with the scope-based `jdk.jpackage.internal.cli` package. Error messages and some validation order changed. Accepted options per step are otherwise unchanged except as listed above.

## How the four steps map to invocations

| Step | Invocation | Platforms |
| --- | --- | --- |
| 1. Runtime image | `jlink …` (normally run internally by jpackage; run separately here) | All |
| 2. App image | `jpackage --type app-image --runtime-image <rt> …` | All |
| 3. Sign app image | `jpackage --type app-image --app-image <X.app> --mac-sign …` | M only |
| 4. Installer | `jpackage --type <deb\|rpm\|msi\|exe\|dmg\|pkg> --app-image <image> …` | All |

General rules for the split (all releases):

- `--runtime-image` cannot be combined with `--add-modules`, `--jlink-options`, or `--app-image` (`ERR_MutuallyExclusiveOptions`).
- `--main-jar` and `--module` cannot be combined.
- `--mac-signing-key-user-name` cannot be combined with `--mac-app-image-sign-identity` or `--mac-installer-sign-identity`.
- With `--app-image` (steps 3 and 4), the launcher and runtime options are rejected. These are `--input`, `--main-jar`, `--main-class`, `--module`, `--module-path`, `--add-modules`, `--jlink-options`, `--arguments`, `--java-options`, `--add-launcher`, `--app-content`, `--runtime-image` and `--win-console`.

---

## Step 1 – Runtime image (jlink)

When jpackage builds the runtime itself, it calls the `jlink` tool provider with the options below. To get the same runtime from a separate jlink run, reproduce them. Supplying `--runtime-image` skips this step.

| jpackage option | Platforms | Versions | What jpackage passes to jlink |
| --- | --- | --- | --- |
| `--module-path`, `-p` | All | All | `--module-path`. **26/27:** if no entry contains `java.base.jmod`, the JDK's default module path (`$JAVA_HOME/jmods`) is appended. |
| `--add-modules` | All | All | Root modules. The phony names `ALL-MODULE-PATH` and `ALL-DEFAULT` are expanded by jpackage before calling jlink. |
| `--module`, `-m` | All | All | The launcher's main module is added to `--add-modules`. |
| `--add-launcher` | All | All | The `module` property of each additional launcher is added to `--add-modules`. |
| `--jlink-options` | All | All | Replaces the default options (below) completely. `--output`, `--add-modules` and `--module-path` are blocked (`error.blocked.option`). |
| `--mac-app-store` | M | All | Validation only. If `--jlink-options` is given, it must contain `--strip-native-commands` (`ERR_MissingJLinkOptMacAppStore`). The runtime must not contain `bin/`. |
| `--runtime-image` | All | All | Skips jlink. The runtime is copied instead, excluding `jmods/` and `src.zip`. |

Defaults when the corresponding option is absent:

- **Default jlink options:** `--strip-native-commands --strip-debug --no-man-pages --no-header-files`.
- **Default modules:** applies if no launcher is modular and `--add-modules` is absent. jpackage uses `ALL-DEFAULT`: every system module that exports an unqualified API, plus anything they require. Resolution is **without service binding**, as in JEP 343/392.
- **JDK 26/27:** the generated `--add-modules` list is sorted. This has no functional effect.
- **macOS:** jpackage writes the runtime into `Contents/runtime/Contents/Home` inside the bundle. A predefined runtime may be a plain directory or a `.jdk`-style bundle.

- `--input` and `--main-jar` do not affect which modules are selected; non-modular apps always get `ALL-DEFAULT`.
- jpackage supports `--limit-modules` internally but has no CLI option for it.

---

## Step 2 – Application image

Invocation: `jpackage --type app-image` with `--runtime-image` from step 1. Do not use `--mac-sign`, because signing is step 3.

### Common options

| Option | Platforms | Versions | Effect |
| --- | --- | --- | --- |
| `--type app-image`, `-t` | All | All | Selects the app-image operation. |
| `--dest`, `-d` | All | All | Output directory. |
| `--temp` | All | All | Work directory, which is kept. It must be empty or not exist. |
| `--verbose` | All | All | Logging. 27 accepts categories. |
| `--name`, `-n` | All | All | Image/bundle name, main launcher name, `.cfg` name, recorded in `.jpackage.xml`. |
| `--app-version` | All | All | Recorded in `.jpackage.xml` and as `-Djpackage.app-version` in every `.cfg`. Also used for W executable version resources and M `CFBundleVersion`. Default: the main module version, or `1.0`. |
| `--description` | All | All | Main launcher description in `.jpackage.xml`. W `FileDescription`. Default for additional launchers. |
| `--vendor` | W, M | All | W executable `CompanyName`; M Info.plist vendor. No effect on Linux images. |
| `--copyright` | W, M | All | W `LegalCopyright`; M `NSHumanReadableCopyright`. No effect on Linux images. |
| `--icon` | All | All | W `.ico` embedded in executables; M `.icns` for the bundle; L `.png` copied to `lib/`. |
| `--input`, `-i` | All | All | Copied to `app/`. Its JARs form the classpath. |
| `--main-jar` | All | All | Non-modular entry point. Requires `--input`. |
| `--main-class` | All | All | Main class. 26/27 also apply it to `--module` launchers; 25 ignores it there. |
| `--module`, `-m` | All | All | Modular entry point `module[/class]`. Requires `--module-path` or `--runtime-image`. |
| `--module-path`, `-p` | All | All | With `--runtime-image`, modules not in the JDK's default module path are copied to `app/mods`. |
| `--arguments` | All | All | Default application arguments in `.cfg`. |
| `--java-options` | All | All | JVM options in `.cfg`. |
| `--add-launcher name=file` | All | All | Additional launchers. Properties are recorded in `.jpackage.xml`, including installer-only keys such as `launcher-as-service`, `linux-shortcut`, `win-shortcut` and `win-menu`, for use in step 4. |
| `--app-content` | All | All | Extra files and directories. On macOS they go under `Contents/`. |
| `--runtime-image` | All | All | The runtime from step 1. |
| `--resource-dir` | All | All | Overrides image resources: icons; M `Info.plist`, runtime plist and entitlements; W `<launcher>.properties` version-info template. A `<name>-post-image` script runs after the image is built. |

### Platform-specific options

| Option | Platforms | Versions | Effect |
| --- | --- | --- | --- |
| `--win-console` | W | All | Console instead of GUI launcher executable. Also a property key for additional launchers. |
| `--file-associations` | M | All | Document types and UTIs written to `Info.plist`, plus icons. On macOS this must happen here, because step 4 does not rewrite the bundle. |
| `--mac-package-name` | M | All | `CFBundleName`. |
| `--mac-package-identifier` | M | All | `CFBundleIdentifier`. Also the default signing prefix. 27 validates the value. |
| `--mac-app-category` | M | All | `LSApplicationCategoryType`. |
| `--mac-app-store` | M | All | Runtime must not contain `bin/`. Recorded as `app-store` in `.jpackage.xml`, so a later signing step uses App Store certificates and the sandbox entitlements. |

### Accepted at this step but not considered

- **All releases:** `--mac-sign`, `--mac-app-image-sign-identity`, `--mac-signing-key-user-name`, `--mac-signing-keychain`, `--mac-package-signing-prefix`, `--mac-entitlements`. These are not ignored: they sign the image in this same step. Omit them in the split flow and pass them in step 3.
- **JDK 26/27 only** (25 rejects these with `ERR_InvalidTypeOption`): `--license-file`, `--install-dir`, and `--file-associations` on L/W. They are installer inputs and must be passed again in step 4.
- **All releases, mutually exclusive with `--runtime-image`:** `--add-modules`, `--jlink-options`. These belong to step 1.

Rejected for `--type app-image` in all releases: `--about-url`, `--launcher-as-service`, `--mac-dmg-content`, `--mac-installer-sign-identity`, all `--linux-*` options, and all `--win-*` options except `--win-console`.

---

## Step 3 – Sign the app image (macOS only)

Invocation: `jpackage --type app-image --app-image <X.app> --mac-sign …`. jpackage signs **in place** and rewrites `.jpackage.xml`. No new image is created, so `--dest`, `--name` and `--temp` are rejected (`ERR_InvalidOptionWithAppImageSigning`).

| Option | Versions | Effect |
| --- | --- | --- |
| `--type app-image`, `-t` | All | Combined with `--app-image` on macOS, selects the signing operation. |
| `--app-image` | All | The `.app` to sign. 27 validates that it is a bundle. |
| `--mac-sign`, `-s` | All | Enables signing. Required in 26/27 (`error.app-image.mac-sign.required`). |
| `--mac-app-image-sign-identity` | All | Explicit codesign identity. |
| `--mac-signing-key-user-name` | All | Selects the certificate `Developer ID Application: <name>`, or `3rd Party Mac Developer Application: <name>` for the App Store. Cannot be combined with the identity options. |
| `--mac-signing-keychain` | All | Keychain used to look up the identity. |
| `--mac-package-signing-prefix` | All | Prefix for the signing identifiers of nested executables and libraries. Default: `--mac-package-identifier` + `.`, taken from the bundle. |
| `--mac-entitlements` | All | Entitlements file. Default: the built-in `entitlements.plist`, or `sandbox.plist` for the App Store. |
| `--mac-app-store` | 27 | Selects App Store certificates and the sandbox entitlements. In 25/26 the value comes only from `app-store` in `.jpackage.xml`, recorded in step 2. In 27 the CLI value wins and `.jpackage.xml` is the fallback. |
| `--resource-dir` | 26, 27 | `<name>.entitlements` overrides the default entitlements resource. |
| `--verbose` | All | Logging. |

`--mac-installer-sign-identity` is rejected here in all releases: 25 via an explicit `app-image` type check, 26/27 because its scope is pkg only.

The bundle metadata (`CFBundleIdentifier`, name, category) is read from the image's `Info.plist`. It must already be correct after step 2.

---

## Step 4 – Installer from the app image

Invocation: `jpackage --type <installer> --app-image <image> …`.

When a predefined app image is used, jpackage reads `.jpackage.xml` and the platform metadata from the image. The **app** name, version and launchers, including their description, service flag and shortcut hints, come from the image. CLI `--name`, `--app-version`, `--description` and `--icon` therefore affect only the **package**, not the launchers. On macOS the bundle is copied unchanged. A signed bundle is never re-signed and gets no `.package` file.

### Common options

| Option | Platforms | Versions | Effect |
| --- | --- | --- | --- |
| `--type`, `-t` | All | All | `deb`, `rpm`, `msi`, `exe`, `dmg`, `pkg`. |
| `--app-image` | All | All | Image from step 2, or step 3 on macOS. |
| `--dest`, `-d` | All | All | Output directory. |
| `--temp` | All | All | Work directory. |
| `--verbose` | All | All | Logging. |
| `--name`, `-n` | All | All | Package name and file name. Default: the app name from the image. |
| `--app-version` | All | All | Package version. Default: the version from the image. Normalized per format: rpm `-`→`.`, W 2–4 components, M 1–3 components. |
| `--description` | All | All | Package description and summary. |
| `--vendor` | L, W | All | L package vendor and DEB `Maintainer` name. W MSI manufacturer, EXE installer resources, and the derived default MSI UpgradeCode/ProductCode (with `--name`). |
| `--copyright` | L (deb), W (exe) | All | DEB `copyright` file; EXE installer version resources. |
| `--icon` | W (exe), M (dmg) | All | EXE installer icon; DMG volume icon. Launcher and desktop icons come from the image. |
| `--about-url` | L, W | All | DEB `Homepage`, RPM `URL`, MSI `ARPURLINFOABOUT`. Accepted on M without effect. |
| `--license-file` | All | All | DEB copyright, RPM license, MSI/EXE license dialog, DMG license, PKG license page. |
| `--install-dir` | All | All | Installation directory. L absolute path; W relative to Program Files or LocalAppData; M under `/Applications`. |
| `--file-associations` | L, W | All | L desktop/MIME registration; W registry associations. Accepted on M without effect, because the bundle is not rewritten. Use step 2 on macOS. |
| `--launcher-as-service` | All | All | Registers the main launcher as a systemd unit, launchd agent or Windows service. Additional launchers use `launcher-as-service` from `.jpackage.xml`. |
| `--resource-dir` | All | All | Installer templates: DEB `control`/scripts, RPM `.spec`, WiX `.wxs`/`.wxl`, DMG setup script/background, PKG scripts/background, systemd/launchd templates. W `service-installer.exe` for services. |

### Linux

| Option | Types | Versions | Effect |
| --- | --- | --- | --- |
| `--linux-package-name` | deb, rpm | All | Package name. Default: derived from `--name`. |
| `--linux-deb-maintainer` | deb | All | Maintainer email. |
| `--linux-rpm-license-type` | rpm | All | RPM `License:`. |
| `--linux-app-release` | deb, rpm | All | Release/revision. |
| `--linux-app-category` | deb, rpm | All | DEB `Section` / RPM `Group`. |
| `--linux-package-deps` | deb, rpm | All | Additional dependencies. |
| `--linux-menu-group` | deb, rpm | All | Desktop entry `Categories`. |
| `--linux-shortcut` | deb, rpm | All | Desktop entry for the main launcher. Additional launchers use `.jpackage.xml`. |

### Windows

| Option | Types | Versions | Effect |
| --- | --- | --- | --- |
| `--win-upgrade-uuid` | msi, exe | All | MSI UpgradeCode. |
| `--win-per-user-install` | msi, exe | All | Per-user instead of per-machine install. |
| `--win-dir-chooser` | msi, exe | All | Installation directory dialog. |
| `--win-menu` | msi, exe | All | Start menu shortcut for the main launcher. |
| `--win-menu-group` | msi, exe | All | Start menu folder. |
| `--win-shortcut` | msi, exe | All | Desktop shortcut for the main launcher. |
| `--win-shortcut-prompt` | msi, exe | All | Dialog for choosing shortcuts. |
| `--win-help-url` | msi, exe | All | `ARPHELPLINK`. |
| `--win-update-url` | msi, exe | All | `ARPURLUPDATEINFO`. |
| `--win-with-ui` | msi, exe | 27 | Forces installer UI. |

### macOS

| Option | Types | Versions | Effect |
| --- | --- | --- | --- |
| `--mac-dmg-content` | dmg | All | Extra content in the DMG root. |
| `--mac-sign`, `-s` | pkg | All | Enables PKG signing. jpackage never signs DMGs. |
| `--mac-installer-sign-identity` | pkg | All | `productbuild --sign` identity. |
| `--mac-signing-key-user-name` | pkg | All | Selects `Developer ID Installer: <name>`, or `3rd Party Mac Developer Installer: <name>` for the App Store. |
| `--mac-signing-keychain` | pkg | All | Keychain for the installer identity. |
| `--mac-app-store` | pkg | All | Selects the App Store installer certificate. |
| `--mac-package-identifier` | pkg | All | Base of the PKG component/package identifiers. Default: `CFBundleIdentifier` from the image. |
| `--mac-package-name`, `--mac-app-category` | dmg, pkg | All | Accepted. They override the `Info.plist` values only in jpackage's internal model and do not rewrite the bundle. |

How the app's sign state is determined during packaging differs by release:

- **JDK 25/26:** read from `.jpackage.xml` (`signed`, `app-store`). CLI `--mac-sign` only controls PKG signing.
- **JDK 27:** the `signed` flag is gone and the CLI decides. If the image is already signed (detected with `spctl`), it is not re-signed. If it is unsigned and `--mac-sign` is given with an app-image identity, jpackage signs the copied bundle during packaging. `--mac-sign` with only `--mac-installer-sign-identity` signs only the PKG.

The remaining app-image signing options (`--mac-app-image-sign-identity`, `--mac-package-signing-prefix`, `--mac-entitlements`) are accepted in step 4. They have no effect once the image was signed in step 3.

### Rejected at this step (all releases)

- Launcher and runtime inputs: `--input`, `--main-jar`, `--main-class`, `--module`, `--module-path`, `--add-modules`, `--jlink-options`, `--arguments`, `--java-options`, `--add-launcher`, `--app-content`, `--win-console`.
- `--runtime-image`, which is mutually exclusive with `--app-image`.

---

## Summary matrix

✓ = considered. (✓) = accepted but no effect in the split flow. ✗ = rejected. Version footnotes are in the tables above.

| Option | Platforms | 1 jlink | 2 app image | 3 sign (M) | 4 installer |
| --- | --- | :---: | :---: | :---: | :---: |
| `--type` | All | — | ✓ | ✓ | ✓ |
| `--dest`, `--temp` | All | — | ✓ | ✗ | ✓ |
| `--verbose` | All | — | ✓ | ✓ | ✓ |
| `--name` | All | — | ✓ | ✗ | ✓ |
| `--app-version`, `--description` | All | — | ✓ | ✗ | ✓ |
| `--vendor`, `--copyright` | All | — | ✓ W/M | ✗ | ✓ L/W |
| `--icon` | All | — | ✓ | ✗ | ✓ W exe / M dmg |
| `--input`, `--main-jar`, `--main-class`, `--arguments`, `--java-options`, `--app-content` | All | — | ✓ | ✗ | ✗ |
| `--module` | All | ✓ | ✓ | ✗ | ✗ |
| `--module-path` | All | ✓ | ✓ | ✗ | ✗ |
| `--add-launcher` | All | ✓ | ✓ | ✗ | ✗ |
| `--add-modules`, `--jlink-options` | All | ✓ | ✗ with `--runtime-image` | ✗ | ✗ |
| `--runtime-image` | All | replaces | ✓ | ✗ | ✗ |
| `--app-image` | All | — | — | ✓ | ✓ |
| `--resource-dir` | All | — | ✓ | ✓ 26+ | ✓ |
| `--file-associations` | All | — | ✓ M; (✓) L/W 26+ | ✗ | ✓ L/W; (✓) M |
| `--license-file`, `--install-dir` | All | — | (✓) 26+, ✗ 25 | ✗ | ✓ |
| `--about-url`, `--launcher-as-service` | All | — | ✗ | ✗ | ✓ |
| `--win-console` | W | — | ✓ | — | ✗ |
| `--win-*` installer options | W | — | ✗ | — | ✓ (`--win-with-ui` 27+) |
| `--linux-*` | L | — | ✗ | — | ✓ |
| `--mac-package-name`, `--mac-package-identifier`, `--mac-app-category` | M | — | ✓ | ✗ | (✓); identifier ✓ for pkg |
| `--mac-app-store` | M | validation | ✓ | ✓ 27+ | ✓ pkg |
| `--mac-sign`, `--mac-signing-key-user-name`, `--mac-signing-keychain` | M | — | (✓) signs in place | ✓ | ✓ pkg |
| `--mac-app-image-sign-identity`, `--mac-package-signing-prefix`, `--mac-entitlements` | M | — | (✓) signs in place | ✓ | (✓) |
| `--mac-installer-sign-identity` | M | — | ✗ | ✗ | ✓ pkg |
| `--mac-dmg-content` | M | — | ✗ | ✗ | ✓ dmg |

## Primary sources

- JDK 25: [`ValidOptions`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/ValidOptions.java), [`Arguments`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/Arguments.java), [`FromParams`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/FromParams.java), [`MacFromParams`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/MacFromParams.java)
- JDK 26/27: [`StandardOption`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/cli/StandardOption.java), [`StandardBundlingOperation`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/cli/StandardBundlingOperation.java), [`OptionsAnalyzer`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/cli/OptionsAnalyzer.java), [`jpackage-options.md` (golden scope table)](https://github.com/openjdk/jdk/blob/jdk-27-ga/test/jdk/tools/jpackage/junit/share/jdk.jpackage/jdk/jpackage/internal/cli/jpackage-options.md), [`FromOptions`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/FromOptions.java), [`OptionsTransformer`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/OptionsTransformer.java), [`MacFromOptions`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/MacFromOptions.java), [`MacPackagingPipeline`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/MacPackagingPipeline.java)
- jlink defaults: [`RuntimeBuilderBuilder`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/RuntimeBuilderBuilder.java), [`JLinkRuntimeBuilder`](https://github.com/openjdk/jdk/blob/jdk-27-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/JLinkRuntimeBuilder.java)
- Launcher-level effects of these options: [jpackage-launcher-options.md](jpackage-launcher-options.md)
