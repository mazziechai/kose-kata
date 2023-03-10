/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.entities

import cafe.ferret.kosekata.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * A guild, containing the notes that have been created in it.
 */
@Serializable
data class GuildData(override val _id: Snowflake, var notes: MutableList<Int>) : Entity<Snowflake>
