/*
 * Copyright (c) 2023 mazziechai
 */

import dev.kordex.gradle.docker.file.*
import dev.kordex.gradle.plugins.kordex.DataCollection

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.johnrengelman.shadow")

    id("dev.kordex.gradle.docker")
    id("dev.kordex.gradle.kordex") version "1.0.2"
}

group = "cafe.ferret"
version = "1.0"

repositories {
    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kx.ser)

    // Logging dependencies
    implementation(libs.groovy)
    implementation(libs.jansi)
    implementation(libs.logback)
    implementation(libs.logging)

    implementation(libs.mongodb)
    implementation(libs.bson)
}

kordEx {
    // https://kordex.dev/blog/2024-07-23/kordex-2#levels
    dataCollection(DataCollection.Standard)

    mainClass = "cafe.ferret.kosekata.AppKt"

    module("unsafe")
}

// Automatically generate a Dockerfile. Set `generateOnBuild` to `false` if you'd prefer to manually run the
// `createDockerfile` task instead of having it run whenever you build.
docker {
    // Create the Dockerfile in the root folder.
    file(rootProject.file("Dockerfile"))

    commands {
        // Each function (aside from comment/emptyLine) corresponds to a Dockerfile instruction.
        // See: https://docs.docker.com/reference/dockerfile/

        from("openjdk:21-jdk-slim")

        emptyLine()

        runShell("mkdir -p /bot/plugins")
        runShell("mkdir -p /bot/data")

        emptyLine()

        copy("build/libs/$name-*-all.jar", "/bot/bot.jar")

        emptyLine()

        workdir("/bot")

        emptyLine()

        entryPointExec(
            "java", "-Xms2G", "-Xmx2G",
            "-jar", "/bot/bot.jar"
        )
    }
}
