/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database

/**
 * A KMongo entity.
 *
 * @property _id The ID type used in MongoDB.
 */
interface Entity<ID> {
    @Suppress("PropertyName")
    val _id: ID
}