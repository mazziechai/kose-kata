/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import cafe.ferret.kosekata.i18n.Translations
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.member
import dev.kordex.core.commands.converters.impl.string

open class ByIdArgs : Arguments() {
    val noteId by string {
        name = Translations.Arguments.Noteid.name
        description = Translations.Arguments.Noteid.description

        validate {
            failIf(Translations.Arguments.Noteid.fail) {
                value.toIntOrNull(16) == null
            }
        }
    }
}

open class ByNameArgs : Arguments() {
    val name by string {
        name = Translations.Arguments.Notename.name
        description = Translations.Arguments.Notename.description
    }
}

class UserNotesArgs : Arguments() {
    val user by member {
        name = Translations.Arguments.Usernotes.name
        description = Translations.Arguments.Usernotes.description
    }
}