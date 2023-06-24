/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import org.litote.kmongo.SetTo
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.set

suspend fun v2(database: CoroutineDatabase) {
    val noteCol = database.getCollection<Note>(NoteCollection.name)

    with(noteCol) {
        val notes = aggregate<Note>(
            set(SetTo(Note::aliases, mutableListOf("\$name")))
        ).toList()

        // This is really slow but who cares, it works
        for (note in notes) {
            noteCol.save(note)
        }
    }
}