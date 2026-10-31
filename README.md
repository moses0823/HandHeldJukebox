# 手持唱片機

這個插件讓玩家隨身攜帶唱片機並播放個人播放清單，不必把唱片機放在固定位置。

## 功能

- 將唱片放進個人清單，支援在遊戲中整理播放順序。
- 使用 `/jb play`、`/jb pause`、`/jb next`、`/jb previous` 控制播放；`/jb` 開啟清單。
- 安裝 PlaceholderAPI 後提供目前曲名、播放進度與曲目序號：
  - `%handheldjukebox_title%`
  - `%handheldjukebox_position%`
  - `%handheldjukebox_track%`

## 使用 VS Code 開啟

本專案使用 Gradle Kotlin DSL，`build.gradle.kts` 指定 **JDK 25**。請先安裝 64 位元 JDK 25，再安裝 VS Code 的 **Extension Pack for Java**。接著在 VS Code 選擇「檔案 → 開啟資料夾」，開啟這個專案根目錄（也就是看得到 `build.gradle.kts` 的 `HandHeldJukebox` 資料夾），不要只開啟 `src` 子資料夾。第一次開啟時，請允許 Java/Gradle 匯入並等待相依套件下載完成。

可在 VS Code 終端機確認 Java 版本：

```text
java -version
```

結果應顯示 `25`。如果 VS Code 仍然整片標紅，請先確認 Gradle 匯入沒有報錯，再執行命令面板中的 **Java: Clean Java Language Server Workspace**，重新載入視窗，並在 Gradle 面板重新整理專案。

## 建置

Windows PowerShell：

```powershell
.\gradlew.bat build
```

macOS / Linux：

```bash
./gradlew build
```

成功後的插件檔會在 `build/libs/HandHeldJukebox-1.0.0.jar`。請將 JAR 放入 Paper 伺服器的 `plugins` 資料夾。
