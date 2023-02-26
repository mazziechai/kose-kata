/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database

/**
 * An abstracted MongoDB collection.
 *
 * @param name The name of the collection.
 */
abstract class DbCollection(val name: String)