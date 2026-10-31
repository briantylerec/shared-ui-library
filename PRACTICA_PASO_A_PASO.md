# Guía paso a paso: biblioteca Compose compartida

**Objetivo.** Construir y publicar una biblioteca Android que comparte tema, tokens y componentes Compose entre varias apps.

## Cómo usar esta guía

El proyecto de esta guía es `01-shared-ui-library`. Abre una terminal en la raíz de esa carpeta. En cada paso crea las carpetas y archivos indicados (o reemplaza su contenido), pega el bloque completo y guarda. La explicación está junto al archivo. Ejecuta el comando al final de cada paso antes de continuar. Los bloques reproducen los archivos del proyecto actual.

Requisitos: JDK 17, Android Studio y Android SDK Platform 35. Usa el Gradle Wrapper que ya está en este proyecto: `./gradlew` en macOS/Linux o `gradlew.bat` en Windows. Conserva `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar`; no los reemplaces con bloques de texto.

## Pasos

### 1. Preparar la build Android Library

Configura el build independiente. settings.gradle.kts resuelve plugins y dependencias; libs.versions.toml centraliza versiones; designsystem/build.gradle.kts declara la biblioteca Android, activa Compose y configura la publicación Maven. El manifest de la biblioteca puede estar vacío.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 1 — Crea o actualiza `settings.gradle.kts`

**En el proyecto de la biblioteca Compose compartida, en la ruta `settings.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara el nombre de esta build, los módulos y los repositorios desde donde Gradle descarga plugins y dependencias.

````kotlin
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral(); mavenLocal() } }
rootProject.name = "shared-ui"
include(":designsystem")
````

#### Subpaso 2 — Crea o actualiza `build.gradle.kts`

**En el proyecto de la biblioteca Compose compartida, en la ruta `build.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara plugins y configuración comunes del build independiente.

````kotlin
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
````

#### Archivos de coordenadas de la biblioteca

Gradle necesita las coordenadas Maven durante la publicación. Coloca el objeto en `buildSrc` para que los scripts Gradle puedan usarlo; no lo pongas en el código Android del módulo.

Crea `buildSrc/build.gradle.kts`:

````kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}
````

Crea `buildSrc/src/main/kotlin/LibraryCoordinates.kt`:

````kotlin
package com.redbull.lab.build

object LibraryCoordinates {
    const val GROUP_ID = "com.redbull.lab"
    const val ARTIFACT_ID = "shared-ui"
    const val VERSION_NAME = "0.1.0"
}
````

#### Subpaso 3 — Crea o actualiza `gradle.properties`

**En el proyecto de la biblioteca Compose compartida, en la ruta `gradle.properties`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Fija opciones de Gradle para que la compilación sea estable.

````properties
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
````

#### Subpaso 4 — Crea o actualiza `gradle/libs.versions.toml`

**En el proyecto de la biblioteca Compose compartida, en la ruta `gradle/libs.versions.toml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Centraliza versiones y aliases para dependencias y plugins en formato TOML.

````toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
composeBom = "2024.10.00"

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

#### Subpaso 5 — Crea o actualiza `designsystem/build.gradle.kts`

**En el proyecto de la biblioteca Compose compartida, en la ruta `designsystem/build.gradle.kts`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Configura el módulo Android de la biblioteca Compose, sus dependencias, publicación Maven y coordenadas.

````kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android { namespace = "com.redbull.lab.ui"; compileSdk = 35
    defaultConfig { minSdk = 24 }
    publishing { singleVariant("release") { withSourcesJar() } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
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

#### Subpaso 6 — Crea o actualiza `designsystem/src/main/AndroidManifest.xml`

**En el proyecto de la biblioteca Compose compartida, en la ruta `designsystem/src/main/AndroidManifest.xml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Declara metadatos Android del módulo; en bibliotecas no incluye una pantalla de app.

````xml
<manifest />
````

**Al terminar este paso:**

```bash
./gradlew tasks
```

### 2. Crear tema y componentes reutilizables

Los tokens viven en RedBullTheme.kt. Los componentes reciben datos, acciones y Modifier, sin conocer Retrofit ni reglas propias de Events/Explore. Así las apps comparten presentación, pero conservan su lógica.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 7 — Crea o actualiza `designsystem/src/main/java/com/redbull/lab/ui/RedBullTheme.kt`

**En el proyecto de la biblioteca Compose compartida, en la ruta `designsystem/src/main/java/com/redbull/lab/ui/RedBullTheme.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Expone tokens visuales y el tema Compose compartido.

````kotlin
package com.redbull.lab.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Applies the shared platform color scheme to a consumer screen. */
@Composable
fun RedBullTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFFCC0000), secondary = Color(0xFF202020)), content = content)
}
````

#### Subpaso 8 — Crea o actualiza `designsystem/src/main/java/com/redbull/lab/ui/ProductCard.kt`

**En el proyecto de la biblioteca Compose compartida, en la ruta `designsystem/src/main/java/com/redbull/lab/ui/ProductCard.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Define la tarjeta reutilizable de producto usada por las aplicaciones.

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
import androidx.compose.ui.unit.dp

/** Displays a compact product summary using shared platform spacing and typography. */
@Composable
fun ProductCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
````

#### Subpaso 9 — Crea o actualiza `designsystem/src/main/java/com/redbull/lab/ui/PlatformComponents.kt`

