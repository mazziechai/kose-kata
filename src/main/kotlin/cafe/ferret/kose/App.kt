/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose

import cafe.ferret.kose.database.database
import cafe.ferret.kose.extensions.*
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER").toLong()
)

val ENVIRONMENT = env("ENVIRONMENT")

private val token = env("TOKEN")

suspend fun main() {
    val bot = ExtensibleBot(token) {
        database(true)

        applicationCommands {
            if (ENVIRONMENT == "dev") {
                defaultGuild(TEST_SERVER_ID)
            }
        }

        extensions {
            add(::CreationExtension)
            add(::ManagementExtension)
            add(::ViewingExtension)
            add(::UtilityExtension)
            add(::DebugExtension)
        }
    }

    bot.start()
}
