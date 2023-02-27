/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.database.entities.Note
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.attachment
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.download
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.Image
import io.ktor.client.request.forms.*
import io.ktor.util.cio.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.seconds

class UtilityExtension : Extension() {
    override val name = "utility"

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
                            content = "This user has no notes."
                        }

                        return@action
                    }

                    editingPaginator {
                        timeoutSeconds = 60

                        notes.chunked(10).forEach { chunkedNotes ->
                            page {
                                author {
                                    name = if (member.nickname != null) {
                                        "${member.nickname} (${member.tag})"
                                    } else {
                                        member.tag
                                    }
                                    icon = member.avatar?.url
                                }

                                title = "Notes"

                                description = buildString {
                                    chunkedNotes.forEach { note ->
                                        append("*${note.name}* | #${note._id.toString(16)} | ")
                                        append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                                        appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                                    }
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
                            content = "This server has no notes."
                        }

                        return@action
                    }

                    val thisGuild = guild!!.asGuild()
                    val cachedUsers = mutableListOf<User>()
                    val unknownUsers = mutableListOf<Snowflake>()

                    editingPaginator {
                        timeoutSeconds = 60

                        notes.chunked(10).forEach { chunkedNotes ->
                            page {
                                author {
                                    name = thisGuild.name
                                    icon = thisGuild.getIconUrl(Image.Format.PNG)
                                }

                                title = "Notes"

                                description = buildString {
                                    chunkedNotes.forEach { note ->
                                        var user = cachedUsers.find { it.id == note.author }

                                        if (user == null) {
                                            if (note.author !in unknownUsers) {
                                                user = this@ephemeralSlashCommand.kord.getUser(note.author)
                                            }
                                        }

                                        if (user == null) {
                                            unknownUsers.add(note.author)
                                        } else {
                                            cachedUsers.add(user)
                                        }

                                        append("${user?.tag ?: "Unknown user"} → ")
                                        append("*${note.name}* | #${note._id.toString(16)} | ")
                                        append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                                        appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                                    }
                                }
                            }
                        }
                    }.send()
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
                        content = "You don't have any notes."
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
                                name = user.tag
                                icon = user.avatar?.url
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
                                    append("*${note.name}* | #${note._id.toString(16)} | ")
                                    append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                                    appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                                }
                            }
                        }
                    }
                }.send()
            }
        }

        ephemeralSlashCommand {
            name = "export"
            description = "Exports all notes to a file"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.ManageGuild))
            }

            action {
                val notes = noteCollection.getByGuild(guild!!.id)

                val json = Json.encodeToString(notes)

                respond {
                    addFile("${guild!!.id}.json", ChannelProvider { json.byteInputStream().toByteReadChannel() })
                }
            }
        }

        ephemeralSlashCommand(::ImportArgs) {
            name = "import"
            description = "Imports all notes from a file"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.ManageGuild))
            }

            action {
                lateinit var notes: List<Note>

                try {
                    notes = Json.decodeFromString(arguments.file.download().toString(Charset.forName("UTF-8")))
                } catch (t: SerializationException) {
                    respond {
                        content = "That's not a valid importable note file."
                    }

                    return@action
                }

                for (note in notes) {
                    noteCollection.new(
                        note.author,
                        guild!!.id,
                        note.name,
                        note.content,
                        note.originalAuthor,
                        note.timeCreated
                    )
                }

                respond {
                    content = "Successfully imported ${notes.count()} notes!"
                }
            }
        }

        ephemeralSlashCommand {
            name = "clear"
            description = "Deletes ALL notes. This is IRREVERSIBLE!"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.ManageGuild))
            }

            action {
                respond {
                    content = buildString {
                        appendLine("Are you sure you want to delete **all** notes? This is **irreversible**.")
                        appendLine("Please backup all notes before doing this action using the `/export` command.")
                    }

                    components(15.seconds) {
                        ephemeralButton {
                            label = "Delete"
                            style = ButtonStyle.Danger

                            action {
                                noteCollection.deleteAllGuild(guild!!.id)

                                edit {
                                    content = "All notes deleted."

                                    components = mutableListOf()
                                }
                            }
                        }

                        ephemeralButton {
                            label = "Cancel"
                            style = ButtonStyle.Secondary

                            action {
                                edit {
                                    content = "Cancelled deletion."

                                    components = mutableListOf()
                                }

                            }
                        }

                        onTimeout {
                            edit {
                                content = "Cancelled deletion."

                                components = mutableListOf()
                            }
                        }
                    }
                }
            }
        }
    }

    inner class UserNotesArgs : Arguments() {
        val user by member {
            name = "user"
            description = "The user to get the notes from"
        }
    }

    inner class ImportArgs : Arguments() {
        val file by attachment {
            name = "file"
            description = "The notes you want to import"

            validate {
                failIf("File is too large!") {
                    value.size > 50 * 1000
                }
            }
        }
    }
}


