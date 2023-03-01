/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string

class ByIdArgs : Arguments() {
    val noteId by string {
        name = "note"
        description = "The note's ID"

        validate {
            failIf("That's not a valid ID!") {
                value.toIntOrNull(16) == null
            }
        }
    }
}