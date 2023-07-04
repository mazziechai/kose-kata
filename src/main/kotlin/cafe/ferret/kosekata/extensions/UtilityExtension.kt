/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.UserNotesArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.guildNotes
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
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.download
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.effectiveName
import io.ktor.client.request.forms.*
import io.ktor.util.cio.*
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
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
        ephemeralSlashCommand {
            name = "list"
            description = "Lists notes"

            ephemeralSubCommand(::UserNotesArgs) {
                name = "user"
                description = "Gets a user's notes"

                check { anyGuild() }

                action {
                    val member = arguments.user

                    val notes = noteCollection
                        .getByUser(member.id)
                        .filter { it.guild == guild!!.id }
                        .sortedBy { it.name }

                    if (notes.isEmpty()) {
                        respond {
                            content = translate("error.usernonotes")
                        }

                        return@action
                    }

                    editingPaginator {
                        timeoutSeconds = 60

                        notes.chunked(10).forEach { chunkedNotes ->
                            page {
                                author {
                                    name = "${member.effectiveName} (${member.username})"
                                    icon = member.avatar?.cdnUrl?.toUrl()
                                }

                                title = "Notes"

                                description = buildString {
                                    chunkedNotes.forEach { note ->
                                        append("*${note.name}* | #%06x | ".format(note._id))
                                        append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                                        appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                                    }
                                }

                                footer {
                                    text = "${notes.count()} notes"
                                }
                            }
                        }
                    }.send()
                }
            }

            ephemeralSubCommand {
                name = "server"
                description = "Gets this server's notes"

                check { anyGuild() }

                action {
                    val notes = noteCollection.getByGuild(guild!!.id).sortedBy { it.name }

                    if (notes.isEmpty()) {
                        respond {
                            content = translate("error.servernonotes")
                        }

                        return@action
                    }

                    guildNotes(this@ephemeralSlashCommand.kord, guild!!.asGuild(), notes)
                }
            }
        }

        ephemeralSlashCommand {
            name = "mynotes"
            description = "Gets your notes, regardless of server"

            action {
                val notes = noteCollection.getByUser(user.id)

                if (notes.isEmpty()) {
                    respond {
                        content = translate("extensions.utility.mynotes.error")
                    }

                    return@action
                }

                val user = user.asUser()
                val cachedGuilds = mutableListOf<Guild>()
                val unknownGuilds = mutableListOf<Snowflake>()

                editingPaginator {
                    timeoutSeconds = 60

                    notes.chunked(10).forEach { chunkedNotes ->
                        page {
                            author {
                                name = user.effectiveName
                                icon = user.avatar?.cdnUrl?.toUrl()
                            }

                            title = "Notes"

                            description = buildString {
                                chunkedNotes.forEach { note ->
                                    var guild = cachedGuilds.find { it.id == note.guild }

                                    if (guild == null) {
                                        if (note.guild !in unknownGuilds) {
                                            guild = this@ephemeralSlashCommand.kord.getGuildOrNull(note.guild)
                                        }
                                    }

                                    if (guild == null) {
                                        unknownGuilds.add(note.guild)
                                    } else {
                                        cachedGuilds.add(guild)
                                    }

                                    append("${guild?.name ?: "Unknown server"} → ")
                                    append("*${note.name}* | #%06x | ".format(note._id))
                                    append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                                    appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                                }
                            }

                            footer {
                                text = "${notes.count()} notes"
                            }
                        }
                    }
                }.send()
            }
        }

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
                    val koseJson: JsonElement

                    try {
                        koseJson = Json.parseToJsonElement(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: SerializationException) {
                        respond {
                            content = translate("error.invalidjson", arrayOf("```\n$t\n```"))
                        }

                        return@action
                    }

                    for (element in koseJson.jsonArray) {
                        val jsonObject = element.jsonObject

                        try {
                            noteCollection.new(
                                Snowflake(jsonObject["author"]!!.jsonPrimitive.long),
                                guild!!.id,
                                jsonObject["name"]!!.jsonPrimitive.content,
                                jsonObject["names"]?.jsonArray?.map { it.jsonPrimitive.content }?.toMutableList()
                                    ?: mutableListOf(jsonObject["name"]!!.jsonPrimitive.content),
                                jsonObject["content"]!!.jsonPrimitive.content,
                                originalAuthor = Snowflake(jsonObject["originalAuthor"]!!.jsonPrimitive.long),
                                timeCreated = jsonObject["timeCreated"]!!.jsonPrimitive.content.toInstant()
                            )
                        } catch (t: Throwable) {
                            respond {
                                content = translate("error.partialimport", arrayOf("```\n$t\n```"))
                            }

                            return@action
                        }
                    }

                    respond {
                        content = translate("extensions.utility.import.success", arrayOf(koseJson.jsonArray.count()))
                    }
                }
            }

            ephemeralSubCommand(::ImportArgs) {
                name = "qbot"
                description = "Import notes from a qbot notes file"

                action {
                    val qbotJson: JsonElement

                    try {
                        qbotJson = Json.parseToJsonElement(arguments.file.download().toString(Charset.forName("UTF-8")))
                    } catch (t: SerializationException) {
                        respond {
                            content = translate("error.invalidjson", arrayOf("```\n$t\n```"))
                        }

                        return@action
                    }

                    for (element in qbotJson.jsonArray) {
                        val jsonObject = element.jsonObject
                        try {
                            noteCollection.new(
                                Snowflake(jsonObject["user_id"]!!.jsonPrimitive.long),
                                guild!!.id,
                                jsonObject["name"]!!.jsonPrimitive.content,
                                mutableListOf(jsonObject["name"]!!.jsonPrimitive.content),
                                jsonObject["text"]!!.jsonPrimitive.content,
                                timeCreated = jsonObject["created_at"]!!.jsonPrimitive.content.toInstant()
                            )
                        } catch (t: Throwable) {
                            respond {
                                content = translate("error.partialimport", arrayOf("```\n$t\n```"))
                            }

                            return@action
                        }
                    }

                    respond {
                        content = "Successfully imported ${qbotJson.jsonArray.count()} notes!"
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
                                    content = translate("extensions.utility.clear.success")

                                    components = mutableListOf()
                                }
                            }
                        }

                        ephemeralButton {
                            label = "Cancel"
                            style = ButtonStyle.Secondary

                            action {
                                edit {
                                    content = translate("extensions.management.delete.cancel")

                                    components = mutableListOf()
                                }

                            }
                        }

                        onTimeout {
                            edit {
                                content = translate("extensions.management.delete.cancel")

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
                    value.size > 52428800 // 50 MiB
                }
            }
        }
    }
}


