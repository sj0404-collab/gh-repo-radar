import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.sj0404.nimbusdeck"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sj0404.nimbusdeck"
        minSdk = 23
        targetSdk = 35
        versionCode = 10000
        versionName = "1.0.0"
    }

    val signingFile = rootProject.file(".local-signing/keystore.properties")
    if (signingFile.exists()) {
        val signingProperties = Properties().apply {
            signingFile.inputStream().use(::load)
        }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
                storePassword = signingProperties.getProperty("storePassword")
                keyAlias = signingProperties.getProperty("keyAlias")
                keyPassword = signingProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
