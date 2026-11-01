# 手持唱片機 (HandHeldJukebox)

**隨身攜帶音樂，隨時隨地享受你的音樂！**

HandHeldJukebox 是一款 Minecraft 插件，讓玩家能夠建立自己的個人播放清單，隨時播放音樂唱片，不必將唱片機放置在世界中的固定位置。

## 功能特色

- **個人播放清單** - 將音樂唱片加入自己的播放清單。
- **播放清單管理** - 在遊戲中整理及調整唱片的播放順序。
- **播放控制** - 支援播放、暫停、下一首及上一首。
- **音量調整** - 自由調整音樂播放音量。
- **隨身播放** - 不需要放置實體唱片機，隨時隨地享受音樂。
- **PlaceholderAPI 支援** - 提供目前播放曲目、播放進度及曲目序號等資訊。

## 指令

| 指令                   | 說明                 |
| -------------------- | ------------------ |
| `/jb`                | 開啟個人播放清單           |
| `/jb play`           | 開始或繼續播放            |
| `/jb pause`          | 暫停播放               |
| `/jb next`           | 播放下一首              |
| `/jb previous`       | 播放上一首              |
| `/jb prev`           | `/jb previous` 的簡寫 |
| `/jb volume <0-100>` | 調整播放音量             |

## PlaceholderAPI

PlaceholderAPI 為選用依賴，安裝後即可使用以下變數：

| 變數                           | 說明        |
| ---------------------------- | --------- |
| `%handheldjukebox_title%`    | 目前播放的曲目名稱 |
| `%handheldjukebox_position%` | 目前播放進度    |
| `%handheldjukebox_track%`    | 目前播放的曲目序號 |

### 使用範例

```yaml
- "播放中：%handheldjukebox_title%"
- "曲目：%handheldjukebox_track%"
- "進度：%handheldjukebox_position%"
```

## 系統需求

- Minecraft 伺服器
- Paper
- Java 25 或更新版本
- PlaceholderAPI（選用）

## 安裝方式

1. 下載最新版本的 `HandHeldJukebox` 插件。
2. 將下載的 JAR 檔案放入伺服器的 `plugins` 資料夾。
3. 重新啟動伺服器。
4. 在遊戲中輸入 `/jb` 開啟個人播放清單。

## 插件資訊

|   | 項目     | 資訊                              |
| - | ------ | ------------------------------- |
|   | 插件名稱   | HandHeldJukebox                 |
|   | 中文名稱   | 手持唱片機                           |
|   | 版本     | 1.1.1                           |
|   | 主類別    | `tw.moses.handheldjukebox.main` |
|   | API 版本 | `26.3`                          |
|   | 選用依賴   | PlaceholderAPI                  |

---

**讓音樂不再受限於唱片機的位置！**


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
