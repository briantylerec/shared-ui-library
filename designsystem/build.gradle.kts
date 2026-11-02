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
