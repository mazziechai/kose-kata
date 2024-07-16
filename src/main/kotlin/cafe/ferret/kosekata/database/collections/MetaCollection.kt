/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.collections

import cafe.ferret.kosekata.database.Database
import cafe.ferret.kosekata.database.DbCollection
import cafe.ferret.kosekata.database.entities.Meta
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.inject

class MetaCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Meta>(name)

    suspend fun get() = col.find().firstOrNull()

    suspend fun set(meta: Meta) = col.replaceOne(eq(Meta::_id.name, "meta"), meta, ReplaceOptions().upsert(true))

    companion object : DbCollection("meta")
}