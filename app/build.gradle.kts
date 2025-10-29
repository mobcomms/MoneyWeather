import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("so.daro.m") // daro
}

val appName = "donsee"
val date = SimpleDateFormat("yyyyMMdd").format(Date())

extra["daroAppKey"] = project.findProperty("daroAppKey") ?: ""

android {
    namespace = "com.moneyweather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moneyweather"
        minSdk = 26
        targetSdk = 35
        versionCode = 76
        versionName = "1.0.43"

        setProperty("archivesBaseName", "${appName}_v${versionName}($versionCode)_$date")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
//        val daroAppKey: String by project
//        manifestPlaceholders["DARO_APP_KEY"] = daroAppKey
        manifestPlaceholders["kakaoAppKey"] = getLocalProperty("KAKAO_APP_KEY")
        manifestPlaceholders["kakaoScheme"] = getLocalProperty("KAKAO_SCHEME")
        manifestPlaceholders["tnkadAppId"] = getLocalProperty("TNKAD_APP_ID")
        manifestPlaceholders["adpopcornAppKey"] = getLocalProperty("ADPOPCORN_APP_KEY")
        manifestPlaceholders["adpopcornHashKey"] = getLocalProperty("ADPOPCORN_HASH_KEY")
        manifestPlaceholders["adpopcornRewardServerType"] = getLocalProperty("ADPOPCORN_REWARD_SERVER_TYPE")
        manifestPlaceholders["nasmediaAppKey"] = getLocalProperty("NASMEDIA_APP_KEY")

        buildConfigField("String", "KAKAO_APP_KEY", "\"${getLocalProperty("KAKAO_APP_KEY")}\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${getLocalProperty("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${getLocalProperty("NAVER_CLIENT_SECRET")}\"")
        buildConfigField("String", "NAVER_CLIENT_NAME", decodeUnicodeEscapes("\"${getLocalProperty("NAVER_CLIENT_NAME")}\""))
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${getLocalProperty("GOOGLE_CLIENT_ID")}\"")
        buildConfigField("String", "MOBON_CODE", "\"${getLocalProperty("MOBON_CODE")}\"")
        buildConfigField("String", "MOBON_CODE_50_50", "\"${getLocalProperty("MOBON_CODE_50_50")}\"")
        buildConfigField("String", "MOBON_BNTYPE", "\"${getLocalProperty("MOBON_BNTYPE")}\"")
        buildConfigField("String", "MOBWITH_UNIT_ID_320_50", "\"${getLocalProperty("MOBWITH_UNIT_ID_320_50")}\"")
        buildConfigField("String", "MOBWITH_ZONE_ID_320_50", "\"${getLocalProperty("MOBWITH_ZONE_ID_320_50")}\"")
        buildConfigField("String", "MOBWITH_ZONE_ID_320_50_COUPANG", "\"${getLocalProperty("MOBWITH_ZONE_ID_320_50_COUPANG")}\"")
        buildConfigField("String", "MOBWITH_ZONE_ID_300_250", "\"${getLocalProperty("MOBWITH_ZONE_ID_300_250")}\"")
        buildConfigField("String", "MOBWITH_ZONE_ID_320_100", "\"${getLocalProperty("MOBWITH_ZONE_ID_320_100")}\"")
        buildConfigField("String", "UNITY_ADS_GAME_ID", "\"${getLocalProperty("UNITY_ADS_GAME_ID")}\"")
        buildConfigField("String", "LEVEL_PLAY_APP_KEY", "\"${getLocalProperty("LEVEL_PLAY_APP_KEY")}\"")
        buildConfigField("String", "DT_EXCHANGE_APP_KEY", "\"${getLocalProperty("DT_EXCHANGE_APP_KEY")}\"")
        buildConfigField("String", "ADPIE_MEDIA_ID", "\"${getLocalProperty("ADPIE_MEDIA_ID")}\"")
        buildConfigField("String", "ADPIE_NATIVE_SLOT_ID", "\"${getLocalProperty("ADPIE_NATIVE_SLOT_ID")}\"")
        buildConfigField("String", "ADPIE_BANNER_SLOT_ID", "\"${getLocalProperty("ADPIE_BANNER_SLOT_ID")}\"")
        buildConfigField("String", "POMISSION_MEDIA_ID", "\"${getLocalProperty("POMISSION_MEDIA_ID")}\"")
        buildConfigField("String", "POMISSION_REFRESH_TOKEN", "\"${getLocalProperty("POMISSION_REFRESH_TOKEN")}\"")
        buildConfigField("String", "TUNE_APP_KEY", "\"${getLocalProperty("TUNE_APP_KEY")}\"")
        buildConfigField("String", "PINCRUX_PUB_KEY", "\"${getLocalProperty("PINCRUX_PUB_KEY")}\"")
        buildConfigField("String", "BUZZVIL_APP_ID", "\"${getLocalProperty("BUZZVIL_APP_ID")}\"")
        buildConfigField("String", "BUZZVIL_APP_ID_DEV", "\"${getLocalProperty("BUZZVIL_APP_ID_DEV")}\"")
    }

    signingConfigs {
        create("release") {
            keyAlias = getLocalProperty("KEY_ALIAS")
            keyPassword = getLocalProperty("KEY_PASSWORD")
            storeFile = file(getLocalProperty("STORE_FILE"))
            storePassword = getLocalProperty("STORE_PASSWORD")
        }
    }

    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val verName = "${appName}_v${versionName}(${versionCode})_$date"
            outputImpl.outputFileName = outputImpl.outputFileName.replace("app-", "$verName")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

