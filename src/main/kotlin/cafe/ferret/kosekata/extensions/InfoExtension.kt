/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByNameArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.i18n.Translations
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import org.koin.core.component.inject

class InfoExtension : Extension() {
    override val name = "info"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        publicSlashCommand(::ByNameArgs) {
            name = Translations.Extensions.Info.Ids.name
            description = Translations.Extensions.Info.Ids.description

            check {
                anyGuild()
            }

            action {
                val notes = noteCollection.getByGuildAndName(guild!!.id, arguments.name)

                if (notes.isEmpty()) {
                    respond {
                        content = Translations.Error.notfoundname.translate()
                    }
                    return@action
                }

                if (notes.count() > 200) {
                    respond {
                        content = Translations.Error.toomanynotes.translate()
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
