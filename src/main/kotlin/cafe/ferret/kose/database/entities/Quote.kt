/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.entities

import cafe.ferret.kose.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * A quote, containing information about itself and the content of the quote.
 */
@Serializable
data class Quote(
    override val _id: Int,
    val author: Snowflake,
    val guild: Snowflake,
    val name: String,
    val content: String,
    val originalAuthor: Snowflake?
) :
    Entity<Int>
