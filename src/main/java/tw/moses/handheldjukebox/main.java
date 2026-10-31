package tw.moses.handheldjukebox;

import org.bukkit.plugin.java.JavaPlugin;

public final class main extends JavaPlugin {
	private playlist jukeboxPlaylist;

	@Override
	public void onEnable() {
		jukeboxPlaylist = new playlist(this);
		getCommand("jb").setExecutor(jukeboxPlaylist);
		getServer().getPluginManager().registerEvents(jukeboxPlaylist, this);
	}

	@Override
	public void onDisable() {
		if (jukeboxPlaylist != null) {
			jukeboxPlaylist.shutdown();
		}
	}
}
