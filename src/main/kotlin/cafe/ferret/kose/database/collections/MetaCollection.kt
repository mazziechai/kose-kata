/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.collections

import cafe.ferret.kose.database.Database
import cafe.ferret.kose.database.DbCollection
import cafe.ferret.kose.database.entities.Meta
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.koin.core.component.inject

class MetaCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Meta>(name)

    suspend fun get() = col.findOne()

    suspend fun set(meta: Meta) = col.save(meta)

    companion object : DbCollection("meta")
}