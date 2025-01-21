import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.github.gmazzo.buildconfig") version "5.5.1"
}

val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.reader())
}
val apiKey: String = (System.getenv("WEATHER_API_KEY") ?: properties.getProperty("weather.api.key"))

android {

    namespace = "br.com.aline.weather"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }

    buildConfig {
        buildConfigField("String", "WEATHER_API_KEY", "\"${apiKey}\"")
    }

    defaultConfig {
        applicationId = "br.com.aline.weather"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation("io.coil-kt.coil3:coil:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}