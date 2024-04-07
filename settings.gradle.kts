/*
 * Copyright (c) 2023 mazziechai
 */

pluginManagement {
    plugins {
        // Update this in libs.version.toml when you change it here
        kotlin("jvm") version "1.9.23"
        kotlin("plugin.serialization") version "1.9.23"

        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

rootProject.name = "kose-kata"
