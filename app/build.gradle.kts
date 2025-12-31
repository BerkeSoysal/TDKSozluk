plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.berke.sozluk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.berke.sozluk"
        minSdk = 24
        targetSdk = 35
        versionCode = 38
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.volley)
    implementation(libs.sqlite)
    implementation(libs.lifecycle)
    implementation(libs.rxjava) // Latest RxJava version
    implementation(libs.rxandroid) // RxJava bindings for Android
    implementation(libs.rxbinding) // RxBinding for views
    implementation(libs.kotlinx.coroutines.android)

}