/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.NoteCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(database: CoroutineDatabase) {
    database.createCollection(NoteCollection.name)
}