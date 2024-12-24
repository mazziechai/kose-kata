/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.edit
import dev.kord.core.event.message.ReactionAddEvent
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.extensions.event
import dev.kordex.core.utils.hasPermission
import dev.kordex.core.utils.respond
import org.koin.core.component.inject

class ChatCommandsExtension : Extension() {
    override val name = "chatCommands"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Reimplementation of the /post command as a chat command.
         */
        chatCommand(::PostCommandArgs) {
            name = Translations.Extensions.Viewing.Post.name
            aliasKey = Translations.Extensions.Viewing.Post.Chatcommand.aliases
            description = Translations.Extensions.Viewing.Post.Chatcommand.description

            check {
                anyGuild()
                failIf {
                    event.member == null
                }
            }

            action {
                val note = noteCollection.getRandomNote(guild!!.id, arguments.noteName)

                if (note == null) {
                    message.respond {
                        content = Translations.Error.notfound.translate()
                    }
                    return@action
                }

                val referenceRegex = Regex("\\{\\{(.+?)}}")
                val references = referenceRegex.findAll(note.content).distinctBy { it.groupValues[1] }

                message.respond {
                    content = "${note.content}\n\n`#%06x` `%s`".format(note._id, note.name)
                    if (references.any()) {
                        content += "\nThis note contains note references, which are only available in the slash command equivalents of this command."
                    }
                }
            }

            event<ReactionAddEvent> {
                check {
                    val interaction = event.getMessage().interaction
                    failIf(event.guild == null)
                    failIf(event.messageAuthorId != bot.kordRef.selfId)
                    failIf(event.emoji.name != "❌")
                    if (interaction == null) {
                        failIf(
                            event.userId != event.getMessage().referencedMessage?.author?.id || event.getUserAsMember()
                                ?.hasPermission(Permission.ManageMessages) == false
                        )
                    } else {
                        failIfNot(interaction.name in listOf("post", "peek", "postid", "peekid"))
                        failIf(
                            event.userId != interaction.user.id || event.getUserAsMember()
                                ?.hasPermission(Permission.ManageMessages) == false
                        )
                    }
                }

                action {
                    event.message.edit {
                        content = "<removed>"
                    }
                }
            }
        }
    }

    inner class PostCommandArgs : Arguments() {
        val noteName by string {
            name = Translations.Arguments.Notename.name
            description = Translations.Arguments.Notename.description
        }
    }
}
