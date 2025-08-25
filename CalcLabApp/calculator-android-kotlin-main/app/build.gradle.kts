plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.agenciacristal.calculadora"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.agenciacristal.calculadora"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["accessibilityService"] = "enabled"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.core:core-ktx:1.12.0") // Downgrade to last stable version
    implementation("androidx.appcompat:appcompat:1.6.1") // Use stable version
    implementation("com.google.android.material:material:1.11.0") // Use stable version
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Use stable version
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("org.json:json:20210307")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Use stable version
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Use stable version
}