**En el proyecto de la biblioteca Compose compartida, en la ruta `designsystem/src/main/java/com/redbull/lab/ui/PlatformComponents.kt`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Define componentes Compose reutilizables para búsqueda, carga, error y vacío.

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
import androidx.compose.ui.unit.dp

/** A labeled search input shared by consumer applications. */
@Composable
fun PlatformSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Search",
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
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator()
        Text("Loading…", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Shared empty state with optional supporting text. */
@Composable
fun PlatformEmptyState(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
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
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
            Text("Retry")
        }
    }
}
````

**Al terminar este paso:**

```bash
./gradlew :designsystem:assembleDebug
```

### 3. Compilar, probar y publicar localmente

Compila el AAR release y corre pruebas unitarias (el POC no incluye tests de UI). Publicar en Maven Local permite que Events y Explore resuelvan com.redbull.lab:shared-ui:0.1.0 sin publicar todavía en internet. `groupId`, `artifactId` y la versión local predeterminada se configuran en `LibraryCoordinates`; en CI, la publicación usa la versión del tag `vX.Y.Z`.

**Al terminar este paso:**

```bash
./gradlew test assembleDebug publishToMavenLocal
```

Comprueba que Maven Local recibió el paquete:

```bash
find ~/.m2/repository/com/redbull/lab/shared-ui -maxdepth 2 -type f
```

### 4. Configurar CI y release independiente

El workflow CI compila en push/PR. Para publicar una versión nueva, integra el cambio en main y crea un tag SemVer vX.Y.Z. El workflow verifica y después publica el artefacto en GitHub Packages de este repo.

**Sigue cada archivo como un subpaso en el orden que aparece. Termina uno antes de continuar al siguiente.**

#### Subpaso 10 — Crea o actualiza `.github/workflows/android-ci.yml`

**En el proyecto de la biblioteca Compose compartida, en la ruta `.github/workflows/android-ci.yml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Centraliza versiones y aliases para dependencias y plugins en formato TOML.

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
      - uses: android-actions/setup-android@v3
      - name: Install Android API 35
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test assembleDebug
````

#### Subpaso 11 — Crea o actualiza `.github/workflows/publish.yml`

**En el proyecto de la biblioteca Compose compartida, en la ruta `.github/workflows/publish.yml`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Centraliza versiones y aliases para dependencias y plugins en formato TOML.

````yaml
name: Publish Android library
on:
  push:
    tags: ['v*']
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
      - uses: android-actions/setup-android@v3
      - name: Install Android API 35
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test assembleDebug

  publish:
    needs: verify
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: android-actions/setup-android@v3
      - name: Install Android API 35
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - name: Publish tagged version
        run: ./gradlew publishAllPublicationsToGitHubPackagesRepository -PreleaseVersion="${GITHUB_REF_NAME#v}"
        env:
          GITHUB_ACTOR: ${{ github.actor }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
````

#### Subpaso 12 — Crea o actualiza `.gitignore`

**En el proyecto de la biblioteca Compose compartida, en la ruta `.gitignore`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Evita versionar salidas de build, cachés y configuración local.

````text
**/.gradle/
**/build/
local.properties
*.iml
.idea/
.DS_Store
````

#### Subpaso 13 — Crea o actualiza `README.md`

**En el proyecto de la biblioteca Compose compartida, en la ruta `README.md`, crea el archivo si no existe y pega el siguiente código.**

**Explicación:** Describe el propósito, uso, coordenadas y publicación de este repositorio.

````text
# Shared Compose UI library

Design tokens, `RedBullTheme`, `ProductCard` and shared search/loading/empty/error components. This folder is the root of the UI library's own GitHub repository.

## Local build and publication

Requires JDK 17 and Android SDK 35. From this directory:

```bash
./gradlew test assembleDebug
./gradlew publishToMavenLocal
```

The Maven group, artifact, and local default version are configured in `buildSrc/src/main/kotlin/LibraryCoordinates.kt`. A new `vX.Y.Z` tag runs `.github/workflows/publish.yml`, which verifies the library and publishes that tag version to GitHub Packages. Do not republish an existing version.

The workflow publishes into this GitHub repository's Maven package registry. Its `GITHUB_TOKEN` only needs `packages: write`; no personal token is stored in the repo. Consumer repos need `Read` access to this package. See the parent lab's `TUTORIAL.md` for the full four-repo release flow.
````

**Al terminar este paso:**

```bash
git tag v0.1.1 && git push origin v0.1.1
```

## Publicar y conectar con los otros repositorios

Este es un repositorio GitHub independiente. Desde esta carpeta, sube el código a su repositorio. Después de integrar el cambio en `main`, publica una versión nueva con un tag que no se haya usado antes (ejemplo: `v0.1.1`):

```bash
git add .
git commit -m "Prepare library release"
git push origin main
git tag v0.1.1
git push origin v0.1.1
```

El workflow `.github/workflows/publish.yml` ejecuta la validación y publica el paquete de este repo en GitHub Packages. Incrementa la versión Maven antes de reutilizar el proceso. Para consumir un paquete privado, concede permiso de lectura al repo app.

## Comandos útiles desde este proyecto

```bash
./gradlew --version
./gradlew clean
```

Si estás en Windows sustituye `./gradlew` por `gradlew.bat`. Si Gradle no encuentra una biblioteca local, publícala primero desde su propio repo y confirma que la versión de la app coincide con la publicada.
