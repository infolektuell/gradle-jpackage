# Which jpackage options affect launchers?

Source audit: OpenJDK **25 GA** ([`6c48f4ed707b`](https://github.com/openjdk/jdk/tree/6c48f4ed707bf0b15f9b6098de30db8aae6fa40f/src/jdk.jpackage)) and **26 GA** ([`4408cd2a07a1`](https://github.com/openjdk/jdk/tree/4408cd2a07a14243a58cd9d30813302bfbe81133/src/jdk.jpackage)), available in `~/git/lib/jdk`. JDK 25 sources were extracted from its Git tag without changing the checkout. Findings are version-specific, not a claim about every JDK release.

Here, “launcher” includes its executable, `.cfg`, and OS-visible metadata (macOS `.app` bundle, Windows executable resources, Linux desktop entry). Installer integration is identified separately. Unless stated otherwise, the results apply to both audited releases when **creating a new application image**, either directly or as part of an installer.

Packaging an existing `--app-image` reuses its launchers; creation options do not retrofit them. macOS also supports a separate mode for signing an existing image.

## Common options

| Option | Actual effect on all three platforms |
| --- | --- |
| `--name` / `-n` | Main executable and configuration filenames; application image name. Also feeds platform metadata. |
| `--main-jar`, `--main-class`, `--module` / `-m` | Java entry point in `.cfg`: JAR, class, or module/class. Without an explicit class, jpackage reads the JAR/module main-class metadata. JDK 26 also uses `--main-class` with `--module`; JDK 25 ignores that separate option for modular launchers. |
| `--arguments` | Default application arguments in `.cfg`. Arguments supplied when running the launcher replace these defaults. |
| `--java-options` | JVM options in `.cfg`. |
| `--app-version` | Writes `-Djpackage.app-version=…` into **every** launcher's `.cfg`; also affects Windows version resources and macOS bundle versions. |
| `--add-launcher name=file` | Adds another executable and `.cfg` using properties from the file, with inherited settings where applicable. |

The native launcher normally comes from a prebuilt JDK resource; these options mostly change its name/configuration. Windows additionally patches executable resources, and macOS signing can change executable bytes. See [`CfgFile`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/CfgFile.java) and [`ApplicationImageUtils`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/ApplicationImageUtils.java).

**Inputs and runtime affect launchability indirectly:** `--input` supplies application files and determines the scanned JAR classpath; `--module-path` resolves modules and can supply modules copied into the image. `--runtime-image`, or `--add-modules` / `--jlink-options` when building a runtime, determines the JVM/modules available to the launcher. `--app-content` adds payload, without a dedicated launcher setting. These should not be confused with per-launcher properties.

## Platform differences

| Option | macOS | Linux | Windows |
| --- | --- | --- | --- |
| `--icon` | `.icns` for the whole `.app` bundle | Separate `.png` in the image; used for desktop integration, not embedded in the executable | `.ico` embedded in each executable |
| `--description` | No standard launcher/bundle field | Installer desktop entry `Comment` / systemd service description | Executable `FileDescription` |
| `--copyright` | Bundle `NSHumanReadableCopyright` | No launcher effect | Executable `LegalCopyright` |
| `--vendor` | No launcher effect | No launcher effect | Executable `CompanyName` |
| `--resource-dir` | Can override bundle icon, `Info.plist`, runtime plist, entitlements | Can override launcher icons and installer desktop/service templates | Can override icons and `<launcher>.properties` executable-branding template |
| `--file-associations` | Writes document/UTI declarations and icons into the `.app`, including with `--type app-image` | Installer association/desktop integration | Installer registry associations |

Evidence: [`MacPackagingPipeline`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/MacPackagingPipeline.java), [`LinuxPackagingPipeline`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/linux/classes/jdk/jpackage/internal/LinuxPackagingPipeline.java), [`DesktopIntegration`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/linux/classes/jdk/jpackage/internal/DesktopIntegration.java), [`ExecutableRebrander`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/windows/classes/jdk/jpackage/internal/ExecutableRebrander.java).

- **macOS:** `--mac-package-name`, `--mac-package-identifier`, and `--mac-app-category` set bundle name, identifier, and category. `--mac-sign`, `--mac-app-image-sign-identity`, `--mac-signing-key-user-name`, `--mac-signing-keychain`, `--mac-package-signing-prefix`, and `--mac-entitlements` control image signing when requested. `--mac-app-store` affects App Store preparation/signing, including default entitlements. These apply to the bundle as a whole, not independently to additional launchers. `--mac-installer-sign-identity` signs the PKG installer; `--mac-dmg-content` adds DMG content.
- **Linux:** `--linux-shortcut` requests installer desktop integration; `--linux-menu-group` supplies desktop categories. Neither changes the executable or `.cfg`. `--linux-app-category` sets RPM Group / DEB Section, not desktop categories.
- **Windows:** `--win-console` selects a console executable instead of the default GUI executable. `--win-shortcut`, `--win-menu`, and `--win-menu-group` configure installer shortcuts, not the executable.
- **All:** `--launcher-as-service` configures installer service registration (launchd/systemd/Windows service support), not a different Java entry point. `--install-dir` controls installed paths. Remaining package metadata/UI options do not configure the generated launcher; `--dest`, `--temp`, `--verbose`, `@file`, and help/version switches control the packaging invocation.

## Additional-launcher properties and help inaccuracies

JDK 25 uses one shared property list in help on all platforms. Its effective additional-launcher keys are:

| Platform | Effective keys |
| --- | --- |
| All | `module`, `main-jar`, `main-class`, `arguments`, `java-options`; `launcher-as-service` for installer integration |
| Windows | Also `description`, `icon`, `win-console`, `win-shortcut`, `win-menu` |
| Linux | Also `description` for desktop entries, `icon`, `linux-shortcut` |
| macOS | No independent icon or description is emitted for additional launchers: they are executables inside the same `.app` |

Property values replace inherited `arguments` / `java-options`, rather than append; an empty value clears them. A modular properties file can use `module=moduleName` plus `main-class=qualified.Class`. See JDK 25 [`AddLauncherArguments`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/AddLauncherArguments.java) and [`FromParams`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/FromParams.java).

Confirmed errors and omissions in the **CLI help**, not just the online manual:

| Help claim or placement | Source behavior / correction |
| --- | --- |
| JDK 25 advertises `app-version` in additional-launcher files | Parsed but unused. Both `.cfg` and Windows version resources take the application-wide version. JDK 26 removes this key from help. |
| JDK 25 advertises `linux-app-category` in additional-launcher files | Parsed but unused for that launcher. Category belongs to the package. JDK 26 removes it from help. |
| JDK 25 advertises other platforms' keys everywhere | Windows/Linux keys are platform-specific. JDK 26 provides separate lists, but macOS still lists `icon` and `description` despite emitting neither independently for additional launchers. |
| `--module` says the module must be on the module path and will be linked | An existing `--runtime-image` can already contain it; no module path or new linking is required. Applies to both releases. |
| `--main-class` says it requires `--main-jar` | False in JDK 26: it also selects a modular entry class. With `--module module/class`, that embedded class wins. |
| macOS identifier defaults to the main class name | It defaults to the class's **package**, e.g. `org.example.Main` → `org.example`; an unpackaged class uses its simple name. |
| macOS package name must be shorter than 16 characters | Exactly 16 is accepted; longer explicit names only produce a warning and are retained. |
| `--resource-dir` and `--file-associations` appear under package creation | Incomplete scope: resource overrides also affect application images; macOS associations are stored in the application bundle. |
| JDK 25 puts `--mac-installer-sign-identity` in the launcher section | Installer-only; rejected for `--type app-image`. JDK 26 places it among package options. |
| `--add-modules` description names jlink's `--add-module` argument | Typo in both releases: jlink's option is `--add-modules`. |

Help evidence: JDK 25 [`CLIHelp`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/CLIHelp.java) / [`HelpResources`](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/resources/HelpResources.properties); JDK 26 [`StandardHelpFormatter`](https://github.com/openjdk/jdk/blob/jdk-26-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/cli/StandardHelpFormatter.java) / [`HelpResources`](https://github.com/openjdk/jdk/blob/jdk-26-ga/src/jdk.jpackage/share/classes/jdk/jpackage/internal/resources/HelpResources.properties). macOS default/length behavior: [`MacApplicationBuilder`](https://github.com/openjdk/jdk/blob/jdk-26-ga/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/MacApplicationBuilder.java).

**Verification:** inspected generation and consumption paths for all three platforms, and compared platform-selected help resources with this repository's [macOS](mac/jpackage.txt), [Linux](linux/jpackage_linux.txt), and [Windows](windows/jpackage-jdk25-help.txt) captures. The captures differ in age; the tagged help sources are authoritative for the versions above. Ran Linux `--help` and small application builds using installed OpenJDK 25.0.2 and 26.0.1: confirmed default-argument replacement, JDK 25's application-wide version despite an additional launcher's `app-version=9.9.9`, unchanged executable/`.cfg` after changing vendor/description/copyright, modular launch from a predefined runtime without `--module-path`, and JDK 26's modular `--main-class`. macOS/Windows conclusions are source-verified, not native execution tests.
