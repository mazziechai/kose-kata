/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.guild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import io.ktor.client.request.forms.*
import io.ktor.util.cio.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.time.Clock

class DebugExtension : Extension() {
    override val name = "debug"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "debug"
            description = "Debug commands for the developer"

            check { isDeveloper { event.interaction.user.id } }

            ephemeralSubCommand(::DebugExportArgs) {
                name = "export"
                description = "Exports a guild's notes"

                action {
                    val notes = noteCollection.getByGuild(arguments.guild.id)

                    val json = Json.encodeToString(notes)

                    val timeFormat = SimpleDateFormat("yyyy-MM-dd")
                    respond {
                        addFile(
                            "kose-${guild!!.id}-{${timeFormat.format(Clock.systemUTC())}.json",
                            ChannelProvider { json.byteInputStream().toByteReadChannel() })
                    }
                }
            }
        }
    }

    inner class DebugExportArgs : Arguments() {
        val guild by guild {
            name = "guild"
            description = "The guild to export the notes of"
        }
    }
}

suspend fun <T : Event> CheckContext<T>.isDeveloper(arg: suspend () -> Snowflake) {
    if (!passed) {
        return
    }

    val id = arg()

    if (id != Snowflake(env("DEVELOPER").toLong())) {
        fail()
    } else {
        pass()
    }
}