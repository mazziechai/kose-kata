/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.entities

import cafe.ferret.kosekata.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * A user that has used the bot, containing their notes that they have created.
 */
@Serializable
data class BotUser(override val _id: Snowflake, val notes: MutableList<Int>) : Entity<Snowflake>
