/*
 * Copyright (c) 2023 mazziechai
 */

@file:OptIn(ExperimentalStdlibApi::class)

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import cafe.ferret.kosekata.toId
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.utils.hasPermission
import org.koin.core.component.inject

class AliasExtension : Extension() {
    override val name = "alias"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        publicSlashCommand {
            name = Translations.Extensions.Alias.Alias.name
            description = Translations.Extensions.Alias.Alias.description

            check { anyGuild() }

            publicSubCommand(::UpdateAliasArgs) {
                name = Translations.Extensions.Alias.New.name
                description = Translations.Extensions.Alias.New.description

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = Translations.Error.notfound.translate()
                        }
                        return@action
                    }

                    if (note.author != user.id && !member!!.asMember(guild!!.id)
                            .hasPermission(Permission.ManageMessages)
                    ) {
                        respond {
                            content = Translations.Error.notowned.translate()
                        }
                        return@action
                    }

                    note.aliases.add(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content =
                            Translations.Extensions.Alias.New.success.translate(
                                arguments.alias,
                                note._id.toId()
                            )
                    }
                }
            }

            publicSubCommand(::UpdateAliasArgs) {
                name = Translations.Extensions.Alias.Remove.name
                description = Translations.Extensions.Alias.Remove.description

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = Translations.Error.notfound.translate()
                        }
                        return@action
                    }

                    if (note.author != user.id && !member!!.asMember(guild!!.id)
                            .hasPermission(Permission.ManageMessages)
                    ) {
                        respond {
                            content = Translations.Error.notowned.translate()
                        }
                        return@action
                    }

                    if (note.aliases.count() <= 1) {
                        respond {
                            content = Translations.Extensions.Alias.Remove.error.translate()
                        }
                        return@action
                    }

                    note.aliases.remove(arguments.alias)
                    noteCollection.set(note)

                    respond {
                        content = Translations.Extensions.Alias.Remove.success.translate(
                            arguments.alias,
                            note._id.toId()
                        )
                    }
                }
            }

            ephemeralSubCommand(::ByIdArgs) {
                name = Translations.Extensions.Alias.List.name
                description = Translations.Extensions.Alias.List.description

                action {
                    val noteId = arguments.noteId.toInt(16)

                    val note = noteCollection.get(noteId)

                    if (note == null || note.guild != guild!!.id) {
                        respond {
                            content = Translations.Error.notfound.translate()
                        }
                        return@action
                    }

                    respond {
                        content = buildString {
                            appendLine(
                                Translations.Extensions.Alias.List.success.translate(note.name)
                            )
                            for (alias in note.aliases) {
                                appendLine(alias)
                            }
                        }
                    }
                }
            }
        }
    }

    inner class UpdateAliasArgs : Arguments() {
        val noteId by string {
            name = Translations.Arguments.Noteid.name
            description = Translations.Arguments.Noteid.description

            validate {
                failIf(Translations.Arguments.Noteid.fail) {
                    value.toIntOrNull(16) == null
                }
            }
        }

        val alias by string {
            name = Translations.Arguments.Alias.name
            description = Translations.Arguments.Alias.description
            maxLength = 32
        }
    }
}
