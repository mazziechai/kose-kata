/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.utils.respond
import org.koin.core.component.inject

class ChatCommandsExtension : Extension() {
    override val name = "chatCommands"

    private val noteCollection: NoteCollection by inject()

    override val bundle = BUNDLE

    override suspend fun setup() {
        /**
         * Reimplementation of the /post command as a chat command.
         */
        chatCommand(::PostCommandArgs) {
            name = "post"
            description = "Sends to note as a chat (chat command shortcut)"

            check { anyGuild() }

            action {
                val note = noteCollection.getRandomNote(guild!!.id, arguments.noteName)

                if (note == null) {
                    message.respond {
                        content = translate("error.notfound")
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
        }
    }

    inner class PostCommandArgs : Arguments() {
        val noteName by string {
            name = "note"
            description = "The note's name"
        }
    }
}