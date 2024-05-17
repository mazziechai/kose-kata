/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.BUNDLE
import cafe.ferret.kosekata.ByNameArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import org.koin.core.component.inject

class InfoExtension : Extension() {
    override val name = "info"

    private val noteCollection: NoteCollection by inject()

    override val bundle = BUNDLE

    override suspend fun setup() {
        publicSlashCommand(::ByNameArgs) {
            name = "ids"
            description = "List the IDs of notes under a name"

            check {
                anyGuild()
            }

            action {
                val notes = noteCollection.getByGuildAndName(guild!!.id, arguments.name)

                if (notes.isEmpty()) {
                    respond {
                        content = "I couldn't find any notes under that name."
                    }
                    return@action
                }

                if (notes.count() > 200) {
                    respond {
                        content =
                            "This name has too many note IDs to display without going over the message length limit."
                    }
                    return@action
                }

                respond {
                    content = buildString {
                        append("IDs of notes named ${arguments.name}:\n")
                        for (note in notes) {
                            append("`${note._id.toString(16)}` ")
                        }
                    }
                }
            }
        }
    }
}