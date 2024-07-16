/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.entities

import cafe.ferret.kosekata.database.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    var dbVersion: Int,
    @SerialName("_id")
    override val _id: String = "meta"
) : Entity<String>
