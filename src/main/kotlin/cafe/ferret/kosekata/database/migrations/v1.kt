/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.GuildDataCollection
import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.collections.UserCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(database: CoroutineDatabase) {
    database.createCollection(GuildDataCollection.name)
    database.createCollection(NoteCollection.name)
    database.createCollection(UserCollection.name)
}