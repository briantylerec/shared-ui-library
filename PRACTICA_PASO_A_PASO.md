# Guía paso a paso: biblioteca Compose compartida

**Objetivo:** recorrer el proyecto UI que está en esta carpeta, respetando sus cambios actuales: tokens en archivos separados, componentes que consumen esos tokens y publicación automática desde `main`. Cada subpaso indica ruta, propósito y contenido completo del archivo.

## Antes de empezar

- Proyecto: `01-shared-ui-library`; abre una terminal en la raíz de este repositorio.
- Requisitos: JDK 17, Android Studio y Android SDK Platform 36.
- No reemplaces `gradlew`, `gradlew.bat` ni `gradle/wrapper/gradle-wrapper.jar`; el wrapper se actualiza editando solo `gradle-wrapper.properties`.
- No guardes `local.properties`, claves o tokens en Git.

## Paso 1. Configurar Gradle y las coordenadas Maven

Primero configura el proyecto independiente, su módulo `:designsystem`, el wrapper compatible, las versiones y las coordenadas del artefacto. En cada archivo compara la ruta con la raíz del proyecto y pega el contenido completo.

#### Archivo `settings.gradle.kts`

**Qué hace:** Declara los repositorios de plugins y dependencias, Maven Local y el módulo Android Library.

````kotlin
pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS);
    repositories {
        google();
        mavenCentral();
        mavenLocal()
    }
}
rootProject.name = "shared-ui"
include(":designsystem")
````

#### Archivo `build.gradle.kts`

**Qué hace:** Declara los plugins que aplica el módulo, sin aplicarlos en la raíz.

````kotlin
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
````

#### Archivo `gradle/wrapper/gradle-wrapper.properties`

**Qué hace:** Selecciona Gradle 8.13, requerido por AGP 8.13.

````properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
````

#### Archivo `gradle.properties`

**Qué hace:** Activa AndroidX y opciones de compilación reproducible.

````properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
````

#### Archivo `gradle/libs.versions.toml`

**Qué hace:** Centraliza AGP, Kotlin y el Compose BOM, además de los alias de plugin y librerías.

````toml
[versions]
agp = "8.13.2"
kotlin = "2.3.21"
composeBom = "2026.06.00"

[plugins]
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

[libraries]
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
````

#### Archivo `buildSrc/build.gradle.kts`

**Qué hace:** Activa Kotlin DSL en el buildSrc usado por la configuración del módulo.

````kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}
````

#### Archivo `buildSrc/src/main/kotlin/LibraryCoordinates.kt`

**Qué hace:** Define group, artifact y versión local predeterminada del paquete Maven.

````kotlin
package com.redbull.lab.build

/** Maven coordinates for the shared UI library. */
object LibraryCoordinates {
    const val GROUP_ID = "com.redbull.lab"
    const val ARTIFACT_ID = "shared-ui"
    const val VERSION_NAME = "0.1.5"
}
````

#### Archivo `designsystem/build.gradle.kts`

**Qué hace:** Configura la Android Library Compose, el target Java, dependencias y publicación Maven/GitHub Packages.

````kotlin
import com.redbull.lab.build.LibraryCoordinates

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android { namespace = "com.redbull.lab.ui"; compileSdk = 36
    defaultConfig { minSdk = 24 }
    publishing { singleVariant("release") { withSourcesJar() } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }

}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
dependencies {
    val bom = platform(libs.compose.bom)
    api(bom); api(libs.compose.ui); api(libs.compose.material3); api(libs.compose.preview)
}

publishing { publications { create<MavenPublication>("release") {
    groupId = LibraryCoordinates.GROUP_ID
    artifactId = LibraryCoordinates.ARTIFACT_ID
    version = providers.gradleProperty("releaseVersion")
        .orElse(LibraryCoordinates.VERSION_NAME)
        .get()
} }
    repositories { maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "OWNER/REPOSITORY"}")
        credentials { username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull; password = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.key").orNull }
    } }
}

afterEvaluate { publishing.publications.named<MavenPublication>("release") { from(components["release"]) } }
````

#### Archivo `designsystem/src/main/AndroidManifest.xml`

**Qué hace:** Declara el manifest mínimo de la biblioteca.

````xml
<manifest />
````

**Comprueba la configuración antes de añadir los componentes:**

```bash
./gradlew projects
./gradlew :designsystem:tasks
```

## Paso 2. Crear los tokens del design system

