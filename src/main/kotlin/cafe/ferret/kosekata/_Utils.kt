/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata

import cafe.ferret.kosekata.database.entities.Note
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.common.Color
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

suspend fun FollowupMessageCreateBuilder.noteEmbed(kord: Kord, note: Note, verbose: Boolean) {
    val noteUser = kord.getUser(note.author)
    val noteMember = noteUser?.asMemberOrNull(note.guild)

    embed {
        if (verbose) {
            author {
                name = if (noteMember?.effectiveName != null) {
                    "${noteMember.effectiveName} (${noteUser.username})"
                } else {
                    noteUser?.username ?: "Unknown user"
                }
                icon = noteMember?.avatar?.cdnUrl?.toUrl()
            }
        }

        title = note.name

        description = note.content

        color = Color(note._id)

        footer {
            text = buildString {
                append("#%06x ".format(note._id))

                if (verbose) {
                    append("| Created on ${formatTime(note.timeCreated)}")
                    if (note.aliases.count() > 1) {
                        append("| Aliases: ${note.aliases.drop(1)}")
                    }
                }
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
                        append("*${note.name}* | #%06x | ".format(note._id))
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