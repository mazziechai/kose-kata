/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject

class ViewingExtension : Extension() {
    override val name = "viewing"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Gets a note and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ViewCommandArgs) {
            name = "view"
            description = "View a note"

            check { anyGuild() }

            action {
                val guildNotes = noteCollection.getByGuild(guild!!.id)

                val note = guildNotes.find { it.name == arguments.noteName }

                if (note == null) {
                    respond {
                        content = "I couldn't find that note."
                    }
                    return@action
                }

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = "${author?.nickname ?: "Unknown user"} (${author?.tag ?: "Unknown user"})"
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content
                    }
                }
            }
        }

        /**
         * Gets a note and sends its contents publicly.
         */
        publicSlashCommand(::ViewCommandArgs) {
            name = "post"
            description = "Posts a note to chat"

            check { anyGuild() }

            action {
                val guildNotes = noteCollection.getByGuild(guild!!.id)

                val note = guildNotes.find { it.name == arguments.noteName }

                if (note == null) {
                    respond {
                        content = "I couldn't find that note."
                    }
                    return@action
                }

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = "${author?.nickname ?: "Unknown user"} (${author?.tag ?: "Unknown user"})"
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content
                    }
                }
            }
        }
    }

    inner class ViewCommandArgs : Arguments() {
        val noteName by string {
            name = "note"
            description = "The note you want to view"
        }
    }
}