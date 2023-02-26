/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import cafe.ferret.kose.database.collections.QuoteCollection
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
import org.koin.core.component.inject

class CreationExtension : Extension() {
    override val name = "creation"

    private val quoteCollection: QuoteCollection by inject()

    override suspend fun setup() {
        /**
         * Context command to create a quote from an existing message.
         */
        ephemeralMessageCommand(::CreateQuoteFromMessageModal) {
            name = "New quote"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.SendMessages))
            }

            action { modal ->
                val quoteName = modal?.name!!.value

                val message = targetMessages.first()
                quoteCollection.new(member!!.id, guild!!.id, quoteName!!, message.content, message.author?.id)

                respond { content = "Successfully created quote `$quoteName`!" }

            }
        }

        /**
         * Slash command to create a quote.
         */
        ephemeralSlashCommand(::CreateQuoteFromCommandModal) {
            name = "new"
            description = "Create a new quote"

            check {
                anyGuild()
                val member = event.interaction.user as Member
                failIfNot(member.hasPermission(Permission.SendMessages))
            }

            action { modal ->
                val quoteName = modal?.name!!.value
                val quoteContent = modal.content.value

                quoteCollection.new(member!!.id, guild!!.id, quoteName!!, quoteContent!!)

                respond { content = "Successfully created quote `$quoteName`!" }
            }
        }
    }

    inner class CreateQuoteFromMessageModal : ModalForm() {
        override var title = "Create quote"

        val name = lineText {
            label = "Name of the quote"
            required = true
            maxLength = 32
        }
    }

    inner class CreateQuoteFromCommandModal : ModalForm() {
        override var title = "Create quote"

        val name = lineText {
            label = "Name of the quote"
            required = true
            maxLength = 32
        }

        val content = paragraphText {
            label = "Content of the quote"
            required = true
            maxLength = 2000
        }

    }
}
