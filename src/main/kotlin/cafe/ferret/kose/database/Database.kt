/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


/**
 * Contains the MongoDB database.
 *
 * @param connectionString A valid MongoDB connection string.
 */
class Database(connectionString: String) {
    private val clientSettings = MongoClientSettings
        .builder()
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .applyConnectionString(ConnectionString(connectionString))
        .build()

    private val client = KMongo.createClient(clientSettings).coroutine

    val mongo = client.getDatabase("kose")

    suspend fun migrate() {
        Migrations.migrate()
    }
}