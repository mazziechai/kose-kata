/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.entities.Note
import org.litote.kmongo.coroutine.CoroutineDatabase

/**
 * Renames the quotes collection to notes.
 */
suspend fun v2(database: CoroutineDatabase) {
    val quotes = database.getCollection<Note>("quotes").find().toList()
    if (!quotes.isEmpty()) {
        database.getCollection<Note>("notes").insertMany(quotes)
        database.dropCollection("quotes")
    }
}