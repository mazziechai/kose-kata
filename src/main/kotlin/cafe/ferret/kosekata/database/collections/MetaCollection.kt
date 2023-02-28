/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.collections

import cafe.ferret.kosekata.database.Database
import cafe.ferret.kosekata.database.DbCollection
import cafe.ferret.kosekata.database.entities.Meta
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.koin.core.component.inject

class MetaCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Meta>(name)

    suspend fun get() = col.findOne()

    suspend fun set(meta: Meta) = col.save(meta)

    companion object : DbCollection("meta")
}