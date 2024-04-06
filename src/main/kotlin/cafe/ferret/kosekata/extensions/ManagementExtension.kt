/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.UserNotesArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.edit
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class ManagementExtension : Extension() {
    override val name = "management"

    private val noteCollection: NoteCollection by inject()

    override val bundle = BUNDLE

    @OptIn(UnsafeAPI::class)
    override suspend fun setup() {
        publicSlashCommand {
            name = "delete"
            description = "Note deletion"

            check { anyGuild() }

            publicSubCommand(::ByIdArgs) {
                name = "id"
                description = "Delete a note by its ID. This is irreversible!"

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = translate("error.notfound")
                        }

                        return@action
                    }

                    if (note.author != user.id && !member!!.asMember(guild!!.id)
                            .hasPermission(Permission.ManageMessages)
                    ) {
                        respond {
                            content = translate("error.notowned")
                        }
                        return@action
                    }

                    respond {
                        content = translate("extensions.management.delete.confirmation")

                        noteEmbed(this@publicSlashCommand.kord, note, true)

                        components(15.seconds) {
                            ephemeralButton {
                                label = translate("button.delete.label")
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.delete(note)

                                    edit {
                                        content = translate(
                                            "extensions.management.delete.success",
                                            arrayOf("%06x".format(noteId))
                                        )

                                        components = mutableListOf()
                                    }
                                }
                            }

                            ephemeralButton {
                                label = translate("button.cancel.label")
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
                                    components = mutableListOf()
                                }
                            }
                        }
                    }
                }
            }

            publicSubCommand(::UserNotesArgs) {
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
                            content = translate("error.usernonotes")
                        }

                        return@action
                    }

                    respond {
                        content = translate("extensions.management.deleteuser.confirmation", arrayOf(member.mention))

                        components(15.seconds) {
                            ephemeralButton {
                                label = translate("button.delete.label")
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteByUserInGuild(member.id, guild!!.id)

                                    edit {
                                        content = translate(
                                            "extensions.management.deleteuser.success",
                                            arrayOf(member.mention)
                                        )

                                        components = mutableListOf()
                                    }
                                }
                            }

                            ephemeralButton {
                                label = translate("button.cancel.label")
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
                                    components = mutableListOf()
                                }
                            }
                        }
                    }
                }
            }

            publicSubCommand(::DeleteMultipleModal) {
                name = "multiple"
                description = "Delete multiple notes by their IDs. This is irreversible!"

                check {
                    hasPermission(Permission.ManageMessages)
                }

                action { modal ->
                    if (modal == null) {
                        throw IllegalStateException("Could not find modal!")
                    }

                    val noteIdsSplit = modal.notes.value!!.split(" ")

                    val noteIds = noteIdsSplit.map {
                        try {
                            it.toInt(16)
                        } catch (_: NumberFormatException) {
                            respond {
                                content = translate("extensions.management.deletemultiple.invalidid", arrayOf(it))
                            }

                            return@action
                        }
                    }


                    val notes = noteCollection.getMultipleNotes(noteIds).filter { it.guild == guild!!.id }

                    if (notes.isEmpty()) {
                        respond {
                            content = translate("extensions.management.deletemultiple.nonotes")
                        }

                        return@action
                    }

                    respond {
                        // TODO: List more information about the notes being deleted
                        content = translate("extensions.management.deletemultiple.confirmation", arrayOf(notes.count()))

                        if (notes.count() != noteIds.count()) {
                            content += translate(
                                "extensions.management.deletemultiple.notfound",
                                arrayOf(noteIds.count() - notes.count())
                            )
                        }

                        components(15.seconds) {
                            ephemeralButton {
                                label = translate("button.deleteall.label")
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteMany(notes)

                                    edit {
                                        content = translate(
                                            "extensions.management.deletemultiple.success",
                                            arrayOf(notes.count())
                                        )

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
                                    components = mutableListOf()
                                }
                            }
                        }
                    }

                }
            }
        }

        unsafeSlashCommand(::ByIdArgs) {
            name = "edit"
            description = "Edit a note by its ID. Opens a text box."

            initialResponse = InitialSlashCommandResponse.None

            check { anyGuild() }

            action {
                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respondEphemeral {
                        content = translate("error.notfound")
                    }

                    return@action
                }

                if (note.author != user.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respondEphemeral {
                        content = translate("error.notowned")
                    }
                    return@action
                }

                val modal = EditModal()
                this@unsafeSlashCommand.componentRegistry.register(modal)

                modal.content.initialValue = note.content

                val result = modal.sendAndDeferEphemeral(this)

                if (result == null) {
                    // Modal timed out
                    edit {
                        content = "Modal timed out."
                    }
                    return@action
                }

                note.content = modal.content.value!!

                noteCollection.set(note)

                result.createEphemeralFollowup {
                    content = translate("extensions.management.edit.success", arrayOf("%06x".format(noteId)))

                    noteEmbed(this@unsafeSlashCommand.kord, note, true)
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

        val notes = paragraphText {
            label = "Notes to delete, separated by spaces"
            required = true
            maxLength = 2000
        }
    }
}
