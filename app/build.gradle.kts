plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    kotlin("kapt")
    id("com.google.firebase.crashlytics")
}

android {
//    packagingOptions {
//        exclude("META-INF/DEPENDENCIES")
//    }
    namespace = "com.example.ruine"
    compileSdk = 34
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.ruine"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.firebase.bom.v3312))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.google.firebase.auth)

    // Also add the dependency for the Google Play services library and specify its version
    implementation(libs.play.services.auth.v2111)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.browser)
    implementation(libs.okhttp)

//    implementation (libs.google.api.client)
//    implementation ("com.google.api-client:google-api-client-gson:1.31.5")
//    implementation (libs.google.api.services.gmail.vv1rev20220404200)
//    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")import com.google.api.services.gmail.model.Message

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.glide)
    kapt(libs.compiler)
    implementation(libs.glide.transformations)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.fancytoast)
    implementation(libs.mail)
    implementation(libs.jxl)
    implementation("org.apache.poi:poi:5.2.3") // for .xls files
    implementation("org.apache.poi:poi-ooxml:5.2.3") // for .xlsx files
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

//    ksp("androidx.room:room-compiler:$room_version")
}

