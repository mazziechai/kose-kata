# kose kata

A Discord bot for creating "notes", a message that is stored in the bot then can be retrieved later.

## Features

- [x] Creating a note from a message
- [x] Creating notes with a command
- [x] Viewing a note with an ephemeral message
- [x] Posting a note to chat
- [x] Editing notes
- [x] Deleting notes
- [x] Referencing other notes with buttons to view those quotes as well
- [x] Exporting notes to a JSON file
- [x] Importing notes from a JSON file
  - [x] Other bot formats
- [x] Getting a list of a user's notes in a guild
- [x] Getting a list of a user's notes in general
- [x] Getting a list of a guild's notes
- [x] Clearing notes in a guild
- [ ] Note aliases
- [ ] Chat command aliases for slash commands (specifically for qbot transition)
- [ ] Deleting multiple notes at once, with various options for that
- [ ] Searching notes
- [ ] "Reserving" note names: preventing name collision with a toggle

## Technical to-do list

- [ ] Make all note related commands subcommands of a larger note command
- [ ] Utilize MongoDB indexes for improved performance
- [ ] MongoDB fuzzy searching

## Setup

- Make a MongoDB cluster
- Create a `.env` file:

```
TOKEN=your token
TEST_SERVER=your server ID
ENVIRONMENT=(production | dev)
DB_URI=mongodb://localhost
DEVELOPER=your ID
```

- Download the main JAR from the CI artifacts
- Run `java -jar kose-X.Y-SNAPSHOT.jar`

The bot is now set up.
