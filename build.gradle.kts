plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.versions.update)
    application
}

group = "nkiesel.org"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kaml)
    implementation(libs.jpx)
    implementation(libs.csv)
    implementation(libs.okhttp)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "MainKt"
}
