# Shared Compose UI library

Design tokens, `RedBullTheme`, `ProductCard` and shared search/loading/empty/error components. This folder is the root of the UI library's own GitHub repository. The current implementation keeps colors, dimensions and strings in `theme/Colors.kt`, `theme/Dimensions.kt` and `theme/Strings.kt`; the shared components read those tokens.

## Local build and publication

Requires JDK 17 and Android SDK Platform 36. From this directory:

```bash
./gradlew test assembleDebug
./gradlew publishToMavenLocal
```

The Maven group, artifact and default version are in `buildSrc/src/main/kotlin/LibraryCoordinates.kt`. The current default version is `0.1.5`. The publish workflow runs after changes are integrated into `main`: it reads the version, skips a release tag that already exists, validates and publishes `com.redbull.lab:shared-ui:<version>` to GitHub Packages, then creates the matching GitHub Release. Increment `VERSION_NAME` before the next release.

The workflow uses `GITHUB_TOKEN`; no personal token is stored in the repository. Consumer repos need `Read` access to this package. See the parent lab's `TUTORIAL.md` for the full four-repo release flow.

## Build versions

The catalog uses AGP 8.13.2, Kotlin 2.3.21, Compose BOM 2026.06.00 and Gradle 8.13. This is the latest Compose line compatible with the project's API 36 / AGP 8 setup; Compose 1.12 requires compileSdk 37 and AGP 9, which would require a broader build migration.

The POC has no unit or instrumented test sources yet; `test` completes with `NO-SOURCE`, while `assembleDebug` compiles the library. See [PRACTICA_PASO_A_PASO.md](PRACTICA_PASO_A_PASO.md) for the file-by-file guide.
