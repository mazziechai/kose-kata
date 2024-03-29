/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import org.koin.core.component.inject

class CreationExtension : Extension() {
    override val name = "creation"

    private val noteCollection: NoteCollection by inject()

    override val bundle = BUNDLE

    override suspend fun setup() {
        /**
         * Context command to create a note from an existing message.
         */
        publicMessageCommand(::CreateNoteFromMessageModal) {
            name = "New note"

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
                    content = translate("extensions.creation.success", arrayOf(noteName, "%06x".format(note._id)))
                }
            }
        }

        /**
         * Slash command to create a note.
         */
        publicSlashCommand(::CreateNoteFromCommandModal) {
            name = "new"
            description = "Create a new note. Opens a text box."

            check { anyGuild() }

            action { modal ->
                val noteName = modal!!.name.value!!
                val noteContent = modal.content.value!!

                val note = noteCollection.new(user.id, guild!!.id, noteName, mutableListOf(noteName), noteContent)

                respond {
                    content = translate("extensions.creation.success", arrayOf(noteName, "%06x".format(note._id)))
                    noteEmbed(this@publicSlashCommand.kord, note, false)
                }
            }
        }
    }

    inner class CreateNoteFromMessageModal : ModalForm() {
        override var title = "Create note"

        val name = lineText {
            label = "Name of the note"
            required = true
            maxLength = 32
        }
    }

    inner class CreateNoteFromCommandModal : ModalForm() {
        override var title = "Create note"

        val name = lineText {
            label = "Name of the note"
            required = true
            maxLength = 32
        }

        val content = paragraphText {
            label = "Content of the note"
            required = true
            maxLength = 2000
        }
    }
}
