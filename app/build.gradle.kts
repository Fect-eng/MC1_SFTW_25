plugins {
    alias(libs.plugins.android.application)
    // Add the dependency for the Google services Gradle plugin

    id("com.google.gms.google-services") version "4.4.3" apply false  // ✅ versión aquí
}

android {
    namespace = "com.devlabting.tucancha"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.devlabting.tucancha"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.mapbox.maps:android:10.15.1")

   // implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:6.11.0")

    implementation("com.facebook.fresco:fresco:3.6.0")

    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // =========================================================================================

    // Mapbox Maps SDK
  //  implementation("com.mapbox.maps:android:11.5.0")

// Mapbox Services (Directions, Geocoding, etc.)
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:6.11.0")

// Retrofit (para que compile Directions y callbacks)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Material Components (para FAB, Toolbar, etc.)
    implementation("com.google.android.material:material:1.11.0")

// RecyclerView y CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")


    // =========================================================================================

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}