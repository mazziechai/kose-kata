/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database

import cafe.ferret.kose.database.collections.GuildDataCollection
import cafe.ferret.kose.database.collections.MetaCollection
import cafe.ferret.kose.database.collections.NoteCollection
import cafe.ferret.kose.database.collections.UserCollection
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
                single { GuildDataCollection() } bind GuildDataCollection::class
                single { NoteCollection() } bind NoteCollection::class
                single { UserCollection() } bind UserCollection::class
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