/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import org.koin.core.component.inject

class CreationExtension : Extension() {
    override val name = "creation"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Context command to create a note from an existing message.
         */
        ephemeralMessageCommand(::CreateNoteFromMessageModal) {
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

                respond { content = "Successfully created note `$noteName` with ID `#%06x`!".format(note._id) }
            }
        }

        /**
         * Slash command to create a note.
         */
        ephemeralSlashCommand(::CreateNoteFromCommandModal) {
            name = "new"
            description = "Create a new note. Opens a text box."

            check { anyGuild() }

            action { modal ->
                newNote(modal!!, user, guild!!)
            }
        }

        ephemeralSlashCommand(::CreateNoteFromCommandModal) {
            name = "note"
            description = "Create a new note. Opens a text box."

            check { anyGuild() }

            action { modal ->
                newNote(modal!!, user, guild!!)
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

    private suspend fun EphemeralInteractionContext.newNote(
        modal: CreateNoteFromCommandModal,
        user: UserBehavior,
        guild: GuildBehavior
    ) {
        val noteName = modal.name.value!!
        val noteContent = modal.content.value!!

        val note = noteCollection.new(user.id, guild.id, noteName, mutableListOf(noteName), noteContent)

        respond {
            content = "Successfully created note `$noteName` with ID `#%06x`!".format(note._id)
            noteEmbed(kord, note, false)
        }
    }
}
