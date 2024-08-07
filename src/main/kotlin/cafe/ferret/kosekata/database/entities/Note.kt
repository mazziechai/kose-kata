/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.entities

import cafe.ferret.kosekata.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A note, containing information about itself and the content of the note.
 */
@Serializable
data class Note(
    @SerialName("_id")
    override val _id: Int,
    val author: Snowflake,
    val guild: Snowflake,
    val name: String, // This is the original name
    val aliases: MutableList<String>, // while this is all the valid names
    var content: String,
    val originalAuthor: Snowflake?,
    val timeCreated: Instant
) :
    Entity<Int>
