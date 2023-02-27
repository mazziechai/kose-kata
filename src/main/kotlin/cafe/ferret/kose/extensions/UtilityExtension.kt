/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import org.koin.core.component.inject

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
                                                user = this@ephemeralSlashCommand.kord.getUser(note.guild)
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
    }

    inner class UserNotesArgs : Arguments() {
        val user by member {
            name = "user"
            description = "The user to get the notes from"
        }
    }
}


