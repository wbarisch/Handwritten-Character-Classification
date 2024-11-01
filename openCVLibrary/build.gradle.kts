plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "org.opencv"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        ndkVersion = "28.0.12433566"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "LIBRARY_PACKAGE_NAME", "\"org.opencv\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }




}

dependencies {

}