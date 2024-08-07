/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import cafe.ferret.kosekata.database.database
import cafe.ferret.kosekata.extensions.*
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.allowedMentions

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER").toLong()
)

val ENVIRONMENT = env("ENVIRONMENT")

private val token = env("TOKEN")
private val sentry_dsn = env("SENTRY_DSN")

const val BUNDLE = "kose.strings"

suspend fun main() {
    val bot = ExtensibleBot(token) {
        database(true)

        applicationCommands {
            if (ENVIRONMENT == "dev") {
                defaultGuild(TEST_SERVER_ID)
            }
        }

        chatCommands {
            enabled = true
            prefix { "." }
        }

        errorResponse { message, error ->
            allowedMentions { }
            content = buildString {
                append("There was an **unexpected error** while handling this command. ")
                appendLine("Please let us know on Discord or GitHub about it!")
                appendLine("`$error`")
            }
        }

        extensions {
            sentry {
                enable = true

                if (ENVIRONMENT == "dev") {
                    debug = true
                }

                dsn = sentry_dsn
                environment = ENVIRONMENT
            }

            add(::CreationExtension)
            add(::ManagementExtension)
            add(::ViewingExtension)
            add(::UtilityExtension)
            add(::DebugExtension)
            add(::AliasExtension)
            add(::ChatCommandsExtension)
            add(::InfoExtension)
        }
    }

    bot.start()
}
