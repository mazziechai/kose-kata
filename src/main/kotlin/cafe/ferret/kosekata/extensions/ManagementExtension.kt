/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.UserNotesArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
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
        ephemeralSlashCommand {
            name = "delete"
            description = "Note deletion"

            check { anyGuild() }

            ephemeralSubCommand(::ByIdArgs) {
                name = "id"
                description = "Delete a note by its ID. This is irreversible!"

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

                    respond {
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

            ephemeralSubCommand(::UserNotesArgs) {
                name = "user"
                description = "Delete all notes from a user. This is irreversible!"

                check {
                    hasPermission(Permission.ManageMessages)
                }

                action {
                    val member = arguments.user

                    val notes = noteCollection
                            .getByUser(member.id)
                            .filter { it.guild == guild!!.id }

                    if (notes.isEmpty()) {
                        respond {
                            content = "This user has no notes."
                        }

                        return@action
                    }

                    respond {
                        content = "Are you sure you want to delete ALL of ${member.mention}'s notes?"

                        components(15.seconds) {
                            ephemeralButton {
                                label = "Delete"
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteByUserInGuild(member.id, guild!!.id)

                                    edit {
                                        content = "All notes from ${member.mention} deleted."

                                        components = mutableListOf()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand(::DeleteMultipleModal) {
                name = "multiple"
                description = "Delete multiple notes by their IDs. This is irreversible!"

                check {
                    hasPermission(Permission.ManageMessages)
                }

                action { modal ->
                    if (modal == null) {
                        throw IllegalStateException("Could not find modal!")
                    }

                    val noteIdsSplit = modal.content.value!!.split(" ")

                    val noteIds = noteIdsSplit.map {
                        try {
                            it.toInt(16)
                        } catch (_: NumberFormatException) {
                            respond {
                                content = "$it is an invalid ID."
                            }

                            return@action
                        }
                    }


                    val notes = noteCollection.getMultipleNotes(noteIds).filter { it.guild == guild!!.id }

                    if (notes.isEmpty()) {
                        respond {
                            content = "I couldn't find any notes."
                        }

                        return@action
                    }

                    respond {
                        // TODO: List more information about the notes being deleted
                        content = "Are you sure you want to delete ${notes.count()} note(s)?"

                        if (notes.count() != noteIds.count()) {
                            content += "\n(${noteIds.count() - notes.count()} note(s) were not found)"
                        }

                        components(15.seconds) {
                            ephemeralButton {
                                label = "Delete all"
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteMany(notes)

                                    edit {
                                        content = "${notes.count()} notes deleted."

                                        components = mutableListOf()
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        ephemeralSlashCommand(::ByIdArgs, ::EditModal) {
            name = "edit"
            description = "Edit a note by its ID. Opens a text box."

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

                respond {
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

    inner class DeleteMultipleModal : ModalForm() {
        override var title = "Delete multiple notes"

        val content = paragraphText {
            label = "Notes to delete, separated by spaces"
            required = true
            maxLength = 2000
        }
    }
}
