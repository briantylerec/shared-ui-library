# Shared Compose UI library

Design tokens, `RedBullTheme`, `ProductCard` and shared search/loading/empty/error components. This folder is the root of the UI library's own GitHub repository.

## Local build and publication

Requires JDK 17 and Android SDK 35. From this directory:

```bash
./gradlew test assembleDebug
./gradlew publishToMavenLocal
```

The Maven group, artifact, and version are in `buildSrc/src/main/kotlin/LibraryCoordinates.kt`. To release, create a branch such as `v0.1.4`, update `VERSION_NAME` to `0.1.4`, and merge the changes into `main`. The publish workflow runs on the merge, validates and builds the library, publishes `com.redbull.lab:shared-ui:0.1.4` to GitHub Packages, then creates the GitHub Release `v0.1.4` with a ZIP of the Maven files. It skips publication if that version's release tag already exists; use a new version for every release.

The Maven package is published to this GitHub repository's package registry; the Release ZIP is a downloadable copy of the Maven files. Consumer projects should resolve the dependency from GitHub Packages and use `implementation("com.redbull.lab:shared-ui:0.1.4")`. Consumer repos need `Read` access to this package. See the parent lab's `TUTORIAL.md` for the full four-repo release flow.
