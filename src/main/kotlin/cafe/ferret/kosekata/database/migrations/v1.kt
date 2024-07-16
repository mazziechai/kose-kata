/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.NoteCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

suspend fun v1(database: MongoDatabase) {
    database.createCollection(NoteCollection.name)
}