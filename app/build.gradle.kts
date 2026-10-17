import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nexun.nexres"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nexun.nexres"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    // Release is signed with the public AOSP testkey committed under keystore/.
    // CI may override with its own keystore through environment variables.
    val envKeystore = System.getenv("KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
    val envStorePassword = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
    val envKeyAlias = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() }
    val envKeyPassword = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(envKeystore ?: "keystore/testkey.p12")
            storeType = "PKCS12"
            storePassword = envStorePassword ?: "android"
            keyAlias = envKeyAlias ?: "platform"
            keyPassword = envKeyPassword ?: envStorePassword ?: "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    debugImplementation(libs.androidx.ui.tooling)
}
