/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose

import cafe.ferret.kose.database.entities.Note
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat

fun formatTime(instant: Instant): String {
    val format = SimpleDateFormat("yyyy MMMM dd, HH:mm")
    return format.format(instant.epochSeconds * 1000L)
}

suspend fun MessageModifyBuilder.noteEmbed(kord: Kord, note: Note, guild: Snowflake) {
    val noteUser = kord.getUser(note.author)
    val noteMember = noteUser?.asMemberOrNull(guild)

    embed {
        author {
            name = if (noteMember?.nickname != null) {
                "${noteMember.nickname} (${noteUser.tag})"
            } else {
                noteUser?.tag ?: "Unknown user"
            }
            icon = noteMember?.avatar?.url
        }

        title = note.name

        description = note.content

        footer {
            text = buildString {
                append("#${note._id.toString(16)} ")
                append("| Created on ${formatTime(note.timeCreated)}")
            }
        }
    }
}