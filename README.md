# HandHeldJukebox

**Carry your music with you!**

HandHeldJukebox is a Minecraft plugin that allows players to carry their own jukebox and play music discs through a personal playlist, without needing to place a jukebox in the world.

## Features

- **Personal Playlists** - Add music discs to your own playlist.
- **Playlist Management** - Organize and manage the playback order of your music discs.
- **Playback Controls** - Play, pause, skip to the next track, or return to the previous track.
- **Volume Control** - Adjust your playback volume.
- **Portable Music** - Enjoy your music anywhere without placing a physical jukebox.
- **PlaceholderAPI Support** - Display current track information and playback progress in other plugins.

## Commands

| Command | Description |
|---|---|
| `/jb` | Open your personal playlist |
| `/jb play` | Start or resume playback |
| `/jb pause` | Pause playback |
| `/jb next` | Play the next track |
| `/jb previous` | Play the previous track |
| `/jb prev` | Alias for `/jb previous` |
| `/jb volume <0-100>` | Adjust playback volume |

## PlaceholderAPI

PlaceholderAPI is an optional dependency.

When installed, HandHeldJukebox provides the following placeholders:

| Placeholder | Description |
|---|---|
| `%handheldjukebox_title%` | Current track title |
| `%handheldjukebox_position%` | Current playback progress |
| `%handheldjukebox_track%` | Current track number |

Example:

```yaml
- "Now Playing: %handheldjukebox_title%"
- "Track: %handheldjukebox_track%"
- "Progress: %handheldjukebox_position%"
```

## Requirements

- Minecraft Server
- Paper
- Java 25+
- PlaceholderAPI (Optional)

## Installation

1. Download the latest `HandHeldJukebox` JAR file.
2. Place it in your server's `plugins` folder.
3. Restart your server.
4. Use `/jb` to open your personal playlist.

## Plugin Information

| Property | Value |
|---|---|
| Name | HandHeldJukebox |
| Version | 1.1.1 |
| Main Class | `tw.moses.handheldjukebox.main` |
| API Version | `26.3` |
| Soft Dependencies | PlaceholderAPI |

---

**Enjoy your music, wherever you go!**
# HandHeldJukebox

**Carry your music with you!**

HandHeldJukebox is a Minecraft plugin that allows players to carry their own jukebox and play music discs through a personal playlist, without needing to place a jukebox in the world.

## Features

- **Personal Playlists** - Add music discs to your own playlist.
- **Playlist Management** - Organize and manage the playback order of your music discs.
- **Playback Controls** - Play, pause, skip to the next track, or return to the previous track.
- **Volume Control** - Adjust your playback volume.
- **Portable Music** - Enjoy your music anywhere without placing a physical jukebox.
- **PlaceholderAPI Support** - Display current track information and playback progress in other plugins.

## Commands

| Command | Description |
|---|---|
| `/jb` | Open your personal playlist |
| `/jb play` | Start or resume playback |
| `/jb pause` | Pause playback |
| `/jb next` | Play the next track |
| `/jb previous` | Play the previous track |
| `/jb prev` | Alias for `/jb previous` |
| `/jb volume <0-100>` | Adjust playback volume |

## PlaceholderAPI

PlaceholderAPI is an optional dependency.

When installed, HandHeldJukebox provides the following placeholders:

| Placeholder | Description |
|---|---|
| `%handheldjukebox_title%` | Current track title |
| `%handheldjukebox_position%` | Current playback progress |
| `%handheldjukebox_track%` | Current track number |

Example:

```yaml
- "Now Playing: %handheldjukebox_title%"
- "Track: %handheldjukebox_track%"
- "Progress: %handheldjukebox_position%"
```

## Requirements

- Minecraft Server
- Paper
- Java 25+
- PlaceholderAPI (Optional)

## Installation

1. Download the latest `HandHeldJukebox` JAR file.
2. Place it in your server's `plugins` folder.
3. Restart your server.
4. Use `/jb` to open your personal playlist.

## Plugin Information

| Property | Value |
|---|---|
| Name | HandHeldJukebox |
| Version | 1.1.1 |
| Main Class | `tw.moses.handheldjukebox.main` |
| API Version | `26.3` |
| Soft Dependencies | PlaceholderAPI |

---

**Enjoy your music, wherever you go!**