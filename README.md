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

This branch is a rewrite. Most features aren't implemented yet compared to the version in production.

- [ ] Creating a note from a message
- [ ] Creating notes with a command
- [ ] Viewing a note with an ephemeral message
- [ ] Posting a note to chat
- [ ] Editing notes
- [ ] Deleting notes
- [ ] Referencing other notes with buttons to view those quotes as well
- [ ] Exporting notes to a JSON file
- [ ] Importing notes from a JSON file
  - [ ] qbot format
- [ ] Getting a list of a user's notes in a guild
- [ ] Getting a list of a user's notes in general
- [ ] Getting a list of a guild's notes
- [ ] Clearing notes in a guild
- [ ] Note aliases
- [ ] Deleting multiple notes at once, with various options for that
- [ ] Searching notes
- [ ] "Reserving" note names: preventing name collision with a toggle
- [ ] Help command
- [ ] Audit logs and usage counts for notes
- [ ] i18n and settings

## Technical to-do list

- [ ] Markdown escaping for notes
- [ ] Case normalization for note names
- [ ] More logging
- [ ] Lazy pagination to improve performance
