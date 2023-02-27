/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.database.collections

import cafe.ferret.kose.database.Database
import cafe.ferret.kose.database.DbCollection
import cafe.ferret.kose.database.entities.BotUser
import cafe.ferret.kose.database.entities.Note
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.inject
import org.litote.kmongo.and
import org.litote.kmongo.eq
import kotlin.random.Random

/**
 * MongoDB collection for [Note].
 */
class NoteCollection : KordExKoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Note>(name)

    private val userCollection: UserCollection by inject()
    private val guildDataCollection: GuildDataCollection by inject()

    /**
     * Creates a new [Note] and adds it to the collection, updating the other collections' data as well.
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
        content: String,
        originalAuthor: Snowflake? = null,
        timeCreated: Instant = Clock.System.now()
    ): Note {
        var id: Int

        do {
            id = Random.nextInt(0x0, 0xFFFFFF)
        } while (col.countDocuments(Note::_id eq id) != 0L)

        val note = Note(id, author, guild, name, content, originalAuthor, timeCreated)

        set(note)

        val botUser = userCollection.get(author)

        if (botUser == null) {
            userCollection.new(author, mutableListOf(note._id))
        } else {
            botUser.notes.add(note._id)
            userCollection.set(botUser)
        }

        val guildData = guildDataCollection.get(guild)

        if (guildData == null) {
            guildDataCollection.new(guild, mutableListOf(note._id))
        } else {
            guildData.notes.add(note._id)
            guildDataCollection.set(guildData)
        }

        return note
    }

    /**
     * Deletes a [Note] from the collection, updating the other collections' data as well.
     *
     * @param note The [Note] to delete from the collection.
     */
    suspend fun delete(note: Note) {
        val guildData = guildDataCollection.get(note.guild)

        if (guildData != null) {
            guildData.notes.remove(note._id)
            guildDataCollection.set(guildData)
        }

        val botUser = userCollection.get(note.author)

        if (botUser != null) {
            botUser.notes.remove(note._id)
            userCollection.set(botUser)
        }

        col.deleteOne(Note::_id eq note._id)
    }

    /**
     * Deletes all notes from a guild, updating the other collections' data as well.
     *
     * @param guild The guild to delete all notes of.
     */
    suspend fun deleteAllGuild(guild: Snowflake) {
        val notes = getByGuild(guild)

        val guildData = guildDataCollection.get(guild)

        if (guildData != null) {
            guildData.notes.clear()
            guildDataCollection.set(guildData)
        }

        val users = mutableListOf<BotUser>()

        for (note in notes) {
            if (users.any { it._id == note.author }) {
                val user = userCollection.get(note.author)

                if (user != null) {
                    users.add(user)
                }
            }
        }

        users.forEach { user ->
            user.notes.clear()
            userCollection.set(user)
        }

        col.deleteMany(Note::guild eq guild)
    }

    /**
     * Gets a [Note] from its ID.
     *
     * @param id The ID of the note.
     * @return The [Note], if any.
     */
    suspend fun get(id: Int) = col.findOne(Note::_id eq id)

    /**
     * Saves a [Note] to the collection.
     *
     * @param note The [Note] to save to the collection.
     */
    suspend fun set(note: Note) = col.save(note)

    /**
     * Gets [Note]s from a user's [Snowflake].
     *
     * @param user The user's snowflake.
     * @return The [Note]s, if any.
     */
    suspend fun getByUser(user: Snowflake) = col.find(Note::author eq user).toList()

    /**
     * Gets [Note]s from a guild's [Snowflake].
     *
     * @param guild The guild's snowflake.
     * @return The [Note]s, if any.
     */
    suspend fun getByGuild(guild: Snowflake) = col.find(Note::guild eq guild).toList()

    /**
     * Gets [Note]s from a guild's [Snowflake] and a name of the note.
     *
     * @param guild The guild's snowflake.
     * @param name The name of the note.
     * @return The [Note]s, if any.
     */
    suspend fun getByGuildAndName(guild: Snowflake, name: String) =
        col.find(and(Note::guild eq guild, Note::name eq name)).toList()

    companion object : DbCollection("notes")
}