/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.ByIdArgs
import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.database.entities.Note
import cafe.ferret.kose.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.response.EphemeralMessageInteractionResponse
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import org.koin.core.component.inject

class ViewingExtension : Extension() {
    override val name = "viewing"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Gets a note by name and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ViewByNameCommandArgs) {
            name = "view"
            description = "View a note"

            check { anyGuild() }

            action {
                val guildNotes = noteCollection.getByGuildAndName(guild!!.id, arguments.noteName)

                if (guildNotes.isEmpty()) {
                    respond {
                        content = "I couldn't find that note."
                    }
                    return@action
                }

                val note = guildNotes.random()

                viewNoteResponse(note, guild!!.id)
            }
        }

        /**
         * Gets a note by name and sends its contents publicly.
         */
        publicSlashCommand(::ViewByNameCommandArgs) {
            name = "post"
            description = "Post a note to chat"

            check { anyGuild() }

            action {
                val guildNotes = noteCollection.getByGuildAndName(guild!!.id, arguments.noteName)

                if (guildNotes.isEmpty()) {
                    respond {
                        content = "I couldn't find that note."
                    }
                    return@action
                }

                val note = guildNotes.random()

                viewNoteResponse(note, guild!!.id)
            }
        }

        /**
         * Gets a note by ID and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ByIdArgs) {
            name = "viewid"
            description = "View a note by its ID"

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

                viewNoteResponse(note, guild!!.id)
            }
        }

        publicSlashCommand(::ByIdArgs) {
            name = "postid"
            description = "Post a note to chat by its ID"

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

                viewNoteResponse(note, guild!!.id)
            }
        }
    }

    inner class ViewByNameCommandArgs : Arguments() {
        val noteName by string {
            name = "note"
            description = "The note you want to view"
        }
    }

    private suspend fun PublicInteractionContext.viewNoteResponse(
        note: Note,
        guild: Snowflake,
    ): PublicMessageInteractionResponse {
        return edit {
            noteEmbed(kord, note, guild)
            viewNoteReferences(note, guild)
        }
    }

    private suspend fun EphemeralInteractionContext.viewNoteResponse(
        note: Note,
        guild: Snowflake,
    ): EphemeralMessageInteractionResponse {
        return edit {
            noteEmbed(kord, note, guild)
            viewNoteReferences(note, guild)
        }
    }

    private suspend fun MessageModifyBuilder.viewNoteReferences(note: Note, guild: Snowflake): ComponentContainer {
        val referenceRegex = Regex("\\{\\{(.+?)}}")
        val references = referenceRegex.findAll(note.content).distinctBy { it.groupValues[1] }

        val referencedNotes = mutableSetOf<Note>()

        for (reference in references) {
            referencedNotes.addAll(noteCollection.getByGuildAndName(guild, reference.groupValues[1]))
        }

        return components {
            if (referencedNotes.isNotEmpty()) {
                references.forEach { result ->
                    ephemeralButton {
                        label = result.groupValues[1]

                        val referencedNote =
                            referencedNotes.filter { it.name == result.groupValues[1] }.random()

                        action {
                            viewNoteResponse(referencedNote, guild)
                        }
                    }
                }
            }
        }
    }
}