Los tokens mantienen colores, tamaños y textos en un lugar común. Los componentes pueden reutilizarlos sin repetir valores literales. Crea las carpetas si no existen.

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/theme/Colors.kt`

**Qué hace:** Expone los colores primario y secundario de la biblioteca.

````kotlin
package com.redbull.lab.ui.theme

import androidx.compose.ui.graphics.Color

/** Shared color tokens for the Red Bull design system. */
object Colors {
    val Primary = Color(0xFFCC0000)
    val Secondary = Color(0xFF202020)
}
````

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/theme/Dimensions.kt`

**Qué hace:** Centraliza espaciados que usan tarjetas y estados.

````kotlin
package com.redbull.lab.ui.theme

import androidx.compose.ui.unit.dp

/** Shared spacing and size tokens for the Red Bull design system. */
object Dimensions {
    val Dim6dp = 6.dp
    val Dim8dp = 8.dp
    val Dim16dp = 16.dp
    val Dim24dp = 24.dp
}
````

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/theme/Strings.kt`

**Qué hace:** Centraliza etiquetas predeterminadas de búsqueda, carga y reintento.

````kotlin
package com.redbull.lab.ui.theme

/** Shared default text labels for the design system components. */
object Strings {
    const val Search = "Search"
    const val Loading = "Loading…"
    const val Retry = "Retry"
}
````

## Paso 3. Crear tema y componentes reutilizables

El tema toma sus colores de `Colors`. `ProductCard` usa dimensiones compartidas. Los estados y el campo de búsqueda reciben callbacks y parámetros, así cada app mantiene el comportamiento mientras reutiliza la presentación.

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/RedBullTheme.kt`

**Qué hace:** Aplica el esquema compartido de Material 3 usando los tokens de color.

````kotlin
package com.redbull.lab.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.redbull.lab.ui.theme.Colors

