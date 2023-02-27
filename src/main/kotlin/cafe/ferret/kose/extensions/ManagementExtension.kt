/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.formatTime
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class ManagementExtension : Extension() {
    override val name = "management"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        ephemeralSlashCommand(::DeleteCommandArgs) {
            name = "delete"
            description = "Delete a note by its ID. This is irreversible!"

            check { anyGuild() }

            action {
                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respond {
                        content = "I couldn't find that note."
                    }

                    return@action
                }

                val author = guild!!.getMemberOrNull(note.author)

                if (author?.id != member!!.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respond {
                        content = "You don't own that note."
                    }
                    return@action
                }

                respond {
                    content = "Are you sure you want to delete this note?"

                    embed {
                        author {
                            name = "${author?.nickname ?: "Unknown user"} (${author?.tag ?: "Unknown user"} )"
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content

                        footer {
                            text = buildString {
                                append("#${note._id.toString(16)} ")
                                append("| Created on ${formatTime(note.timeCreated)}")
                            }
                        }
                    }

                    components(15.seconds) {
                        ephemeralButton {
                            label = "Delete"
                            style = ButtonStyle.Danger

                            action {
                                noteCollection.delete(note)

                                edit {
                                    content = "Note `${note.name}` deleted."

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

    inner class DeleteCommandArgs : Arguments() {
        val noteId by string {
            name = "id"
            description = "The ID of the note you want to delete"

            validate {
                failIf("That's not a valid ID!") {
                    value.toIntOrNull(16) == null
                }
            }
        }
    }
}
