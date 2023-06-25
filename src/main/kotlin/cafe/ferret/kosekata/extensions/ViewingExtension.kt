/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.extensions

import cafe.ferret.kosekata.ByIdArgs
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import cafe.ferret.kosekata.guildNotes
import cafe.ferret.kosekata.noteEmbed
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringCollection
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import me.xdrop.fuzzywuzzy.FuzzySearch
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

                viewNoteResponse(note)
            }
        }

        /**
         * Gets a note by name and sends its contents publicly.
         */
        publicSlashCommand(::ViewByNameCommandArgs) {
            name = "post"
            description = "Post a note to chat"

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

                viewNoteResponse(note)
            }
        }

        /**
         * Gets a note by ID and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ByIdArgs) {
            name = "viewid"
            description = "View a note by its ID"

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

                viewNoteResponse(note)
            }
        }

        /**
         * Gets a note by ID and then sends its contents publicly.
         */
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

                viewNoteResponse(note)
            }
        }

        ephemeralSlashCommand(::SearchCommandArgs) {
            name = "search"
            description = "Search for a note"

            check { anyGuild() }

            action {
                val guildNotes = noteCollection.getByGuild(guild!!.id)

                if (guildNotes.isEmpty()) {
                    respond {
                        content = "This server has no notes."
                    }
                    return@action
                }

                val noteNames = guildNotes.flatMap { it.aliases }
                val searchResults = FuzzySearch.extractSorted(arguments.searchParam, noteNames, 66).map { it.string }

                val notes = guildNotes.filter { searchResults.any { it in noteNames } }

                if (notes.isEmpty()) {
                    respond {
                        content = "I couldn't find any notes matching the query."
                    }

                    return@action
                }

                guildNotes(this@ephemeralSlashCommand.kord, guild!!.asGuild(), notes, arguments.searchParam)
            }
        }
    }

    inner class ViewByNameCommandArgs : Arguments() {
        val noteName by string {
            name = "note"
            description = "The note's name"

            var notes: List<String>? = null

            autoComplete {
                if (data.guildId.value != null) {
                    if (notes == null) {
                        notes = noteCollection.getByGuild(data.guildId.value!!).flatMap { it.aliases }.distinct()
                    }

                    val noteNames = FuzzySearch.extractTop(focusedOption.value, notes!!, 10).map { it.string }

                    suggestStringCollection(noteNames)
                }
            }
        }
    }

    inner class SearchCommandArgs : Arguments() {
        val searchParam by string {
            name = "name"
            description = "The name to search for"
        }
    }

    /**
     * Recursively calls [edit] to display a [Note].
     *
     * @param note The note to display.
     */
    private suspend fun PublicInteractionContext.viewNoteResponse(
        note: Note,
    ) {
        respond {
            noteEmbed(kord, note)
            noteReferencesComponents(note)
        }

        val regex = Regex("""(http|ftp|https)://([\w_-]+(?:\.[\w_-]+)+)([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])""")
        val urls = regex.findAll(note.content)

        if (!urls.none()) {
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
     * Recursively calls [edit] to display a [Note].
     *
     * @param note The note to display.
     */
    private suspend fun EphemeralInteractionContext.viewNoteResponse(
        note: Note,
    ) {
        respond {
            noteEmbed(kord, note)
            noteReferencesComponents(note)
        }

        val regex = Regex("""(http|ftp|https)://([\w_-]+(?:\.[\w_-]+)+)([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])""")
        val urls = regex.findAll(note.content)

        if (!urls.none()) {
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
     */
    private suspend fun FollowupMessageCreateBuilder.noteReferencesComponents(note: Note): ComponentContainer {
        val referenceRegex = Regex("\\{\\{(.+?)}}")
        val references = referenceRegex.findAll(note.content).distinctBy { it.groupValues[1] }

        val referencedNotes = mutableSetOf<Note>()

        for (reference in references) {
            referencedNotes.addAll(noteCollection.getByGuildAndName(note.guild, reference.groupValues[1]))
        }

        return components {
            if (references.any() && referencedNotes.isNotEmpty()) {
                ephemeralSelectMenu {
                    placeholder = "Referenced notes"

                    references.take(25).forEach { result ->
                        val noteName = result.groupValues[1]

                        val referencedNote =
                            referencedNotes.filter { it.aliases.contains(noteName) }.random()

                        option(noteName, referencedNote._id.toString(16))
                    }

                    action {
                        edit {
                            viewNoteResponse(referencedNotes.first { it._id.toString(16) == selected.first() })
                        }
                    }
                }
            }
        }
    }
}