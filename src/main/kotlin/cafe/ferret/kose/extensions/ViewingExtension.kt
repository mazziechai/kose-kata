/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.QuoteCollection
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

    private val quoteCollection: QuoteCollection by inject()

    override suspend fun setup() {
        /**
         * Gets a quote and then sends its contents ephemerally.
         */
        ephemeralSlashCommand(::ViewCommandArgs) {
            name = "view"
            description = "View a quote"

            check { anyGuild() }

            action {
                val guildQuotes = quoteCollection.getByGuild(guild!!.id)

                val quote = guildQuotes.find { it.name == arguments.quoteName }

                if (quote == null) {
                    respond {
                        content = "I couldn't find that quote."
                    }
                    return@action
                }

                val author = guild!!.getMemberOrNull(quote.author)

                respond {
                    embed {
                        author {
                            name = "${author?.nickname ?: "Unknown user"} (${author?.tag ?: "Unknown user"})"
                            icon = author?.avatar?.url
                        }

                        title = quote.name

                        description = quote.content
                    }
                }
            }
        }

        /**
         * Gets a quote and sends its contents publicly.
         */
        publicSlashCommand(::ViewCommandArgs) {
            name = "post"
            description = "Posts a quote to chat"

            check { anyGuild() }

            action {
                val guildQuotes = quoteCollection.getByGuild(guild!!.id)

                val quote = guildQuotes.find { it.name == arguments.quoteName }

                if (quote == null) {
                    respond {
                        content = "I couldn't find that quote."
                    }
                    return@action
                }

                val author = guild!!.getMemberOrNull(quote.author)

                respond {
                    embed {
                        author {
                            name = "${author?.nickname ?: "Unknown user"} (${author?.tag ?: "Unknown user"})"
                            icon = author?.avatar?.url
                        }

                        title = quote.name

                        description = quote.content
                    }
                }
            }
        }
    }

    inner class ViewCommandArgs : Arguments() {
        val quoteName by string {
            name = "quote"
            description = "The quote you want to view"
        }
    }
}