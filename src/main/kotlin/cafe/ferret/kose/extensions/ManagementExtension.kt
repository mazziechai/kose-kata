/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.ByIdArgs
import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.formatTime
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
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
        ephemeralSlashCommand(::ByIdArgs) {
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

                val authorUser = this@ephemeralSlashCommand.kord.getUser(note.author)
                val authorMember = guild!!.getMemberOrNull(note.author)

                if (authorMember?.id != member!!.id && !member!!.asMember(guild!!.id)
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
                            name = if (authorMember?.nickname != null) {
                                "${authorMember.nickname} (${authorMember.tag})"
                            } else {
                                authorUser?.tag ?: "Unknown user"
                            }
                            icon = authorMember?.avatar?.url
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

        ephemeralSlashCommand(::ByIdArgs, ::EditModal) {
            name = "edit"
            description = "Edit a note by its ID."

            check { anyGuild() }

            action { modal ->

                if (modal == null) {
                    throw IllegalStateException("Could not find modal!")
                }

                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respond {
                        content = "I couldn't find that note."
                    }

                    return@action
                }

                val authorUser = this@ephemeralSlashCommand.kord.getUser(note.author)
                val authorMember = guild!!.getMemberOrNull(note.author)

                if (authorMember?.id != member!!.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respond {
                        content = "You don't own that note."
                    }
                    return@action
                }

                note.content = modal.content.value!!

                noteCollection.set(note)

                respond {
                    content = "Updated note `${note.name}`!"

                    embed {
                        author {
                            name = if (authorMember?.nickname != null) {
                                "${authorMember.nickname} (${authorMember.tag})"
                            } else {
                                authorUser?.tag ?: "Unknown user"
                            }
                            icon = authorMember?.avatar?.url
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
                }
            }
        }
    }

    inner class EditModal : ModalForm() {
        override var title = "Edit note"

        val content = paragraphText {
            label = "Content of the note"
            required = true
            maxLength = 2000
        }
    }
}