/** Applies the shared platform color scheme to a consumer screen. */
@Composable
fun RedBullTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Colors.Primary, secondary = Colors.Secondary),
        content = content
    )
}
````

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/ProductCard.kt`

**Qué hace:** Muestra título y subtítulo dentro de una tarjeta Compose compartida.

````kotlin
package com.redbull.lab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.redbull.lab.ui.theme.Dimensions

/** Displays a compact product summary using shared platform spacing and typography. */
@Composable
fun ProductCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = Dimensions.Dim6dp)) {
        Column(Modifier.padding(Dimensions.Dim16dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
````

#### Archivo `designsystem/src/main/java/com/redbull/lab/ui/PlatformComponents.kt`

**Qué hace:** Expone búsqueda, indicador de carga, estado vacío y error con acción de reintento.

````kotlin
package com.redbull.lab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.redbull.lab.ui.theme.Dimensions
import com.redbull.lab.ui.theme.Strings

/** A labeled search input shared by consumer applications. */
@Composable
fun PlatformSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = Strings.Search,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
    )
}

/** Shared loading presentation for platform-backed screens. */
@Composable
fun PlatformLoadingIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Dimensions.Dim24dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Dim8dp),
    ) {
        CircularProgressIndicator()
        Text(Strings.Loading, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Shared empty state with optional supporting text. */
@Composable
fun PlatformEmptyState(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(Dimensions.Dim16dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Shared recoverable error state. The consumer supplies its retry action. */
@Composable
fun PlatformErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(Dimensions.Dim16dp)) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = Dimensions.Dim8dp)) {
            Text(Strings.Retry)
        }
    }
}
````

**Compila el AAR debug y release y ejecuta las tareas de prueba disponibles:**

```bash
./gradlew test assembleDebug
```

Este POC no contiene pruebas unitarias ni pruebas instrumentadas, así que Gradle informa `NO-SOURCE` para esas tareas. La compilación sí valida los componentes Compose.

## Paso 4. Publicar localmente y consumir la biblioteca

Publica el artefacto localmente para que las apps de práctica lo resuelvan desde `mavenLocal()`. La versión local predeterminada se lee de `LibraryCoordinates.VERSION_NAME`; se puede sobreescribir con la propiedad `releaseVersion`.

```bash
./gradlew publishToMavenLocal
find ~/.m2/repository/com/redbull/lab/shared-ui -maxdepth 2 -type f
```

La coordenada actual es `com.redbull.lab:shared-ui:0.1.5`. Las apps consumidoras deben solicitar una versión publicada y tener acceso de lectura al repositorio o paquete correspondiente.

## Paso 5. Configurar CI y publicación de GitHub Packages

El CI valida push y pull request. El workflow de publicación corre al integrar a `main`: lee `VERSION_NAME`, valida SemVer y comprueba que el tag no exista; luego compila, publica el paquete y crea el tag y GitHub Release. Antes del próximo release incrementa `VERSION_NAME` en `LibraryCoordinates.kt` y mergea el cambio en `main`. El token de GitHub Actions se usa desde `GITHUB_TOKEN`; no agregues credenciales al repositorio.

#### Archivo `.github/workflows/android-ci.yml`

**Qué hace:** Instala JDK 17 y Android SDK 36 y ejecuta la validación automática para pushes y pull requests.

````yaml
name: Android CI
on: [push, pull_request]
permissions:
  contents: read
jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: android-actions/setup-android@v4
      - name: Install Android API 36
        run: sdkmanager "platforms;android-36" "build-tools;36.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test assembleDebug
````

#### Archivo `.github/workflows/publish.yml`

**Qué hace:** Verifica, publica la versión del archivo de coordenadas y crea un GitHub Release al integrar en main.

````yaml
name: Publish Android library
on:
  push:
    branches: [main]
permissions:
  contents: read
jobs:
  prepare:
    runs-on: ubuntu-latest
    outputs:
      version: ${{ steps.version.outputs.version }}
      release_needed: ${{ steps.version.outputs.release_needed }}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - name: Read version and check existing release
        id: version
        run: |
          version=$(sed -nE 's/.*VERSION_NAME = "([^"]+)".*/\1/p' buildSrc/src/main/kotlin/LibraryCoordinates.kt)
          if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo "Invalid release version in LibraryCoordinates.kt: $version" >&2
            exit 1
          fi
          echo "version=$version" >> "$GITHUB_OUTPUT"
          if git ls-remote --exit-code --tags origin "refs/tags/v$version" >/dev/null 2>&1; then
            echo "Release v$version already exists; skipping duplicate publication."
            echo "release_needed=false" >> "$GITHUB_OUTPUT"
          else
            echo "release_needed=true" >> "$GITHUB_OUTPUT"
          fi

  verify:
    needs: prepare
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: android-actions/setup-android@v4
      - name: Install Android API 36
        run: sdkmanager "platforms;android-36" "build-tools;36.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test assembleDebug

  publish:
    needs: [prepare, verify]
    if: needs.prepare.outputs.release_needed == 'true'
    runs-on: ubuntu-latest
    permissions:
      contents: write
      packages: write
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: android-actions/setup-android@v4
      - name: Install Android API 36
        run: sdkmanager "platforms;android-36" "build-tools;36.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - name: Publish tagged version to GitHub Packages
        run: ./gradlew publishAllPublicationsToGitHubPackagesRepository -PreleaseVersion="${{ needs.prepare.outputs.version }}"
        env:
          GITHUB_ACTOR: ${{ github.actor }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: v${{ needs.prepare.outputs.version }}
          target_commitish: ${{ github.sha }}
          generate_release_notes: true
````

#### Archivo `.gitignore`

**Qué hace:** Excluye caches, builds, configuración local y metadatos del IDE.

````text
**/.gradle/
**/build/
local.properties
*.iml
.idea/
.DS_Store
````

#### Archivo `README.md`

**Qué hace:** Resume versiones, comandos, publicación y forma de consumir el artefacto.

````markdown
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
````

## Compilar, publicar y comprobar un release

Desde la raíz de `01-shared-ui-library`, el flujo local completo es:

```bash
./gradlew clean
./gradlew test assembleDebug
./gradlew publishToMavenLocal
```

Para GitHub Packages, sube y mergea cambios a `main` después de aumentar la versión en `buildSrc/src/main/kotlin/LibraryCoordinates.kt`. El workflow publica la versión declarada y crea automáticamente `vX.Y.Z` si aún no existe.

## Por qué estas versiones

El código visual y las firmas de los componentes no se cambiaron; la actualización se limitó a versiones de build/dependencias y a los ajustes necesarios para que esa combinación compile. Se fijaron AGP 8.13.2, Gradle 8.13 y Kotlin 2.3.21; el Compose BOM es 2026.06.00, última línea estable anterior al requisito de API 37. Compose 1.12 requiere compileSdk 37 y AGP 9, por lo que adoptarlo aquí exigiría una migración de build mayor.

Fuentes oficiales consultadas el 2026-11-01: [Compose setup y BOM](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler), [requisito de Compose 1.12/API 37](https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html), [compatibilidad AGP/Gradle](https://developer.android.com/build/releases/about-agp), [historial de Kotlin](https://kotlinlang.org/docs/releases.html).
