package tw.moses.handheldjukebox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class playlist implements CommandExecutor, TabCompleter, Listener {
	private static final int PLAYLIST_SIZE = 54;
	private final JavaPlugin plugin;
	private final Map<String, Inventory> playlists = new HashMap<>();
	private final Map<UUID, PlaybackState> playbackStates = new HashMap<>();
	private final File playlistFile;
	private final YamlConfiguration playlistData;

	public playlist(JavaPlugin plugin) {
		this.plugin = plugin;
		playlistFile = new File(plugin.getDataFolder(), "playlists.yml");
		playlistData = YamlConfiguration.loadConfiguration(playlistFile);
	}

	public void shutdown() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			stopCurrentSound(player);
		}
		savePlaylists();
	}

	public String getCurrentTitle(UUID playerId) {
		ItemStack disc = getCurrentDisc(playbackStates.get(playerId));
		if (disc == null) {
			return "";
		}
		String[] words = disc.getType().name().replace("MUSIC_DISC_", "").toLowerCase(Locale.ROOT).split("_");
		StringBuilder title = new StringBuilder();
		for (String word : words) {
			if (title.length() > 0) {
				title.append(' ');
			}
			title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return title.toString();
	}

	public String getCurrentPosition(UUID playerId) {
		PlaybackState state = playbackStates.get(playerId);
		ItemStack disc = getCurrentDisc(state);
		if (disc == null) {
			return "00:00";
		}
		long elapsedMillis = state.paused ? state.elapsedMillis : getElapsedMillis(state);
		long durationSeconds = getDurationTicks(disc.getType()) / 20;
		long elapsedSeconds = Math.max(0, Math.min(elapsedMillis / 1000, durationSeconds));
		return String.format(Locale.ROOT, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
	}

	public int getCurrentTrackNumber(UUID playerId) {
		PlaybackState state = playbackStates.get(playerId);
		Inventory inventory = state == null ? null : playlists.get(state.playlistKey);
		if (inventory == null || !isDiscAt(inventory, state.currentSlot)) {
			return 0;
		}
		int trackNumber = 0;
		for (int slot = 0; slot <= state.currentSlot; slot++) {
			if (isDiscAt(inventory, slot)) {
				trackNumber++;
			}
		}
		return trackNumber;
	}

	private ItemStack getCurrentDisc(PlaybackState state) {
		if (state == null) {
			return null;
		}
		Inventory inventory = playlists.get(state.playlistKey);
		return inventory != null && isDiscAt(inventory, state.currentSlot) ? inventory.getItem(state.currentSlot) : null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("只有玩家可以使用這個指令。");
			return true;
		}

		if (args.length == 0) {
			if (!hasJukebox(player)) {
				player.sendMessage("背包中需要有唱片機才能使用個人播放清單。");
				return true;
			}
			openPlaylist(player);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "play" -> play(player);
			case "pause" -> pause(player);
			case "next" -> skip(player, 1);
			case "previous", "prev" -> skip(player, -1);
			case "volume" -> setVolume(player, args);
			default -> player.sendMessage("用法：/jb [play|pause|next|previous|prev|volume [0-100]]");
		}
		return true;
	}

	@Override
	public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 2 && args[0].equalsIgnoreCase("volume")) {
			String prefix = args[1];
			return java.util.List.of("0", "25", "50", "75", "100").stream()
					.filter(option -> option.startsWith(prefix)).toList();
		}
		if (args.length == 1) {
			String prefix = args[0].toLowerCase();
			return java.util.List.of("play", "pause", "next", "previous", "prev", "volume").stream()
					.filter(option -> option.startsWith(prefix)).toList();
		}
		return java.util.Collections.emptyList();
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		Inventory top = event.getView().getTopInventory();
		if (isPlaylist(player, top)) {
			int rawSlot = event.getRawSlot();
			if (rawSlot < 0) {
				return;
			}

			if (rawSlot >= top.getSize()) {
				ItemStack clicked = event.getCurrentItem();
				if (event.isShiftClick() && isMusicDisc(clicked)) {
					event.setCancelled(true);
					addToPlaylist(top, clicked);
					savePlaylists();
				}
				return;
			}

			event.setCancelled(true);
			if (event.getClick().isKeyboardClick() || event.getClick().isCreativeAction()) {
				return;
			}
			moveDisc(event, top, rawSlot);
			savePlaylists();
			return;
		}

		if (event.isShiftClick() && event.getClick().isLeftClick()
				&& event.getCurrentItem() != null
				&& event.getCurrentItem().getType() == Material.JUKEBOX) {
			event.setCancelled(true);
			Bukkit.getScheduler().runTask(plugin, () -> openPlaylist(player));
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		Inventory top = event.getView().getTopInventory();
		if (!isPlaylist(player, top)) {
			return;
		}

		boolean touchesPlaylist = event.getRawSlots().stream().anyMatch(slot -> slot < top.getSize());
		if (touchesPlaylist && !isMusicDisc(event.getOldCursor())) {
			event.setCancelled(true);
		} else if (touchesPlaylist) {
			Bukkit.getScheduler().runTask(plugin, this::savePlaylists);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		PlaybackState state = playbackStates.get(event.getPlayer().getUniqueId());
		if (state != null && !state.paused) {
			stopCurrentSound(event.getPlayer());
			state.elapsedMillis = getElapsedMillis(state);
			state.paused = true;
		}
	}

	private void openPlaylist(Player player) {
		String key = playerKey(player.getUniqueId());
		Inventory inventory = getPlaylist(key);
		player.openInventory(inventory);
	}

	private boolean isPlaylist(Player player, Inventory inventory) {
		return inventory != null && playlists.get(playerKey(player.getUniqueId())) == inventory;
	}

	private Inventory getPlaylist(String key) {
		return playlists.computeIfAbsent(key, playlistKey -> {
			Inventory inventory = Bukkit.createInventory(null, PLAYLIST_SIZE, "Handheld Jukebox Playlist");
			ConfigurationSection items = playlistData.getConfigurationSection("playlists." + playlistKey + ".items");
			if (items != null) {
				for (String slotName : items.getKeys(false)) {
					try {
						int slot = Integer.parseInt(slotName);
						if (slot >= 0 && slot < PLAYLIST_SIZE) {
							inventory.setItem(slot, items.getItemStack(slotName));
						}
					} catch (NumberFormatException ignored) {
						plugin.getLogger().warning("忽略無效的播放清單欄位：" + slotName);
					}
				}
			}
			return inventory;
		});
	}

	private String playerKey(UUID playerId) {
		return "player_" + playerId;
	}

	private boolean hasJukebox(Player player) {
		return player.getInventory().contains(Material.JUKEBOX);
	}

	private String playlistForCommand(Player player) {
		String key = playerKey(player.getUniqueId());
		if (!hasJukebox(player)) {
			player.sendMessage("背包中需要有唱片機才能播放個人播放清單。");
			return null;
		}
		getPlaylist(key);
		return key;
	}

	private void savePlaylists() {
		for (Map.Entry<String, Inventory> entry : playlists.entrySet()) {
			String path = "playlists." + entry.getKey();
			playlistData.set(path + ".items", null);
			Inventory inventory = entry.getValue();
			for (int slot = 0; slot < inventory.getSize(); slot++) {
				ItemStack item = inventory.getItem(slot);
				if (isMusicDisc(item)) {
					playlistData.set(path + ".items." + slot, item);
				}
			}
		}
		try {
			File parent = playlistFile.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			playlistData.save(playlistFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("無法儲存播放清單：" + exception.getMessage());
		}
	}

	private void moveDisc(InventoryClickEvent event, Inventory inventory, int slot) {
		ItemStack current = inventory.getItem(slot);
		ItemStack cursor = event.getCursor();
		boolean hasCursor = cursor != null && !cursor.getType().isAir();
		boolean hasCurrent = current != null && !current.getType().isAir();

		if (hasCursor) {
			if (!isMusicDisc(cursor)) {
				return;
			}
			int amountToMove = event.getClick() == ClickType.RIGHT ? 1 : cursor.getAmount();
			if (!hasCurrent) {
				ItemStack placed = cursor.clone();
				placed.setAmount(amountToMove);
				inventory.setItem(slot, placed);
				if (amountToMove == cursor.getAmount()) {
					event.setCursor(null);
				} else {
					cursor.setAmount(cursor.getAmount() - amountToMove);
					event.setCursor(cursor);
				}
				return;
			}
			if (current.isSimilar(cursor)) {
				int space = current.getMaxStackSize() - current.getAmount();
				int moved = Math.min(space, amountToMove);
				current.setAmount(current.getAmount() + moved);
				inventory.setItem(slot, current);
				if (moved == cursor.getAmount()) {
					event.setCursor(null);
				} else if (moved > 0) {
					cursor.setAmount(cursor.getAmount() - moved);
					event.setCursor(cursor);
				}
			}
			return;
		}

		if (!hasCurrent || !isMusicDisc(current)) {
			return;
		}
		if (event.getClick() == ClickType.RIGHT) {
			int pickedUp = (current.getAmount() + 1) / 2;
			ItemStack pickedUpStack = current.clone();
			pickedUpStack.setAmount(pickedUp);
			current.setAmount(current.getAmount() - pickedUp);
			inventory.setItem(slot, current.getAmount() == 0 ? null : current);
			event.setCursor(pickedUpStack);
		} else {
			inventory.setItem(slot, null);
			event.setCursor(current);
		}
	}

	private void addToPlaylist(Inventory inventory, ItemStack disc) {
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			ItemStack existing = inventory.getItem(slot);
			if (existing != null && existing.isSimilar(disc)
					&& existing.getAmount() < existing.getMaxStackSize()) {
				int moved = Math.min(disc.getAmount(), existing.getMaxStackSize() - existing.getAmount());
				existing.setAmount(existing.getAmount() + moved);
				disc.setAmount(disc.getAmount() - moved);
				if (disc.getAmount() == 0) {
					return;
				}
			}
		}
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (inventory.getItem(slot) == null) {
				int moved = Math.min(disc.getAmount(), disc.getMaxStackSize());
				ItemStack stack = disc.clone();
				stack.setAmount(moved);
				inventory.setItem(slot, stack);
				disc.setAmount(disc.getAmount() - moved);
				if (disc.getAmount() == 0) {
					return;
				}
			}
		}
	}

	private boolean isMusicDisc(ItemStack item) {
		return item != null && item.getType().name().startsWith("MUSIC_DISC_");
	}

	private void setVolume(Player player, String[] args) {
		if (args.length == 1) {
			player.sendMessage("目前音量：" + getVolume(player.getUniqueId()) + "%。");
			return;
		}
		if (args.length != 2) {
			player.sendMessage("用法：/jb volume <0-100>");
			return;
		}

		int volume;
		try {
			volume = Integer.parseInt(args[1]);
		} catch (NumberFormatException exception) {
			player.sendMessage("音量必須是 0 到 100 之間的整數。");
			return;
		}
		if (volume < 0 || volume > 100) {
			player.sendMessage("音量必須是 0 到 100 之間的整數。");
			return;
		}

		playlistData.set("volumes." + player.getUniqueId(), volume);
		savePlaylists();
		player.sendMessage("音量已設為 " + volume + "%。");

		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state != null && !state.paused && isDiscAt(state.playlistKey, state.currentSlot)) {
			playSlot(player, state.playlistKey, state.currentSlot);
		}
	}

	private int getVolume(UUID playerId) {
		return playlistData.getInt("volumes." + playerId, 100);
	}

	private void play(Player player) {
		String key = playlistForCommand(player);
		if (key == null) {
			return;
		}
		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state != null && state.paused && key.equals(state.playlistKey) && isDiscAt(key, state.currentSlot)) {
			playSlot(player, key, state.currentSlot);
			return;
		}
		playSlot(player, key, findNextSlot(key, -1, 1));
	}

	private void pause(Player player) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state == null || state.paused) {
			player.sendMessage("目前沒有正在播放的唱片。");
			return;
		}
		stopCurrentSound(player);
		state.elapsedMillis = getElapsedMillis(state);
		state.paused = true;
		player.sendMessage("已暫停。使用 /jb play 從目前曲目繼續。");
	}

	private void skip(Player player, int direction) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		String key = state != null && !state.paused ? state.playlistKey : playlistForCommand(player);
		if (key == null) {
			return;
		}
		if (key.startsWith("player_") && !hasJukebox(player)) {
			stopCurrentSound(player);
			if (state != null) {
				state.paused = true;
			}
			player.sendMessage("背包中已沒有唱片機，個人播放清單已停止。");
			return;
		}
		int currentSlot = state != null && key.equals(state.playlistKey) ? state.currentSlot : -1;
		int nextSlot = findNextSlot(key, currentSlot, direction);
		if (nextSlot < 0) {
			player.sendMessage("歌單裡沒有唱片，請先使用 /jb 開啟歌單。");
			return;
		}
		playSlot(player, key, nextSlot);
	}

	private int findNextSlot(String key, int currentSlot, int direction) {
		Inventory inventory = playlists.get(key);
		if (inventory == null) {
			return -1;
		}
		for (int offset = 1; offset <= inventory.getSize(); offset++) {
			int slot = Math.floorMod(currentSlot + direction * offset, inventory.getSize());
			if (isDiscAt(key, slot)) {
				return slot;
			}
		}
		return -1;
	}

	private boolean isDiscAt(String key, int slot) {
		Inventory inventory = playlists.get(key);
		return inventory != null && isDiscAt(inventory, slot);
	}

	private boolean isDiscAt(Inventory inventory, int slot) {
		return slot >= 0 && slot < inventory.getSize() && isMusicDisc(inventory.getItem(slot));
	}

	private void playSlot(Player player, String key, int slot) {
		if (!isDiscAt(key, slot)) {
			player.sendMessage("歌單裡沒有唱片，請先使用 /jb 開啟歌單。");
			return;
		}
		stopCurrentSound(player);
		ItemStack disc = playlists.get(key).getItem(slot);
		Sound sound = Sound.valueOf(disc.getType().name());
		PlaybackState state = playbackStates.computeIfAbsent(player.getUniqueId(), ignored -> new PlaybackState());
		state.playlistKey = key;
		state.currentSlot = slot;
		state.paused = false;
		state.sound = sound;
		state.startedAtMillis = System.currentTimeMillis();
		state.elapsedMillis = 0;
		player.playSound(player, sound, SoundCategory.RECORDS,
				getVolume(player.getUniqueId()) / 100.0f, 1.0f);
		state.advanceTask = Bukkit.getScheduler().runTaskLater(plugin,
				() -> skip(player, 1), getDurationTicks(disc.getType()));
		player.sendMessage("正在播放：" + disc.getType().name().replace("MUSIC_DISC_", ""));
	}

	private long getDurationTicks(Material disc) {
		int seconds = switch (disc) {
			case MUSIC_DISC_11 -> 71;
			case MUSIC_DISC_CREATOR_MUSIC_BOX -> 73;
			case MUSIC_DISC_MELLOHI -> 96;
			case MUSIC_DISC_STAL -> 150;
			case MUSIC_DISC_PIGSTEP -> 149;
			case MUSIC_DISC_FAR -> 174;
			case MUSIC_DISC_CREATOR, MUSIC_DISC_5, MUSIC_DISC_13 -> 178;
			case MUSIC_DISC_CHIRP, MUSIC_DISC_CAT -> 185;
			case MUSIC_DISC_STRAD -> 188;
			case MUSIC_DISC_MALL -> 197;
			case MUSIC_DISC_RELIC, MUSIC_DISC_OTHERSIDE -> 218;
			case MUSIC_DISC_WAIT -> 238;
			case MUSIC_DISC_WARD -> 251;
			case MUSIC_DISC_PRECIPICE -> 299;
			case MUSIC_DISC_BLOCKS -> 345;
			default -> 180;
		};
		return seconds * 20L;
	}

	private void stopCurrentSound(Player player) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state == null) {
			return;
		}
		if (state.advanceTask != null) {
			state.advanceTask.cancel();
			state.advanceTask = null;
		}
		if (state.sound != null) {
			player.stopSound(state.sound, SoundCategory.RECORDS);
		}
	}

	private long getElapsedMillis(PlaybackState state) {
		return Math.max(0, System.currentTimeMillis() - state.startedAtMillis);
	}

	private static final class PlaybackState {
		private String playlistKey;
		private int currentSlot = -1;
		private boolean paused;
		private Sound sound;
		private BukkitTask advanceTask;
		private long startedAtMillis;
		private long elapsedMillis;
	}
}
