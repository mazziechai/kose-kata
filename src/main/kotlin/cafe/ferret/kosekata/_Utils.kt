/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import cafe.ferret.kosekata.database.entities.Note
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat

/**
 * Formats an Instant to a string.
 */
fun formatTime(instant: Instant): String {
    val format = SimpleDateFormat("yyyy MMMM dd, HH:mm")
    return format.format(instant.epochSeconds * 1000L) + " UTC"
}

/**
 * An extension to create note embeds easily and consistently.
 *
 * @param kord The bot.
 * @param note The note to display.
 */
suspend fun FollowupMessageCreateBuilder.noteEmbed(kord: Kord, note: Note) {
    val noteUser = kord.getUser(note.author)
    val noteMember = noteUser?.asMemberOrNull(note.guild)

    embed {
        author {
            name = if (noteMember?.effectiveName != null) {
                "${noteMember.effectiveName} (${noteUser.username})"
            } else {
                noteUser?.username ?: "Unknown user"
            }
            icon = noteMember?.avatar?.cdnUrl?.toUrl()
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

suspend fun EphemeralInteractionContext.guildNotes(
    kord: Kord,
    guild: Guild,
    notes: List<Note>,
    searchParam: String? = null
) {
    val cachedUsers = mutableListOf<User>()
    val unknownUsers = mutableListOf<Snowflake>()

    editingPaginator {
        timeoutSeconds = 60

        notes.chunked(10).forEach { chunkedNotes ->
            page {
                author {
                    name = guild.name
                    icon = guild.icon?.cdnUrl?.toUrl { format = Image.Format.PNG }
                }

                title = if (searchParam != null) {
                    "Notes matching `$searchParam`"
                } else {
                    "Notes"
                }

                description = buildString {
                    chunkedNotes.forEach { note ->
                        var user = cachedUsers.find { it.id == note.author }

                        if (user == null) {
                            if (note.author !in unknownUsers) {
                                user = kord.getUser(note.author)
                            }
                        }

                        if (user == null) {
                            unknownUsers.add(note.author)
                        } else {
                            cachedUsers.add(user)
                        }

                        append("${user?.username ?: "Unknown user"} → ")
                        append("*${note.name}* | #${note._id.toString(16)} | ")
                        append("Created on ${note.timeCreated.toDiscord(TimestampType.ShortDate)} ")
                        appendLine("at ${note.timeCreated.toDiscord(TimestampType.ShortTime)}")
                    }
                }

                footer {
                    text = "${notes.count()} notes"
                }
            }
        }
    }.send()
}