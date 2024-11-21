/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.guild
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.utils.env
import io.ktor.client.request.forms.*
import io.ktor.util.cio.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class DebugExtension : Extension() {
    override val name = "debug"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = Translations.Extensions.Debug.Debug.name
            description = Translations.Extensions.Debug.Debug.description

            check { isDeveloper { event.interaction.user.id } }

            ephemeralSubCommand(::DebugExportArgs) {
                name = Translations.Extensions.Debug.Export.name
                description = Translations.Extensions.Debug.Export.description

                action {
                    val notes = noteCollection.getByGuild(arguments.guild.id)

                    val json = Json.encodeToString(notes)

                    val timeFormat = SimpleDateFormat("yyyy-MM-dd")
                    respond {
                        addFile(
                            "kose-${guild!!.id}-{${timeFormat.format(Date())}.json",
                            ChannelProvider { json.byteInputStream().toByteReadChannel() })
                    }
                }
            }
        }
    }

    inner class DebugExportArgs : Arguments() {
        val guild by guild {
            name = Translations.Arguments.Guild.name
            description = Translations.Arguments.Guild.description
        }
    }
}

suspend fun <T : Event> CheckContext<T>.isDeveloper(arg: suspend () -> Snowflake) {
    if (!passed) {
        return
    }

    val id = arg()

    if (id != Snowflake(env("DEVELOPER").toLong())) {
        fail(Translations.Error.notdev)
    } else {
        pass()
    }
}