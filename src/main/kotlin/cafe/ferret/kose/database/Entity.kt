/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database

/**
 * A KMongo entity.
 *
 * @property _id The ID type used in MongoDB.
 */
interface Entity<ID> {
    @Suppress("PropertyName")
    val _id: ID
}