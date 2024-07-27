/*
 * Copyright (c) 2023 mazziechai
 */

pluginManagement {
    plugins {
        // Update this in libs.version.toml when you change it here
        kotlin("jvm") version "2.0.255-SNAPSHOT"
        kotlin("plugin.serialization") version "2.0.20-Beta1"

        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

rootProject.name = "kose-kata"
