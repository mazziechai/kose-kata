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

/**
 * MongoDB collection for [BotUser].
 */
class UserCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<BotUser>(name)

    /**
     * Creates a new [BotUser] with an empty quotes list by default and adds it to the collection.
     *
     * @param id The ID of the user.
     * @return The created [BotUser].
     */
    suspend fun new(id: Snowflake, quotes: MutableList<Quote> = mutableListOf()): BotUser {
        val botUser = BotUser(id, quotes)
        set(botUser)
        return botUser
    }

    /**
     * Gets a [BotUser] from the collection from a [Snowflake].
     *
     * @param id The ID of the user.
     * @return The [BotUser], if any.
     */
    suspend fun get(id: Snowflake) = col.findOne(BotUser::_id eq id)

    /**
     * Saves a [BotUser] to the collection.
     *
     * @param user The [BotUser] to save to the collection.
     */
    suspend fun set(user: BotUser) = col.save(user)

    companion object : DbCollection("bot_users")
}