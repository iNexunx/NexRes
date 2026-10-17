// Kotlin support is built into AGP 9.1+; the kotlin-android plugin is no longer needed.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
