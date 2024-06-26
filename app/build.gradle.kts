plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    kotlin("kapt")
}

android {
//    packagingOptions {
//        exclude("META-INF/DEPENDENCIES")
//    }
    namespace = "com.example.ruine"
    compileSdk = 34
    buildFeatures{
        viewBinding=true
    }
    defaultConfig {
        applicationId = "com.example.ruine"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("androidx.credentials:credentials:1.3.0-alpha01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    implementation ("androidx.browser:browser:1.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")



//    implementation (libs.google.api.client)
//    implementation ("com.google.api-client:google-api-client-gson:1.31.5")
//    implementation (libs.google.api.services.gmail.vv1rev20220404200)
//    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")import com.google.api.services.gmail.model.Message




    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation (libs.glide)
    kapt (libs.compiler)
    implementation (libs.glide.transformations)
    implementation(libs.androidx.swiperefreshlayout)
    implementation (libs.fancytoast)
    implementation(libs.mail)
    // To use Kotlin Symbol Processing (KSP)
//    ksp("androidx.room:room-compiler:$room_version")


}