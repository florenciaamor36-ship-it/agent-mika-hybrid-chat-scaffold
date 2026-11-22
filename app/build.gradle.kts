plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.laclave.hybridagent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.laclave.hybridagent"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}

kotlin {
    jvmToolchain(17)
}
