/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.migrations

import cafe.ferret.kosekata.database.collections.NoteCollection
import cafe.ferret.kosekata.database.entities.Note
import com.mongodb.client.model.Aggregates.addFields
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase

suspend fun v2(database: MongoDatabase) {
    val noteCol = database.getCollection<Note>(NoteCollection.name)

    with(noteCol) {
        aggregate<Note>(
            listOf(
                addFields(
                    Field("aliases", mutableListOf("\$name"))
                )
            )
        ).collect {
            noteCol.replaceOne(eq(Note::_id.name, it._id), it)
        }
    }
}
