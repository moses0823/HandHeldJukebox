package tw.moses.handheldjukebox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class playlist implements CommandExecutor, Listener {
	private static final int PLAYLIST_SIZE = 54;
	private final JavaPlugin plugin;
	private final Map<UUID, Inventory> playlists = new HashMap<>();
	private final Map<UUID, PlaybackState> playbackStates = new HashMap<>();

	public playlist(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void shutdown() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			stopCurrentSound(player);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("只有玩家可以使用這個指令。");
			return true;
		}

		if (args.length == 0) {
			openPlaylist(player);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "play" -> play(player);
			case "pulse", "pause" -> pause(player);
			case "next" -> skip(player, 1);
			case "previous", "prev", "pre" -> skip(player, -1);
			default -> player.sendMessage("用法：/jb [play|pulse|next|previous|pre]");
		}
		return true;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.isSneaking() || event.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock != null && clickedBlock.getType() == Material.JUKEBOX) {
			event.setCancelled(true);
			openPlaylist(player);
		}
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
				}
				return;
			}

			event.setCancelled(true);
			if (event.getClick().isKeyboardClick() || event.getClick().isCreativeAction()) {
				return;
			}
			moveDisc(event, top, rawSlot);
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
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		PlaybackState state = playbackStates.get(event.getPlayer().getUniqueId());
		if (state != null && !state.paused) {
			stopCurrentSound(event.getPlayer());
			state.paused = true;
		}
	}

	private void openPlaylist(Player player) {
		Inventory inventory = playlists.computeIfAbsent(player.getUniqueId(),
				ignored -> Bukkit.createInventory(null, PLAYLIST_SIZE, "Handheld Jukebox Playlist"));
		player.openInventory(inventory);
	}

	private boolean isPlaylist(Player player, Inventory inventory) {
		return playlists.get(player.getUniqueId()) == inventory;
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

	private void play(Player player) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state != null && state.paused && isDiscAt(player, state.currentSlot)) {
			playSlot(player, state.currentSlot);
			return;
		}
		playSlot(player, findNextSlot(player, -1, 1));
	}

	private void pause(Player player) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		if (state == null || state.paused) {
			player.sendMessage("目前沒有正在播放的唱片。");
			return;
		}
		stopCurrentSound(player);
		state.paused = true;
		player.sendMessage("已暫停。使用 /jb play 從目前曲目繼續。");
	}

	private void skip(Player player, int direction) {
		PlaybackState state = playbackStates.get(player.getUniqueId());
		int currentSlot = state == null ? -1 : state.currentSlot;
		int nextSlot = findNextSlot(player, currentSlot, direction);
		if (nextSlot < 0) {
			player.sendMessage("歌單裡沒有唱片，請先使用 /jb 開啟歌單。");
			return;
		}
		playSlot(player, nextSlot);
	}

	private int findNextSlot(Player player, int currentSlot, int direction) {
		Inventory inventory = playlists.get(player.getUniqueId());
		if (inventory == null) {
			return -1;
		}
		for (int offset = 1; offset <= inventory.getSize(); offset++) {
			int slot = Math.floorMod(currentSlot + direction * offset, inventory.getSize());
			if (isDiscAt(inventory, slot)) {
				return slot;
			}
		}
		return -1;
	}

	private boolean isDiscAt(Player player, int slot) {
		Inventory inventory = playlists.get(player.getUniqueId());
		return inventory != null && isDiscAt(inventory, slot);
	}

	private boolean isDiscAt(Inventory inventory, int slot) {
		return slot >= 0 && slot < inventory.getSize() && isMusicDisc(inventory.getItem(slot));
	}

	private void playSlot(Player player, int slot) {
		if (!isDiscAt(player, slot)) {
			player.sendMessage("歌單裡沒有唱片，請先使用 /jb 開啟歌單。");
			return;
		}
		stopCurrentSound(player);
		ItemStack disc = playlists.get(player.getUniqueId()).getItem(slot);
		Sound sound = Sound.valueOf(disc.getType().name());
		PlaybackState state = playbackStates.computeIfAbsent(player.getUniqueId(), ignored -> new PlaybackState());
		state.currentSlot = slot;
		state.paused = false;
		state.sound = sound;
		player.playSound(player, sound, SoundCategory.RECORDS, 1.0f, 1.0f);
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

	private static final class PlaybackState {
		private int currentSlot = -1;
		private boolean paused;
		private Sound sound;
		private BukkitTask advanceTask;
	}
}
