plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tryme"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tryme"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    dependencies {
        dependencies {
            implementation(libs.activity.ktx)
            implementation(libs.appcompat)
            implementation(libs.constraintlayout)
            implementation(libs.material)

            // CameraX
            implementation("androidx.camera:camera-core:1.4.2")
            implementation("androidx.camera:camera-camera2:1.4.2")
            implementation("androidx.camera:camera-lifecycle:1.4.2")
            implementation("androidx.camera:camera-view:1.4.2")

            // Google ML Kit QR scanning
            implementation("com.google.mlkit:barcode-scanning:17.3.0")

            testImplementation(libs.junit)
            androidTestImplementation(libs.espresso.core)
            androidTestImplementation(libs.ext.junit)
        }
    }
}

