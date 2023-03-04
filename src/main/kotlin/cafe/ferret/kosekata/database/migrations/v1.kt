/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(database: CoroutineDatabase) {
    database.createCollection(NoteCollection.name)

    val noteCollection = database.getCollection<Note>(NoteCollection.name)
    // Guild index
    noteCollection.ensureIndex(Note::guild)
    // User index
    noteCollection.ensureIndex(Note::author)
}