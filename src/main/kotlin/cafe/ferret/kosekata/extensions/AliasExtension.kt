/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import org.koin.core.component.inject

class AliasExtension : Extension() {
    override val name = "alias"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        publicSlashCommand {
            name = "alias"
            description = "Manage note aliases"

            check { anyGuild() }

            publicSubCommand(::UpdateAliasArgs) {
                name = "new"
                description = "Create a new alias for a note."

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

                    note.aliases.add(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content = "Successfully added alias `${arguments.alias}` to note `#%06x`!".format(noteId)
                    }
                }
            }

            publicSubCommand(::UpdateAliasArgs) {
                name = "remove"
                description = "Remove an alias for a note."

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

                    if (note.aliases.count() <= 1) {
                        respond {
                            content = "You can't remove the last remaining alias for this note."
                        }
                        return@action
                    }

                    note.aliases.remove(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content = "Successfully removed alias `${arguments.alias}` for note `#%06x`!".format(noteId)
                    }
                }
            }

            ephemeralSubCommand(::ByIdArgs) {
                name = "list"
                description = "List a note's aliases"

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = "I couldn't find that note."
                        }
                        return@action
                    }

                    respond {
                        content = buildString {
                            appendLine("Aliases for note `#%06x`:".format(noteId))
                            for (alias in note.aliases) {
                                appendLine("`$alias`")
                            }
                        }
                    }
                }
            }
        }
    }

    inner class UpdateAliasArgs : Arguments() {
        val noteId by string {
            name = "note"
            description = "The note's ID"

            validate {
                failIf("That's not a valid ID!") {
                    value.toIntOrNull(16) == null
                }
            }
        }

        val alias by string {
            name = "alias"
            description = "The alias to add or remove"
            maxLength = 32
        }
    }
}