import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

// properties 객체를 생성하고 로드
val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties") // 속성 파일 경로
properties.load(propertiesFile.inputStream())

android {
    namespace = "com.example.artimo_smart_frame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.artimo_smart_frame"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Base_URL을 BuildConfig에 추가
        val baseUrl = properties["BASE_URL"]?.toString() ?: "https://default-url.com/"
        buildConfigField("String", "BASE_URL", baseUrl)
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
    //레이아웃
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.fragment:fragment:1.6.0")
    implementation(libs.androidx.core.ktx)
    //android tv용 leanback library
    implementation("androidx.leanback:leanback:1.0.0")
    //defaultartfragment의 명화용
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    //server 연결
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
}