import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.alexeymirniy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {url = uri("https://jitpack.io")}
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }
}

dependencies {
    // Telegram bot api
    implementation ("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.6")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.6.10")
    testImplementation(kotlin("test"))
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}