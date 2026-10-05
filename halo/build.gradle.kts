// The Android Gradle plugin must load alongside the Kotlin plugin, so it goes on the root
// classpath, but only where the Android SDK exists: other machines build :core alone.
buildscript {
    val hasAndroidSdk = System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        file("local.properties").let { it.exists() && it.readText().contains("sdk.dir") }
    if (hasAndroidSdk) {
        repositories {
            google()
            mavenCentral()
        }
        dependencies {
            classpath("com.android.tools.build:gradle:8.13.0")
        }
    }
}

plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("android") version "2.3.21" apply false
}
