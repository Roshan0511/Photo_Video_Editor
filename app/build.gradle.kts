plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.roshan.image_videoeditor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.roshan.image_videoeditor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        renderscriptTargetApi = 34
        renderscriptSupportModeEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildToolsVersion = "29.0.2"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        exclude("META-INF/rxjava.properties")
    }

    buildFeatures {
        viewBinding {
            enable = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.circleindicator)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.android.video.trimmer)

    implementation("com.burhanrashid52:photoeditor:3.0.2")
    implementation("com.github.rtugeek:colorseekbar:2.1.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
}