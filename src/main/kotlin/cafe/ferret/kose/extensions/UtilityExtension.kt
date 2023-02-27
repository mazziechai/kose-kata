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
                                        append("${note.name} | #${note._id} | ")
                                        appendLine("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDateTime)}")
                                    }
                                }
                            }
                        }
                    }.send()
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
}