//    buildscript {
//        ext.daroAppKey = "9e754d68-6e84-42dc-90f7-075b3b77df3d"
//    }

//    buildscript{
//        extra["daroAppKey"] = "9e754d68-6e84-42dc-90f7-075b3b77df3d"
//    }

    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        compose = false
        dataBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getLocalProperty(propertyKey: String): String {
    val properties = Properties()
    val localPropertiesFile = File(rootDir, "local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use {
            properties.load(it)
        }
    }
    return properties.getProperty(propertyKey)
        ?: error("Property '$propertyKey' not found in local.properties")
}

fun decodeUnicodeEscapes(input: String): String {
    return Regex("""\\u([0-9a-fA-F]{4})""").replace(input) {
        val hexCode = it.groupValues[1]
        hexCode.toInt(16).toChar().toString()
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-dynamic-links:22.1.0")
    implementation("com.google.firebase:firebase-config-ktx:22.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
    implementation("androidx.appcompat:appcompat:1.7.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // exoplayer
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    // livedata
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.7.0-alpha01")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // rxjava
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.trello.rxlifecycle2:rxlifecycle-android:2.1.0")
    implementation("com.trello.rxlifecycle2:rxlifecycle-components:2.0.1")
    implementation("com.uber.rxdogtag:rxdogtag:0.3.0")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // picasso
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.makeramen:roundedimageview:2.3.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.0")
    implementation("com.squareup.retrofit2:retrofit-mock:2.4.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-simplexml:2.1.0")

    //okhttp3
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.0.0-alpha.2")

    //glide
    implementation("com.github.bumptech.glide:glide:4.13.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    kapt("android.arch.lifecycle:compiler:1.1.1")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    kapt("com.android.databinding:compiler:3.1.4")

    //코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation("io.github.ParkSangGwon:tedpermission-normal:3.3.0")
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation("com.google.code.gson:gson:2.8.9")

    //멀티덱스
    implementation("androidx.multidex:multidex:2.0.1")

    kapt("com.android.databinding:compiler:3.1.4")
    //카카오 로그인
    implementation("com.kakao.sdk:v2-user:2.8.2")
    implementation("com.kakao.sdk:v2-talk:2.8.2")


    implementation("androidx.work:work-runtime:2.7.1")
    implementation("com.google.guava:guava:27.0.1-android")

    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.jakewharton.timber:timber:5.0.1")

    //    // 카카오
    implementation("com.kakao.sdk:v2-all:2.11.2")
    implementation("com.kakao.adfit:ads-base:3.15.2")

    //네이버 로그인
    implementation("com.navercorp.nid:oauth-jdk8:5.1.0")

    implementation("com.google.zxing:core:3.3.3")

    //위치
    implementation("com.google.android.gms:play-services-location:21.3.0")
    //차트
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //달력
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")

    // 에이닉
    implementation(files("libs/en_banner_20251028.aar"))

    // unity
//    implementation("com.ironsource.sdk:mediationsdk:8.3.0")
//    implementation("com.ironsource:adqualitysdk:7.20.5")
//    implementation("com.google.android.gms:play-services-appset:16.0.2")
    // full cover sdk
    // ironsource add
    // Add Applovin Network
    implementation("com.ironsource.adapters:applovinadapter:4.3.48")
    implementation("com.applovin:applovin-sdk:13.0.1")
    // Add APS Network
//    implementation("com.amazon.android:aps-sdk:9.10.0+")
    implementation("com.amazon.android:aps-sdk:11.0.0")
    implementation("com.ironsource.adapters:apsadapter:4.3.14")
    implementation("com.iabtcf:iabtcf-decoder:2.0.10")
    // Add Fyber Network (Adapter only)
    implementation("com.ironsource.adapters:fyberadapter:4.3.35")
    implementation("com.fyber:marketplace-sdk:8.3.8")
    // Add UnityAds Network
//    implementation("com.ironsource.adapters:unityadsadapter:4.3.46")
//    implementation("com.unity3d.ads:unity-ads:4.12.5")

    // for mobwith
    implementation("io.github.mobon:mobwithSDK:1.0.59")
//    implementation("com.pangle.global:pag-sdk:7.1.0.4")

    // adop
//    implementation("ad.helper.openbidding:admob-obh:3.18.0")
//    implementation("com.adop.sdk:bidmad-androidx:3.18.0")
//    implementation("com.adop.sdk.adapter:adfit:3.12.15.2")
//    implementation("com.adop.sdk.adapter:admob:22.0.0.6")
//    implementation("com.adop.sdk.adapter:adpie:1.13.6.0")
//    implementation("com.adop.sdk.adapter:adpopcorn:3.6.3.0")
//    implementation("com.adop.sdk.adapter:applovin:11.9.0.4")
//    implementation("com.adop.sdk.adapter:criteo:6.0.0.2")
//    implementation("com.adop.sdk.adapter:fyber:8.2.3.4")
//    implementation("com.adop.sdk.adapter:pubmatic:2.7.1.4")
//    implementation("com.adop.sdk.adapter:unityads:4.6.1.5")
//    implementation("com.adop.sdk.adapter:vungle:6.12.1.3")

    // coupang
    implementation("com.coupang:ads:1.3.0")

    // TNK Factory
    implementation("com.tnkfactory:rwd:8.07.15")

    // adPopcorn
    implementation("com.igaworks.offerwall:AdPopcornOfferwall:9.1.7")

    // Nas Media
    implementation(files("libs/NASWall_20230607.jar"))

    // Pincrux
    implementation(files("libs/pincrux_offerwall.aar"))

    // adpie
    implementation("com.github.adxcorp:adpie-android-sdk:1.14.1")

    // tune720
    implementation(files("libs/DataManagerSDK-release.aar"))

    // buzzvil
    api(platform("com.buzzvil:buzzvil-bom:6.2.+"))
    implementation("com.buzzvil:buzzvil-sdk")

    // daro
    implementation("so.daro:daro-core:1.3.0")
    implementation("so.daro:daro-m:1.3.0")

}

/**
 * 👇 SafeDK Task 비활성화 (빌드 시 충돌 방지)
 */
gradle.taskGraph.whenReady {
    allTasks
        .filter { task -> task.name.contains("safedk", ignoreCase = true) }
        .forEach { task -> task.enabled = false }
}