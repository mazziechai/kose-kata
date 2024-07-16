/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosekata.database.collections

import cafe.ferret.kosekata.database.Database
import cafe.ferret.kosekata.database.DbCollection
import cafe.ferret.kosekata.database.entities.Note
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Aggregates.sample
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.ReplaceOptions
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.inject
import kotlin.random.Random

/**
 * MongoDB collection for [Note].
 */
class NoteCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Note>(name)

    /**
     * Creates a new [Note] and adds it to the collection.
     * This method creates a randomly generated ID for the [Note].
     *
     * @param author The author's [Snowflake].
     * @param guild The guild that this note belongs to, as a [Snowflake].
     * @param content The note's content.
     *
     * @return The created [Note].
     */
    suspend fun new(
        author: Snowflake,
        guild: Snowflake,
        name: String,
        aliases: MutableList<String>,
        content: String,
        originalAuthor: Snowflake? = null,
        timeCreated: Instant = Clock.System.now()
    ): Note {
        var id: Int

        do {
            id = Random.nextInt(0x0, 0xFFFFFF)
        } while (col.countDocuments(eq(Note::_id.name, id)) != 0L)

        val note = Note(id, author, guild, name, aliases, content, originalAuthor, timeCreated)

        set(note)

        return note
    }

    /**
     * Deletes a [Note] from the collection.
     *
     * @param note The [Note] to delete from the collection.
     */
    suspend fun delete(note: Note) = col.deleteOne(eq(Note::_id.name, note._id))

    suspend fun deleteMany(notes: List<Note>) = col.deleteMany(`in`(Note::_id.name, notes.map { it._id }))

    /**
     * Deletes all notes from a user in a guild.
     */
    suspend fun deleteByUserInGuild(user: Snowflake, guild: Snowflake) =
        col.deleteMany(and(eq(Note::author.name, user), eq(Note::guild.name, guild)))

    /**
     * Deletes all notes from a guild.
     *
     * @param guild The guild to delete all notes of.
     */
    suspend fun deleteAllGuild(guild: Snowflake) = col.deleteMany(eq(Note::guild.name, guild))

    /**
     * Gets a [Note] from its ID.
     *
     * @param id The ID of the note.
     * @return The [Note], if any.
     */
    suspend fun get(id: Int) = col.find(eq(Note::_id.name, id)).firstOrNull()

    /**
     * Saves a [Note] to the collection.
     *
     * @param note The [Note] to save to the collection.
     */
    suspend fun set(note: Note) = col.replaceOne(eq(Note::_id.name, note._id), note, ReplaceOptions().upsert(true))

    /**
     * Gets [Note]s from a user's [Snowflake].
     *
     * @param user The user's snowflake.
     * @return The [Note]s, if any.
     */
    suspend fun getByUser(user: Snowflake) = col.find(eq(Note::author.name, user)).toList()

    suspend fun getByGuildAndUser(guild: Snowflake, user: Snowflake) =
        col.find(and(eq(Note::guild.name, guild), eq(Note::author.name, user))).toList()

    /**
     * Gets [Note]s from a guild's [Snowflake].
     *
     * @param guild The guild's snowflake.
     * @return The [Note]s, if any.
     */
    suspend fun getByGuild(guild: Snowflake) = col.find(eq(Note::guild.name, guild)).toList()

    /**
     * Gets [Note]s from a guild's [Snowflake] and a name of the note.
     *
     * @param guild The guild's snowflake.
     * @param name The name of the note.
     * @return The [Note]s, if any.
     */
    suspend fun getByGuildAndName(guild: Snowflake, name: String) =
        col.find(and(eq(Note::guild.name, guild), `in`(Note::aliases.name, name))).toList()

    suspend fun getRandomNote(guild: Snowflake, name: String) =
        col.aggregate<Note>(
            listOf(
                match(
                    and(
                        eq(Note::guild.name, guild),
                        `in`(Note::aliases.name, name)
                    ),
                ), sample(1)
            )
        ).firstOrNull()

    /**
     * Gets multiple Notes by ID.
     */
    suspend fun getMultipleNotes(ids: List<Int>) = col.find(`in`(Note::_id.name, ids)).toList()

    fun rawCollectionAccess() = col

    companion object : DbCollection("notes")
}