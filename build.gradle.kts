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
    implementation(libs.xmlutil.core)
    implementation(libs.xmlutil.jvm)
    implementation(libs.kaml)
    implementation(libs.jpx)
    implementation(libs.csv)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "MainKt"
}
