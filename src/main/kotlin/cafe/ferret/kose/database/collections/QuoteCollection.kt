/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.collections

import cafe.ferret.kose.database.Database
import cafe.ferret.kose.database.DbCollection
import cafe.ferret.kose.database.entities.BotUser
import cafe.ferret.kose.database.entities.Quote
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.koin.core.component.inject
import org.litote.kmongo.eq
import kotlin.random.Random

/**
 * MongoDB collection for [Quote].
 */
class QuoteCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Quote>(name)

    private val userCollection: UserCollection by inject()
    private val guildDataCollection: GuildDataCollection by inject()

    /**
     * Creates a new [Quote] and adds it to the collection, updating the other collection data as well.
     * This method creates a randomly generated ID for the [Quote].
     *
     * @param author The author's [Snowflake].
     * @param guild The guild that this quote belongs to, as a [Snowflake].
     * @param content The quote's content.
     *
     * @return The created [Quote].
     */
    suspend fun new(
        author: Snowflake,
        guild: Snowflake,
        name: String,
        content: String,
        originalAuthor: Snowflake? = null
    ): Quote {
        var id: Int

        do {
            id = Random.nextInt(0x0, 0xFFFFFF)
        } while (col.countDocuments(Quote::_id eq id) != 0L)

        val quote = Quote(id, author, guild, name, content, originalAuthor)
        set(quote)

        val botUser = userCollection.get(author)

        if (botUser == null) {
            userCollection.new(author, mutableListOf(quote))
        } else {
            botUser.quotes.add(quote)
            userCollection.set(botUser)
        }

        val guildData = guildDataCollection.get(guild)

        if (guildData == null) {
            guildDataCollection.new(guild, mutableListOf(quote))
        } else {
            guildData.quotes.add(quote)
            guildDataCollection.set(guildData)
        }

        return quote
    }

    /**
     * Gets a [Quote] from its ID.
     *
     * @param id The ID of the quote.
     * @return The [Quote], if any.
     */
    suspend fun get(id: Int) = col.findOne(Quote::_id eq id)

    /**
     * Saves a [Quote] to the collection.
     *
     * @param quote The [Quote] to save to the collection.
     */
    suspend fun set(quote: Quote) = col.save(quote)

    /**
     * Gets [Quote]s from a user's [Snowflake].
     *
     * @param botUser The user's snowflake.
     * @return The [Quote]s, if any.
     */
    suspend fun getByUser(botUser: BotUser) = col.find(Quote::author eq botUser._id).toList()

    /**
     * Gets [Quote]s from a [Snowflake].
     *
     * @param guild The guild's snowflake.
     * @return The [Quote]s, if any.
     */
    suspend fun getByGuild(guild: Snowflake) = col.find(Quote::guild eq guild).toList()

    companion object : DbCollection("quotes")
}