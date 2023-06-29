# kose kata

[Invite link](https://discord.com/api/oauth2/authorize?client_id=1079265734456250439&permissions=0&scope=bot%20applications.commands)
[Discord server](https://discord.gg/2YWKMAJT4P)

## What is *kose kata*?

*kose kata* fills the niche of providing retrievable messages (aka notes, quotes, or tags) that can be viewed or posted.
This is useful for creating reusable answers to common questions, creating resources, making fun quotes or jokes, and
much more.

## What does *kose kata* do differently?

kose kata's only functionality is managing notes, but it does it very well.

- Its main selling point is note references. Note references are a special syntax you can use to create traversal
  between notes. They're created by putting `{{braces}}` around the note name. When a note is viewed and there are note
  references in that note, it will create a button that you can press to view the other note. This works recursively!
- All interactions with the bot are through slash commands (and a single context omcmand). This allows for greater
  functionality (autocomplete, modals) and no permissions required.
- Most interactions with the bot are ephemeral, meaning less clutter in chats. Viewing a note is ephemeral, posting a
  note with the /post command will send the note to chat visibly.
- There are many quality of life features, such as its ability to create notes from existing messages using a context
  command.
- Moderation features are extended beyond any other bots' capabilities.

---

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
  - [x] qbot format
- [x] Getting a list of a user's notes in a guild
- [x] Getting a list of a user's notes in general
- [x] Getting a list of a guild's notes
- [x] Clearing notes in a guild
- [x] Note aliases
- [x] Deleting multiple notes at once, with various options for that
- [x] Searching notes
- [ ] "Reserving" note names: preventing name collision with a toggle
- [x] Help command
- [ ] Audit logs and usage counts for notes
- [ ] i18n and settings

## Technical to-do list

- [ ] Markdown escaping for notes
- [x] Dropdown menus for note references
- [ ] Deduplicate code
- [ ] Case normalization for note names
- [ ] PostgreSQL support (in the far future)
- [ ] More logging

## Setup

- Make a MongoDB cluster
- Create a `.env` file:

```
TOKEN=your token
TEST_SERVER=your server ID
ENVIRONMENT=production
DB_URI=mongodb://localhost
DEVELOPER=your ID
```

- Download the main JAR from the CI artifacts
- Run `java -jar kosekata-X.Y-SNAPSHOT.jar`

Congratulations, you now have an instance of kose kata!
