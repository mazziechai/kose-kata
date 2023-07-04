/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.string

open class ByIdArgs : Arguments() {
    val noteId by string {
        name = "note"
        description = "The note's ID"

        validate {
            failIf(translate("arguments.noteid.fail")) {
                value.toIntOrNull(16) == null
            }
        }
    }
}

class UserNotesArgs : Arguments() {
    val user by member {
        name = "user"
        description = "The user to get the notes from"
    }
}