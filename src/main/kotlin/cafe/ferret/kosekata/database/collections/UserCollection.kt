/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.collections

import cafe.ferret.kosekata.database.Database
import cafe.ferret.kosekata.database.DbCollection
import cafe.ferret.kosekata.database.entities.BotUser
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
     * Creates a new [BotUser] with an empty notes list by default and adds it to the collection.
     *
     * @param id The ID of the user.
     * @return The created [BotUser].
     */
    suspend fun new(id: Snowflake, notes: MutableList<Int> = mutableListOf()): BotUser {
        val botUser = BotUser(id, notes)
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