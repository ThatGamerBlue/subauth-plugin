buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("java")
    id("io.freefair.lombok") version "8.14"
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

group = "com.thatgamerblue.subauth"
version = "1.3"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenLocal()
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1") {
        isTransitive = false
    }

    compileOnly("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(7))
}

tasks.shadowJar {
    relocate("org.json", "com.thatgamerblue.json")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}