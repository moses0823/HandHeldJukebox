package tw.moses.handheldjukebox;

import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public final class HandHeldJukeboxExpansion extends PlaceholderExpansion {
	private final JavaPlugin plugin;
	private final playlist jukeboxPlaylist;

	public HandHeldJukeboxExpansion(JavaPlugin plugin, playlist jukeboxPlaylist) {
		this.plugin = plugin;
		this.jukeboxPlaylist = jukeboxPlaylist;
	}

	@Override
	public String getIdentifier() {
		return "handheldjukebox";
	}

	@Override
	public String getAuthor() {
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer player, String params) {
		if (player == null) {
			return "";
		}
		return switch (params.toLowerCase(Locale.ROOT)) {
			case "title" -> jukeboxPlaylist.getCurrentTitle(player.getUniqueId());
			case "position" -> jukeboxPlaylist.getCurrentPosition(player.getUniqueId());
			case "track" -> Integer.toString(jukeboxPlaylist.getCurrentTrackNumber(player.getUniqueId()));
			default -> null;
		};
	}
}