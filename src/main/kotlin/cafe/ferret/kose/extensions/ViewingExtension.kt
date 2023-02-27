/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.ByIdArgs
import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.formatTime
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

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = if (author?.nickname != null) {
                                "${author.nickname} (${author.tag})"
                            } else {
                                author?.tag ?: "Unknown user"
                            }
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content

                        footer {
                            text = buildString {
                                append("#${note._id.toString(16)} ")
                                append("| Created on ${formatTime(note.timeCreated)}")
                            }
                        }
                    }
                }
            }
        }

        /**
         * Gets a note by name and sends its contents publicly.
         */
        publicSlashCommand(::ViewByNameCommandArgs) {
            name = "post"
            description = "Posts a note to chat"

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

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = if (author?.nickname != null) {
                                "${author.nickname} (${author.tag})"
                            } else {
                                author?.tag ?: "Unknown user"
                            }
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content

                        footer {
                            text = buildString {
                                append("#${note._id.toString(16)} ")
                                append("| Created on ${formatTime(note.timeCreated)}")
                            }
                        }
                    }
                }
            }
        }

        /**
         * Gets a note by ID and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ByIdArgs) {
            name = "viewid"
            description = "Views a note by its ID"

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

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = if (author?.nickname != null) {
                                "${author.nickname} (${author.tag})"
                            } else {
                                author?.tag ?: "Unknown user"
                            }
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content

                        footer {
                            text = buildString {
                                append("#${note._id.toString(16)} ")
                                append("| Created on ${formatTime(note.timeCreated)}")
                            }
                        }
                    }
                }
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

                val author = guild!!.getMemberOrNull(note.author)

                respond {
                    embed {
                        author {
                            name = if (author?.nickname != null) {
                                "${author.nickname} (${author.tag})"
                            } else {
                                author?.tag ?: "Unknown user"
                            }
                            icon = author?.avatar?.url
                        }

                        title = note.name

                        description = note.content

                        footer {
                            text = buildString {
                                append("#${note._id.toString(16)} ")
                                append("| Created on ${formatTime(note.timeCreated)}")
                            }
                        }
                    }
                }
            }
        }
    }

    inner class ViewByNameCommandArgs : Arguments() {
        val noteName by string {
            name = "note"
            description = "The note you want to view"
        }
    }
}