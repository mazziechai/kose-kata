/*
 * Copyright (c) 2023 mazziechai
 */

pluginManagement {
    plugins {
        // Update this in libs.version.toml when you change it here
        kotlin("jvm") version "2.0.21"
        kotlin("plugin.serialization") version "2.0.21"

        id("com.github.johnrengelman.shadow") version "8.1.1"

        id("dev.kordex.gradle.kordex") version "1.6.0"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven("https://snapshots-repo.kordex.dev")
        maven("https://releases-repo.kordex.dev")
    }
}

rootProject.name = "kose-kata"
