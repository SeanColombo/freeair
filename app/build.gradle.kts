import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// Only set on machines with the upload keystore configured in local.properties -- release builds
// stay buildable (just unsigned) without it, so other contributors aren't blocked.
val releaseKeystorePath = localProperties.getProperty("release.keystore.path")

android {
    namespace = "com.seancolombo.freeair"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.seancolombo.freeair"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = rootProject.file(releaseKeystorePath)
                storePassword = localProperties.getProperty("release.keystore.storePassword")
                keyAlias = localProperties.getProperty("release.keystore.alias")
                keyPassword = localProperties.getProperty("release.keystore.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Sourced from the gitignored local.properties, and deliberately kept out of the
            // release build type entirely (not just unused) -- real credentials must never end
            // up compiled into a shipped artifact. Only PurpleAirIntegrationTest reads these,
            // and integration tests always run against the debug variant's classpath.
            buildConfigField(
                "String",
                "PURPLEAIR_API_KEY",
                "\"${localProperties.getProperty("purpleair.apiKey", "")}\"",
            )
            buildConfigField(
                "String",
                "PURPLEAIR_SENSOR_ID",
                "\"${localProperties.getProperty("purpleair.sensorId", "")}\"",
            )
        }
        release {
            optimization {
                enable = false
            }
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.org.json)
    testImplementation(libs.androidx.glance.testing)
    testImplementation(libs.androidx.glance.appwidget.testing)
    testImplementation(libs.robolectric)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Integration tests (e.g. PurpleAirIntegrationTest) hit real external services and only need to
// run before a commit, not on every build -- see AGENTS.md. They live alongside unit tests but
// are excluded from testDebugUnitTest and run separately via `./gradlew integrationTest`.
tasks.withType<Test>().configureEach {
    if (name.contains("UnitTest")) {
        exclude("**/*IntegrationTest.class")
    }
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests (e.g. against the live PurpleAir API). Run before committing."
    val unitTestTask = tasks.named<Test>("testDebugUnitTest").get()
    testClassesDirs = unitTestTask.testClassesDirs
    classpath = unitTestTask.classpath
    include("**/*IntegrationTest.class")
}