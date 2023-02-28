/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.collections

import cafe.ferret.kosekata.database.Database
import cafe.ferret.kosekata.database.DbCollection
import cafe.ferret.kosekata.database.entities.GuildData
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * MongoDB collection for [GuildData].
 */
class GuildDataCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<GuildData>(name)

    /**
     * Creates a new [GuildData] with an empty notes list by default and adds it to the collection.
     *
     * @param id The ID of the guild.
     * @return The created [GuildData].
     */
    suspend fun new(id: Snowflake, notes: MutableList<Int> = mutableListOf()): GuildData {
        val guildData = GuildData(id, notes)
        set(guildData)
        return guildData
    }

    /**
     * Gets a [GuildData] from the collection from a [Snowflake].
     *
     * @param id The ID of the guild.
     * @return The [GuildData], if any.
     */
    suspend fun get(id: Snowflake) = col.findOne(GuildData::_id eq id)

    /**
     * Saves a [GuildData] to the collection.
     *
     * @param guildData The [GuildData] to save to the collection.
     */
    suspend fun set(guildData: GuildData) = col.save(guildData)

    companion object : DbCollection("guild_data")
}