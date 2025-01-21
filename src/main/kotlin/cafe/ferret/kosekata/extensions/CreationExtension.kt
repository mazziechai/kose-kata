/*
 * Copyright (c) 2023 mazziechai
 */

@file:OptIn(ExperimentalStdlibApi::class)

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import cafe.ferret.kosekata.noteEmbed
import cafe.ferret.kosekata.toId
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicMessageCommand
import dev.kordex.core.extensions.publicSlashCommand
import org.koin.core.component.inject

class CreationExtension : Extension() {
    override val name = "creation"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Context command to create a note from an existing message.
         */
        publicMessageCommand(::CreateNoteFromMessageModal) {
            name = Translations.Extensions.Creation.Newmessage.name

            check { anyGuild() }

            action { modal ->
                val noteName = modal?.name!!.value!!

                val message = targetMessages.first()
                val note = noteCollection.new(
                    user.id,
                    guild!!.id,
                    noteName,
                    mutableListOf(noteName),
                    message.content,
                    message.author?.id
                )

                respond {
                    content =
                        Translations.Extensions.Creation.success.translate(noteName, note._id.toId())
                }
            }
        }

        /**
         * Slash command to create a note.
         */
        publicSlashCommand(::CreateNoteFromCommandModal) {
            name = Translations.Extensions.Creation.New.name
            description = Translations.Extensions.Creation.New.description

            check { anyGuild() }

            action { modal ->
                val noteName = modal!!.name.value!!
                val noteContent = modal.content.value!!

                val note = noteCollection.new(user.id, guild!!.id, noteName, mutableListOf(noteName), noteContent)

                respond {
                    content =
                        Translations.Extensions.Creation.success.translate(noteName, note._id.toId())
                    noteEmbed(this@publicSlashCommand.kord, note, false)
                }
            }
        }
    }

    inner class CreateNoteFromMessageModal : ModalForm() {
        override var title = Translations.Modals.Createnote.title

        val name = lineText {
            label = Translations.Modals.notename
            required = true
            maxLength = 32
        }
    }

    inner class CreateNoteFromCommandModal : ModalForm() {
        override var title = Translations.Modals.Createnote.title

        val name = lineText {
            label = Translations.Modals.notename
            required = true
            maxLength = 32
        }

        val content = paragraphText {
            label = Translations.Modals.content
            required = true
            maxLength = 2000
        }
    }
}
