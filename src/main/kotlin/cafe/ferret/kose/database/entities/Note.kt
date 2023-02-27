/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.entities

import cafe.ferret.kose.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * A note, containing information about itself and the content of the note.
 */
@Serializable
data class Note(
    override val _id: Int,
    val author: Snowflake,
    val guild: Snowflake,
    val name: String,
    var content: String,
    val originalAuthor: Snowflake?,
    val timeCreated: Instant
) :
    Entity<Int>
