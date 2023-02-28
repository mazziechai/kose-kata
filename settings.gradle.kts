/*
 * Copyright (c) 2023 mazziechai
 */

pluginManagement {
    plugins {
        // Update this in libs.version.toml when you change it here
        kotlin("jvm") version "1.8.10"
        kotlin("plugin.serialization") version "1.8.10"

        id("com.github.johnrengelman.shadow") version "5.2.0"
    }
}

rootProject.name = "kosekata"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
