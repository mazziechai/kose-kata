/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.noteEmbed
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

                if (note.author != user.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respond {
                        content = "You don't own that note."
                    }
                    return@action
                }

                edit {
                    content = "Are you sure you want to delete this note?"

                    noteEmbed(this@ephemeralSlashCommand.kord, note)

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

                if (note.author != user.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respond {
                        content = "You don't own that note."
                    }
                    return@action
                }

                note.content = modal.content.value!!

                noteCollection.set(note)

                edit {
                    content = "Updated note `${note.name}`!"

                    noteEmbed(this@ephemeralSlashCommand.kord, note)
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
