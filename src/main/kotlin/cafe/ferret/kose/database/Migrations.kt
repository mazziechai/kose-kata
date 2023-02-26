/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database

import cafe.ferret.kose.database.collections.MetaCollection
import cafe.ferret.kose.database.entities.Meta
import cafe.ferret.kose.database.migrations.v1
import cafe.ferret.kose.database.migrations.v2
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import mu.KotlinLogging
import org.koin.core.component.inject

object Migrations : KordExKoinComponent {
    private val logger = KotlinLogging.logger { }

    val database: Database by inject()
    val metaCollection: MetaCollection by inject()

    suspend fun migrate() {
        var meta = metaCollection.get()

        if (meta == null) {
            meta = Meta(0)

            metaCollection.set(meta)
        }

        var currentVersion = meta.dbVersion

        logger.info { "Current database version: v${currentVersion}" }

        while (true) {
            val nextVersion = currentVersion + 1

            try {
                when (nextVersion) {
                    1 -> ::v1
                    2 -> ::v2

                    else -> break
                }(database.mongo)

                logger.info { "Migrated to database version v$nextVersion" }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to migrate database to v$nextVersion!" }

                throw t
            }

            currentVersion = nextVersion
        }

        if (currentVersion != meta.dbVersion) {
            meta = meta.copy(dbVersion = currentVersion)

            metaCollection.set(meta)

            logger.info { "Finished database migrations." }
        }
    }
}