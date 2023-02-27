/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
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

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.SendMessages))
            }

            action { modal ->
                val noteName = modal?.name!!.value

                val message = targetMessages.first()
                val note = noteCollection.new(user.id, guild!!.id, noteName!!, message.content, message.author?.id)

                respond { content = "Successfully created note `$noteName` with ID `#${note._id.toString(16)}`!" }

            }
        }

        /**
         * Slash command to create a note.
         */
        ephemeralSlashCommand(::CreateNoteFromCommandModal) {
            name = "new"
            description = "Create a new note"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.SendMessages))
            }

            action { modal ->
                val noteName = modal?.name!!.value
                val noteContent = modal.content.value

                val note = noteCollection.new(user.id, guild!!.id, noteName!!, noteContent!!)

                respond { content = "Successfully created note `$noteName` with ID `#${note._id.toString(16)}`!" }
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
