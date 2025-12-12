plugins {
    id("com.android.application") version "8.3.0"
    id("org.jetbrains.kotlin.android") version "1.9.20"
    id("org.jetbrains.kotlin.kapt") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

android {
    namespace = "com.iamrakeshpanchal.nimusms"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.iamrakeshpanchal.nimusms"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        manifestPlaceholders["smsAppDefault"] = "true"
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // UI Components
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
    
    // Room Database
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Google Drive API
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.1")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230212-2.0.0")
    
    // SMS Permissions
    implementation("androidx.core:core-role:1.0.0")
}
