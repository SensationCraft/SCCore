package org.sensationcraft.sccore;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.sensationcraft.sccore.chat.commands.ShoutCommand;
import org.sensationcraft.sccore.duels.ArenaManager;
import org.sensationcraft.sccore.duels.DuelListeners;
import org.sensationcraft.sccore.duels.commands.ArenaCommand;
import org.sensationcraft.sccore.duels.commands.DuelCommand;
import org.sensationcraft.sccore.duels.commands.SpectateCommand;
import org.sensationcraft.sccore.lockpicks.LockpickListeners;
import org.sensationcraft.sccore.mysql.MySQL;
import org.sensationcraft.sccore.ranks.PermissionsManager;
import org.sensationcraft.sccore.ranks.RankManager;
import org.sensationcraft.sccore.ranks.commands.PermsCommand;
import org.sensationcraft.sccore.ranks.commands.RankCommand;
import org.sensationcraft.sccore.scplayer.SCPlayerManager;
import org.sensationcraft.sccore.stats.StatListeners;
import org.sensationcraft.sccore.stats.StatsManager;
import org.sensationcraft.sccore.utils.Utils;

import com.earth2me.essentials.Essentials;

/**
 * Created by Anml on 12/26/15.
 */

public class SCCore extends JavaPlugin implements Listener {

	public static SCCore instance;
	private Essentials essentials;
	private SCPlayerManager scPlayerManager;
	private ArenaManager arenaManager;
	private RankManager rankManager;
	private StatsManager statsManager;
	private PermissionsManager permissionsManager;
	private Utils utils;
	private MySQL mySQL;

	public static SCCore getInstance() {
		return instance;
	}

	public MySQL getMySQL() {
		return this.mySQL;
	}

	public RankManager getRankManager() {
		return this.rankManager;
	}

	public PermissionsManager getPermissionsManager() {
		return this.permissionsManager;
	}
	public SCPlayerManager getSCPlayerManager() {
		return this.scPlayerManager;
	}
	public ArenaManager getArenaManager() {
		return this.arenaManager;
	}

	public StatsManager getStatsManager() {
		return this.statsManager;
	}

	public Utils getUtils() {
		return this.utils;
	}
	public Essentials getEssentials() {
		return this.essentials;
	}

	@Override
	public void onEnable() {

		instance = this;
		this.saveDefaultConfig();

		this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		if (this.essentials == null || !this.essentials.isEnabled()) {
			this.getLogger().info("Essentials was not found on the server, resulting in the server shutting down.");
			this.getServer().shutdown();
		}

		this.registerManagers();
		this.registerEvents();
		this.registerCommands();

		this.scPlayerManager.loadSCPlayers();

		this.getLogger().info("[SCCore] Plugin has been enabled.");
	}

	@Override
	public void onDisable() {

		instance = null;

		this.getLogger().info("[SCCore] Plugin has been disabled.");

	}

	public void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		pm.registerEvents(this.scPlayerManager, this);
		pm.registerEvents(this, this);
		pm.registerEvents(new LockpickListeners(this), this);
		pm.registerEvents(new DuelListeners(this), this);
		pm.registerEvents(new StatListeners(this), this);
	}

	public void registerCommands() {
		this.getCommand("rank").setExecutor(new RankCommand(this));
		this.getCommand("shout").setExecutor(new ShoutCommand(this));
		this.getCommand("perms").setExecutor(new PermsCommand(this));
		this.getCommand("arena").setExecutor(new ArenaCommand(this));
		this.getCommand("duel").setExecutor(new DuelCommand(this));
		this.getCommand("spectate").setExecutor(new SpectateCommand(this));

	}

	public void registerManagers() {
		this.mySQL = new MySQL(this);
		this.scPlayerManager = new SCPlayerManager(this);
		this.arenaManager = new ArenaManager(this);
		this.rankManager = new RankManager(this);
		this.permissionsManager = new PermissionsManager(this);
		this.statsManager = new StatsManager(this);
		this.utils = new Utils();
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		event.setMotd("                §c§lSensation§4§lCraft");
	}
}