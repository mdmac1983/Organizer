plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.mdmac.organizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mdmac.organizer"
        minSdk = 26        // Android 8.0
        targetSdk = 34
        versionCode = 2
        versionName = "2.00"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Room (database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Encrypted storage for Passwords tab
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Biometric (kept in for now in case you want it later, unused by default)
    implementation("androidx.biometric:biometric:1.1.0")
}
