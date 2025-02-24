plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.visionmate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kbyai.facerecognition"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    lint {
        abortOnError = false // Prevents build failure due to lint errors
        disable += listOf("UnusedResources", "HardcodedText") // Disables specific checks
        checkReleaseBuilds = false // Skips lint checks in release builds
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
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    androidResources {
        noCompress += "tflite"
    }

    packagingOptions{
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }

}


val ASSET_DIR = "${project.projectDir}/src/main/assets"

dependencies {
    // ... your existing dependencies ...

    // Assuming 'download_models.gradle' defines dependencies
    //apply(from = "download_models2.gradle")
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.cloud.speech)
    //implementation(libs.vosk)
    //implementation(libs.jna)
    implementation(libs.vosk.android)
    implementation(libs.androidx.preference.ktx)
    //implementation(libs.litert.support.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val cameraxVersion = "1.4.0-alpha03"
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation ("io.kommunicate.sdk:kommunicateui:2.10.0")
    implementation ("ai.picovoice:porcupine-android:3.0.0")
    implementation("com.google.cloud:google-cloud-speech:2.15.0")

    // app level build.gradle.kts
    //implementation("com.google.cloud:google-cloud-speech:1.29.1")
    implementation( "com.google.auth:google-auth-library-oauth2-http:0.26.0")
    implementation("io.grpc:grpc-okhttp:1.38.1")
    implementation("io.grpc:grpc-stub:1.39.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("com.alphacephei:vosk-android:0.3.47@aar")
    implementation(project(":models"))
    //ML Kit (To detect faces)
    implementation ("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.0.1")
//    implementation("com.google.cloud:google-cloud-speech:2.15.0")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Sentence Embeddings
    // https://github.com/shubham0204/Sentence-Embeddings-Android
    implementation("com.github.shubham0204:Sentence-Embeddings-Android:0.0.3")


    // ObjectBox - vector database
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:4.0.0")
    releaseImplementation("io.objectbox:objectbox-android:4.0.3")

    // Gemini SDK - LLM
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation(project(":libfotoapparat"))
    implementation(project(":libfacesdk"))
}

apply(plugin = "io.objectbox")