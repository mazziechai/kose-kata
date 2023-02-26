/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.entities

import cafe.ferret.kose.database.Entity
import kotlinx.serialization.Serializable

@Serializable
data class Meta(var dbVersion: Int, override val _id: String = "meta") : Entity<String>
