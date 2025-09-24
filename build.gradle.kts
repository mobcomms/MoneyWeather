plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.appdistribution") version "4.0.0" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://artifacts.applovin.com/android")
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.6.1")
        classpath("so.daro:daro-plugin:1.0.11")
        classpath("com.applovin.quality:AppLovinQualityServiceGradlePlugin:5.5.2")
    }
}
