/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.entities

import cafe.ferret.kosekata.database.Entity
import kotlinx.serialization.Serializable

@Serializable
data class Meta(var dbVersion: Int, override val _id: String = "meta") : Entity<String>
