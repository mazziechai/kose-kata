/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import cafe.ferret.kosekata.i18n.Translations
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.attachment
import dev.kordex.core.components.components
import dev.kordex.core.components.ephemeralButton
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.utils.download
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

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        publicSlashCommand {
            name = Translations.Extensions.Utility.Export.name
            description = Translations.Extensions.Utility.Export.description

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
            name = Translations.Extensions.Utility.Userexport.name
            description = Translations.Extensions.Utility.Userexport.description

            ephemeralSubCommand {
                name = Translations.Extensions.Utility.Userexport.Server.name
                description = Translations.Extensions.Utility.Userexport.Server.description

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
                name = Translations.Extensions.Utility.Userexport.All.name
                description = Translations.Extensions.Utility.Userexport.All.description

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
            name = Translations.Extensions.Utility.Import.name
            description = Translations.Extensions.Utility.Import.description

            check {
                anyGuild()
                hasPermission(Permission.ManageGuild)
            }

            ephemeralSubCommand(::ImportArgs) {
                name = Translations.Extensions.Utility.Importkose.name
                description = Translations.Extensions.Utility.Importkose.description

                action {
                    val notes: Array<Note>
                    try {
                        notes = Json.decodeFromString(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: IllegalArgumentException) {
                        respond {
                            content = Translations.Error.invalidjson.translate("```\n$t\n```")
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
                                content = Translations.Error.partialimport.translate("```\n$t\n```")
                            }
                            throw t

                            return@action
                        }
                    }

                    respond {
                        content = Translations.Extensions.Utility.Import.success.translate(notes.count())
                    }
                }
            }

            ephemeralSubCommand(::ImportArgs) {
                name = Translations.Extensions.Utility.Importqbot.name
                description = Translations.Extensions.Utility.Importqbot.description

                action {
                    val rawCollection = noteCollection.rawCollectionAccess()
                    val qbotJson: JsonElement

                    try {
                        qbotJson = Json.parseToJsonElement(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: SerializationException) {
                        respond {
                            content = Translations.Error.invalidjson.translate("```\n$t\n```")
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
                                content = Translations.Error.partialimport.translate("```\n$t\n```")
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
            name = Translations.Extensions.Utility.Clear.name
            description = Translations.Extensions.Utility.Clear.description

            check {
                anyGuild()
                hasPermission(Permission.ManageGuild)
            }

            action {
                respond {
                    content = Translations.Extensions.Utility.Clear.confirmation.translate()

                    components(15.seconds) {
                        ephemeralButton {
                            label = Translations.Button.Deleteall.label
                            style = ButtonStyle.Danger

                            action {
                                noteCollection.deleteAllGuild(guild!!.id)

                                edit {
                                    content = Translations.Extensions.Utility.Clear.success.translate()

                                    components = mutableListOf()
                                }
                            }
                        }

                        ephemeralButton {
                            label = Translations.Button.Cancel.label
                            style = ButtonStyle.Secondary

                            action {
                                edit {
                                    content = Translations.Extensions.Management.Delete.cancel.translate()

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
            name = Translations.Extensions.Utility.Help.name
            description = Translations.Extensions.Utility.Help.description

            action {
                respond {
                    content = Translations.Extensions.Utility.Help.message.translate()
                }
            }
        }
    }

    inner class ImportArgs : Arguments() {
        val file by attachment {
            name = Translations.Arguments.Importfile.name
            description = Translations.Arguments.Importfile.description

            validate {
                failIf(Translations.Error.filetoobig) {
                    value.size > 1048576 // 1 MiB
                }
            }
        }
    }
}
