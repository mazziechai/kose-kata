/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database

import cafe.ferret.kosekata.database.collections.MetaCollection
import cafe.ferret.kosekata.database.collections.NoteCollection
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind

/**
 * Sets up the database and singletons for relevant classes.
 */
suspend fun ExtensibleBotBuilder.database(migrate: Boolean) {
    val uri = env("DB_URI")
    val database = Database(uri)

    hooks {
        beforeKoinSetup {
            loadModule {
                single { database } bind Database::class
            }

            loadModule {
                single { NoteCollection() } bind NoteCollection::class
                single { MetaCollection() } bind MetaCollection::class
            }

            if (migrate) {
                runBlocking {
                    database.migrate()
                }
            }
        }
    }
}