/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import cafe.ferret.kosekata.formatTime
import cafe.ferret.kosekata.i18n.Translations
import cafe.ferret.kosekata.noteEmbed
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.effectiveName
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalBoolean
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.components.ComponentContainer
import dev.kordex.core.components.components
import dev.kordex.core.components.ephemeralStringSelectMenu
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.types.InteractionContext
import dev.kordex.core.types.PublicInteractionContext
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class ViewingExtension : Extension() {
    override val name = "viewing"

    private val noteCollection: NoteCollection by inject()

    override suspend fun setup() {
        /**
         * Gets a note by name and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ViewByNameCommandArgs) {
            name = Translations.Extensions.Viewing.Peek.name
            description = Translations.Extensions.Viewing.Peek.description

            check { anyGuild() }

            action {
                val note = noteCollection.getRandomNote(guild!!.id, arguments.noteName)

                if (note == null) {
                    respond {
                        content = Translations.Error.notfound.translate()
                    }
                    return@action
                }

                viewNoteResponse(note, arguments.text != false, guild!!.id)
            }
        }

        /**
         * Gets a note by name and sends its contents publicly.
         */
        publicSlashCommand(::ViewByNameCommandArgs) {
            name = Translations.Extensions.Viewing.Post.name
            description = Translations.Extensions.Viewing.Post.description

            check { anyGuild() }

            action {
                publicNoteByNameAction(guild!!.asGuild(), arguments)
            }
        }

        /**
         * Gets a note by ID and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ViewByIdCommandArgs) {
            name = Translations.Extensions.Viewing.Peekid.name
            description = Translations.Extensions.Viewing.Peekid.description

            check { anyGuild() }

            action {
                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respond {
                        content = Translations.Error.notfound.translate()
                    }
                    return@action
                }

                viewNoteResponse(note, arguments.text != false, guild!!.id)
            }
        }

        /**
         * Gets a note by ID and then sends its contents publicly.
         */
        publicSlashCommand(::ViewByIdCommandArgs) {
            name = Translations.Extensions.Viewing.Postid.name
            description = Translations.Extensions.Viewing.Postid.description

            check { anyGuild() }

            action {
                publicNoteByIdAction(guild!!, arguments)
            }
        }

        publicSlashCommand(::ByIdArgs) {
            name = Translations.Extensions.Viewing.Info.name
            description = Translations.Extensions.Viewing.Info.description

            check { anyGuild() }

            action {
                val noteId = arguments.noteId.toInt(16)

                val note = noteCollection.get(noteId)

                if (note == null || note.guild != guild!!.id) {
                    respond {
                        content = Translations.Error.notfound.translate()
                    }
                    return@action
                }

                val noteUser = this@publicSlashCommand.kord.getUser(note.author)
                val noteMember = noteUser?.asMemberOrNull(note.guild)

                respond {
                    embed(fun EmbedBuilder.() {
                        author {
                            name = if (noteMember?.effectiveName != null) {
                                "${noteMember.effectiveName} (${noteUser.username})"
                            } else {
                                noteUser?.username ?: "Unknown user"
                            }
                            icon = noteMember?.avatar?.cdnUrl?.toUrl()
                        }

                        title = note.name

                        description = buildString {
                            appendLine("Created on ${formatTime(note.timeCreated)}")
                            if (note.aliases.count() > 1) {
                                appendLine("Aliases: ${note.aliases.drop(1)}")
                            }
                            if (note.originalAuthor != null) {
                                val originalAuthor = this@publicSlashCommand.kord.getUser(note.originalAuthor)
                                appendLine("Original author: ${originalAuthor?.effectiveName} (${originalAuthor?.username})")
                            }
                        }

                        color = Color(note._id)

                        footer {
                            text = "#%06x".format(note._id)
                        }
                    })
                }
            }
        }
    }

    inner class ViewByNameCommandArgs : Arguments() {
        val noteName by string {
            name = Translations.Arguments.Notename.name
            description = Translations.Arguments.Notename.description
        }

        val text by optionalBoolean {
            name = Translations.Arguments.Text.name
            description = Translations.Arguments.Text.description
        }
    }

    inner class ViewByIdCommandArgs : ByIdArgs() {
        val text by optionalBoolean {
            name = Translations.Arguments.Text.name
            description = Translations.Arguments.Text.description
        }
    }

    /**
     * Recursively calls edit to display a [Note].
     *
     * @param note The note to display.
     * @param text If there should be just text instead of an embed.
     */
    private suspend fun InteractionContext<*, *, *, *>.viewNoteResponse(
        note: Note,
        text: Boolean,
        guild: Snowflake
    ) {
        respond {
            if (!text) {
                noteEmbed(kord, note, false)
            } else {
                content = "${note.content}\n\n`#%06x` `%s`".format(note._id, note.name)
            }
            noteReferencesComponents(note, text, guild)
        }

        val regex = Regex("""<?(http|ftp|https)://([\w_-]+(?:\.[\w_-]+)+)([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])>?""")
        val urls = regex.findAll(note.content)

        if (!urls.none() && !text) {
            respond {
                this@respond.content = buildString {
                    appendLine("**URLs** (for embeds)")
                    for (url in urls) {
                        appendLine(url.value)
                    }
                }
            }
        }
    }

    /**
     * Creates reference components that allow for recursive movement between notes.
     *
     * @param note The [Note] to create reference components for.
     * @param text The text parameter from the viewNoteResponse.
     */
    private suspend fun FollowupMessageCreateBuilder.noteReferencesComponents(
        note: Note,
        text: Boolean,
        guild: Snowflake
    ): ComponentContainer {
        val referenceRegex = Regex("\\{\\{(.+?)}}")
        val references =
            referenceRegex.findAll(note.content).distinctBy { it.groupValues[1] }.map { it.groupValues[1] }.toSet()

        val idReferenceRegex = Regex("\\[\\[(.+?)]]")
        val idReferences =
            idReferenceRegex.findAll(note.content).distinctBy { it.groupValues[1] }.map { it.groupValues[1] }.toSet()

        return components(15.seconds) {
            if (references.isNotEmpty()) {
                ephemeralStringSelectMenu {
                    placeholder = "Referenced notes".toKey()

                    references.take(25).forEach { reference ->
                        option(reference.toKey(), reference)
                    }

                    action {
                        val note = noteCollection.getByGuildAndName(guild, selected.first()).randomOrNull()

                        if (note == null) {
                            respond {
                                content = Translations.Error.notfound.translate()
                            }
                            return@action
                        }

                        viewNoteResponse(note, text, guild)
                    }
                }
            }

            if (idReferences.isNotEmpty()) {
                ephemeralStringSelectMenu {
                    placeholder = "ID referenced notes".toKey()

                    idReferences.take(25).forEach { reference ->
                        option(reference.toKey(), reference)
                    }

                    action {
                        val id = selected.first().toIntOrNull(16)

                        if (id == null) {
                            respond {
                                content = Translations.Arguments.Noteid.fail.translate()
                            }
                            return@action
                        }

                        val note = noteCollection.get(id)

                        if (note == null || note.guild != guild) {
                            respond {
                                content = Translations.Error.notfound.translate()
                            }
                            return@action
                        }

                        viewNoteResponse(note, text, guild)
                    }
                }
            }
        }
    }

    private suspend fun PublicInteractionContext.publicNoteByNameAction(
        guild: GuildBehavior,
        arguments: ViewByNameCommandArgs
    ) {
        val note = noteCollection.getRandomNote(guild.id, arguments.noteName)

        if (note == null) {
            respond {
                content = Translations.Error.notfound.translate()
            }
            return
        }

        viewNoteResponse(note, arguments.text != false, guild.id)
    }

    private suspend fun PublicInteractionContext.publicNoteByIdAction(
        guild: GuildBehavior,
        arguments: ViewByIdCommandArgs
    ) {
        val noteId = arguments.noteId.toInt(16)

        val note = noteCollection.get(noteId)

        if (note == null || note.guild != guild.id) {
            respond {
                content = Translations.Error.notfound.translate()
            }
            return
        }

        viewNoteResponse(note, arguments.text != false, guild.id)
    }
}