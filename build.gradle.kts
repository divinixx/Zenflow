// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file where you can add configuration options common to all sub-projects/modules.
// See gradle/libs.versions.toml for shared library and plugin versions
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.hilt) apply false
}
