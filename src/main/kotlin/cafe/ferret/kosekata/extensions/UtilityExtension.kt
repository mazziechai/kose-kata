/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.attachment
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.utils.download
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import io.ktor.client.request.forms.*
import io.ktor.util.cio.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.bson.Document
import org.koin.core.component.inject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.seconds

class UtilityExtension : Extension() {
    override val name = "utility"

    override val bundle = BUNDLE

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        publicSlashCommand {
            name = "export"
            description = "Exports all notes to a file"

            check {
                anyGuild()
                hasPermission(Permission.ManageGuild)
            }

            action {
                val notes = noteCollection.getByGuild(guild!!.id)
                val json = Json.encodeToString(notes)

                val timeFormat = SimpleDateFormat("yyyy-MM-dd")

                respond {
                    addFile(
                        "kose-${guild!!.id}-{${timeFormat.format(Date())}.json",
                        ChannelProvider { json.byteInputStream().toByteReadChannel() })
                }
            }
        }

        ephemeralSlashCommand {
            name = "userexport"
            description = "Export your notes"

            ephemeralSubCommand {
                name = "server"
                description = "Export your notes from this server"

                check {
                    anyGuild()
                }

                action {
                    val notes = noteCollection.getByGuildAndUser(guild!!.id, user.id)
                    val json = Json.encodeToString(notes)

                    val timeFormat = SimpleDateFormat("yyyy-MM-dd")

                    respond {
                        addFile(
                            "kose-${guild!!.id}-{${timeFormat.format(Date())}.json",
                            ChannelProvider { json.byteInputStream().toByteReadChannel() })
                    }
                }
            }

            ephemeralSubCommand {
                name = "all"
                description = "Export all of your notes"

                action {
                    val notes = noteCollection.getByUser(user.id)
                    val json = Json.encodeToString(notes)

                    val timeFormat = SimpleDateFormat("yyyy-MM-dd")

                    respond {
                        addFile(
                            "kose-${user.id}-{${timeFormat.format(Date())}.json",
                            ChannelProvider { json.byteInputStream().toByteReadChannel() })
                    }
                }
            }
        }

        publicSlashCommand {
            name = "import"
            description = "Imports all notes from a file"

            check {
                anyGuild()
                hasPermission(Permission.ManageGuild)
            }

            ephemeralSubCommand(::ImportArgs) {
                name = "kose"
                description = "Import notes from a kose kata notes file"

                action {
                    val notes: Array<Note>
                    try {
                        notes = Json.decodeFromString(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: IllegalArgumentException) {
                        respond {
                            content = translate("error.invalidjson", arrayOf("```\n$t\n```"))
                        }

                        return@action
                    }

                    for (note in notes) {
                        try {
                            val previousNote = noteCollection.get(note._id)
                            if (previousNote == null || previousNote.guild != guild!!.id) {
                                noteCollection.new(
                                    guild!!.id,
                                    note.author,
                                    note.name,
                                    note.aliases,
                                    note.content,
                                    originalAuthor = note.originalAuthor,
                                    timeCreated = note.timeCreated
                                )
                            } else {
                                val updatedNote = Note(
                                    note._id,
                                    note.author,
                                    guild!!.id,
                                    note.name,
                                    note.aliases,
                                    note.content,
                                    originalAuthor = note.originalAuthor,
                                    timeCreated = note.timeCreated
                                )

                                noteCollection.set(updatedNote)
                            }
                        } catch (t: Throwable) {
                            respond {
                                content = translate("error.partialimport", arrayOf("```\n$t\n```"))
                            }
                            throw t

                            return@action
                        }
                    }

                    respond {
                        content = translate("extensions.utility.import.success", arrayOf(notes.count()))
                    }
                }
            }

            ephemeralSubCommand(::ImportArgs) {
                name = "qbot"
                description = "Import notes from a qbot notes file"

                action {
                    val rawCollection = noteCollection.rawCollectionAccess()
                    val qbotJson: JsonElement

                    try {
                        qbotJson = Json.parseToJsonElement(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: SerializationException) {
                        respond {
                            content = translate("error.invalidjson", arrayOf("```\n$t\n```"))
                        }

                        return@action
                    }

                    var notesSkipped = 0

                    for (element in qbotJson.jsonArray) {
                        val jsonObject = element.jsonObject
                        try {
                            if (rawCollection.find(
                                    and(
                                        eq(
                                            Note::author.name,
                                            Snowflake(jsonObject["user_id"]!!.jsonPrimitive.long)
                                        ),
                                        eq(Note::guild.name, guild!!.id),
                                        eq(Note::name.name, jsonObject["name"]!!.jsonPrimitive.content),
                                        eq(Note::content.name, jsonObject["text"]!!.jsonPrimitive.content),
                                        Document.parse("{ timeCreated: '${jsonObject["created_at"]!!.jsonPrimitive.content}' }")
                                    )
                                ).firstOrNull() == null
                            ) {
                                noteCollection.new(
                                    Snowflake(jsonObject["user_id"]!!.jsonPrimitive.long),
                                    guild!!.id,
                                    jsonObject["name"]!!.jsonPrimitive.content,
                                    mutableListOf(jsonObject["name"]!!.jsonPrimitive.content),
                                    jsonObject["text"]!!.jsonPrimitive.content,
                                    timeCreated = Instant.parse(jsonObject["created_at"]!!.jsonPrimitive.content)
                                )
                            } else {
                                notesSkipped++
                            }
                        } catch (t: Throwable) {
                            respond {
                                content = translate("error.partialimport", arrayOf("```\n$t\n```"))
                            }

                            return@action
                        }
                    }

                    respond {
                        content = "Successfully imported ${qbotJson.jsonArray.count() - notesSkipped} notes!"
                        if (notesSkipped > 0) {
                            content += "\n(Skipped $notesSkipped notes due to conflicts)"
                        }
                    }
                }
            }
        }

        publicSlashCommand {
            name = "clear"
            description = "Deletes ALL notes. This is IRREVERSIBLE!"

            check {
                anyGuild()
                hasPermission(Permission.ManageGuild)
            }

            action {
                respond {
                    content = translate("extensions.utility.clear.confirmation")

                    components(15.seconds) {
                        ephemeralButton {
                            label = translate("button.deleteall.label")
                            style = ButtonStyle.Danger

                            action {
                                noteCollection.deleteAllGuild(guild!!.id)

                                edit {
                                    content = translate("extensions.utility.clear.success", BUNDLE)

                                    components = mutableListOf()
                                }
                            }
                        }

                        ephemeralButton {
                            label = "Cancel"
                            style = ButtonStyle.Secondary

                            action {
                                edit {
                                    content = translate("extensions.management.delete.cancel", BUNDLE)

                                    components = mutableListOf()
                                }

                            }
                        }

                        onTimeout {
                            edit {
                                components = mutableListOf()
                            }
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "help"
            description = "Sends the help page"

            action {
                respond {
                    content = translate("extensions.utility.help.message")
                }
            }
        }
    }

    inner class ImportArgs : Arguments() {
        val file by attachment {
            name = "file"
            description = "The notes you want to import"

            validate {
                failIf(translate("error.filetoobig")) {
                    value.size > 1048576 // 1 MiB
                }
            }
        }
    }
}
