/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.UserNotesArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import cafe.ferret.kosekata.noteEmbed
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.components.components
import dev.kordex.core.components.ephemeralButton
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.utils.hasPermission
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.InitialSlashCommandResponse
import dev.kordex.modules.dev.unsafe.extensions.unsafeSlashCommand
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class ManagementExtension : Extension() {
    override val name = "management"

    private val noteCollection: NoteCollection by inject()

    @OptIn(UnsafeAPI::class)
    override suspend fun setup() {
        publicSlashCommand {
            name = Translations.Extensions.Management.Delete.name
            description = Translations.Extensions.Management.Delete.description

            check { anyGuild() }

            publicSubCommand(::ByIdArgs) {
                name = Translations.Extensions.Management.Deleteid.name
                description = Translations.Extensions.Management.Deleteid.description

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = Translations.Error.notfound.translate()
                        }

                        return@action
                    }

                    if (note.author != user.id && !member!!.asMember().hasPermission(Permission.ManageMessages)
                    ) {
                        respond {
                            content = Translations.Error.notowned.translate()
                        }
                        return@action
                    }

                    respond {
                        content = Translations.Extensions.Management.Delete.confirmation.translate()

                        noteEmbed(this@publicSlashCommand.kord, note, true)

                        components(15.seconds) {
                            ephemeralButton {
                                label = Translations.Button.Delete.label
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.delete(note)

                                    edit {
                                        content = Translations.Extensions.Management.Delete.success.translate(
                                            "%06x".format(noteId)
                                        )

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

            publicSubCommand(::UserNotesArgs) {
                name = Translations.Extensions.Management.Deleteuser.name
                description = Translations.Extensions.Management.Deleteuser.description

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
                            content = Translations.Error.usernonotes.translate()
                        }

                        return@action
                    }

                    respond {
                        content = Translations.Extensions.Management.Deleteuser.confirmation.translate(member.mention)

                        components(15.seconds) {
                            ephemeralButton {
                                label = Translations.Button.Delete.label
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteByUserInGuild(member.id, guild!!.id)

                                    edit {
                                        content =
                                            Translations.Extensions.Management.Deleteuser.success.translate(member.mention)

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

            publicSubCommand(::DeleteMultipleModal) {
                name = Translations.Extensions.Management.Deletemultiple.name
                description = Translations.Extensions.Management.Deletemultiple.description

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
                                content = Translations.Extensions.Management.Deletemultiple.invalidid.translate(it)
                            }

                            return@action
                        }
                    }


                    val notes = noteCollection.getMultipleNotes(noteIds).filter { it.guild == guild!!.id }

                    if (notes.isEmpty()) {
                        respond {
                            content = Translations.Extensions.Management.Deletemultiple.nonotes.translate()
                        }

                        return@action
                    }

                    respond {
                        // TODO: List more information about the notes being deleted
                        content =
                            Translations.Extensions.Management.Deletemultiple.confirmation.translate(notes.count())

                        if (notes.count() != noteIds.count()) {
                            content += Translations.Extensions.Management.Deletemultiple.notfound.translate(noteIds.count() - notes.count())
                        }

                        components(15.seconds) {
                            ephemeralButton {
                                label = Translations.Button.Deleteall.label
                                style = ButtonStyle.Danger

                                action {
                                    noteCollection.deleteMany(notes)

                                    edit {
                                        content =
                                            Translations.Extensions.Management.Deletemultiple.success.translate(notes.count())

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
        }

        unsafeSlashCommand(::ByIdArgs) {
            name = Translations.Extensions.Management.Edit.name
            description = Translations.Extensions.Management.Edit.description

            initialResponse = InitialSlashCommandResponse.None

            check { anyGuild() }

            action {
                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respondPublic {
                        content = Translations.Error.notfound.translate()
                    }

                    return@action
                }

                if (note.author != user.id && !member!!.asMember(guild!!.id)
                        .hasPermission(Permission.ManageMessages)
                ) {
                    respondPublic {
                        content = Translations.Error.notowned.translate()
                    }
                    return@action
                }

                val modal = EditModal()
                this@unsafeSlashCommand.componentRegistry.register(modal)

                modal.content.initialValue = note.content.toKey()

                val result = modal.sendAndDeferPublic(this)

                if (result == null) {
                    // Modal timed out
                    edit {
                        content = "Modal timed out."
                    }
                    return@action
                }

                note.content = modal.content.value!!

                noteCollection.set(note)

                result.createPublicFollowup {
                    content = Translations.Extensions.Management.Edit.success.translate("%06x".format(noteId))

                    noteEmbed(this@unsafeSlashCommand.kord, note, true)
                }
            }
        }
    }

    inner class EditModal : ModalForm() {
        override var title = Translations.Modals.Editnote.title

        val content = paragraphText {
            label = Translations.Modals.content
            required = true
            maxLength = 2000
        }
    }

    inner class DeleteMultipleModal : ModalForm() {
        override var title = Translations.Modals.Deletemultiple.title

        val notes = paragraphText {
            label = Translations.Modals.Deletemultiple.content
            required = true
            maxLength = 2000
        }
    }
}
