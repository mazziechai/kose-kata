/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
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

    override val bundle = BUNDLE

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

                    note.aliases.add(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content =
                            translate("extensions.alias.new.success", arrayOf(arguments.alias, "%06x".format(noteId)))
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

                    if (note.aliases.count() <= 1) {
                        respond {
                            content = translate("extensions.alias.remove.error")
                        }
                        return@action
                    }

                    note.aliases.remove(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content = translate(
                            "extensions.alias.remove.success",
                            arrayOf(arguments.alias, "%06x".format(noteId))
                        )
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
                            content = translate("error.notfound")
                        }
                        return@action
                    }

                    respond {
                        content =
                            translate("extensions.alias.list.success", arrayOf("%06x".format(noteId), note.aliases))
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
                failIf(translate("arguments.noteid.fail")) {
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