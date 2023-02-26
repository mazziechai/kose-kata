/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.migrations

import cafe.ferret.kose.database.collections.GuildDataCollection
import cafe.ferret.kose.database.collections.QuoteCollection
import cafe.ferret.kose.database.collections.UserCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(database: CoroutineDatabase) {
    database.createCollection(GuildDataCollection.name)
    database.createCollection(QuoteCollection.name)
    database.createCollection(UserCollection.name)
}