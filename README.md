# Shared Compose UI library

Design tokens, `RedBullTheme`, `ProductCard` and shared search/loading/empty/error components. This folder is the root of the UI library's own GitHub repository.

## Local build and publication

Requires JDK 17 and Android SDK 35. From this directory:

```bash
./gradlew test assembleDebug
./gradlew publishToMavenLocal
```

The Maven group, artifact, and local default version are in `buildSrc/src/main/kotlin/LibraryCoordinates.kt`. For a release, push a new `vX.Y.Z` tag; `.github/workflows/publish.yml` uses the tag as the Maven version, overriding the local default. Do not republish an existing version.

The workflow publishes into this GitHub repository's Maven package registry. Its `GITHUB_TOKEN` only needs `packages: write`; no personal token is stored in the repo. Consumer repos need `Read` access to this package. See the parent lab's `TUTORIAL.md` for the full four-repo release flow.
