# 可攜的唱片機

這個插件為伺服器打造了自由的音樂撥放，不用放在基地的某個角落，只要他在背包，就能撥放唱片

- **撥放清單功能**
把唱片放到唱片機裡，製作你的撥放清單
- **隨時都可以暫停**
使用/jb [play|pulse|next|previous|pre]來控制背包裡的唱片機

## PlaceholderAPI

安裝 PlaceholderAPI 後可使用以下 placeholder：

- `%handheldjukebox_title%`：目前曲名，沒有播放時為空字串
- `%handheldjukebox_position%`：目前曲目進度，格式為 `MM:SS`，沒有播放時為 `00:00`
- `%handheldjukebox_track%`：個人播放清單中的曲目序號，沒有播放時為 `0`
