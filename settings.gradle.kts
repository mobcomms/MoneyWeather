pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")}
        maven { url = uri("https://android-sdk.is.com") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle/") }
        maven { url = uri("https://repo.pubmatic.com/artifactory/public-repos") }
        maven { url = uri("https://sdk.tapjoy.com/") }
        maven { url = uri("https://raw.githubusercontent.com/coupang-ads-sdk/android/main") }
        maven { url = uri("https://repository.tnkad.net:8443/repository/public/") }
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://dl.buzzvil.com/public/maven") }

        maven { url = uri("https://maven.ogury.co") }
        maven { url = uri("https://s3.amazonaws.com/smaato-sdk-releases/") }
        maven { url = uri("https://verve.jfrog.io/artifactory/verve-gradle-release") }
        maven { url = uri("https://artifacts.applovin.com/android") }
    }
}

rootProject.name = "MoneyWeather"
include(":app")
