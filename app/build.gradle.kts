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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Sourced from the gitignored local.properties so real credentials are never committed.
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

    buildTypes {
        release {
            optimization {
                enable = false
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