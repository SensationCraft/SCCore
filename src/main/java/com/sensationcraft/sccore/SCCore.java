package com.sensationcraft.sccore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.sensationcraft.sccore.chat.commands.ShoutCommand;
import com.sensationcraft.sccore.chat.commands.StaffCommand;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.duels.DuelListeners;
import com.sensationcraft.sccore.duels.commands.ArenaCommand;
import com.sensationcraft.sccore.duels.commands.DuelCommand;
import com.sensationcraft.sccore.duels.commands.SpectateCommand;
import com.sensationcraft.sccore.helprequests.HelpAccept;
import com.sensationcraft.sccore.helprequests.HelpCancel;
import com.sensationcraft.sccore.helprequests.HelpDeny;
import com.sensationcraft.sccore.helprequests.HelpList;
import com.sensationcraft.sccore.helprequests.HelpRead;
import com.sensationcraft.sccore.helprequests.HelpRequest;
import com.sensationcraft.sccore.lockpicks.LockpickListeners;
import com.sensationcraft.sccore.mysql.MySQL;
import com.sensationcraft.sccore.punishments.PunishmentListeners;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.commands.BanCommand;
import com.sensationcraft.sccore.punishments.commands.KickCommand;
import com.sensationcraft.sccore.punishments.commands.MuteCommand;
import com.sensationcraft.sccore.punishments.commands.TempbanCommand;
import com.sensationcraft.sccore.punishments.commands.TempmuteCommand;
import com.sensationcraft.sccore.punishments.commands.UnbanCommand;
import com.sensationcraft.sccore.punishments.commands.UnmuteCommand;
import com.sensationcraft.sccore.punishments.commands.WarnCommand;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.ranks.commands.PermsCommand;
import com.sensationcraft.sccore.ranks.commands.RankCommand;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.stats.StatListeners;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.Utils;

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
	private HelpRequest helpRequest;
	private Utils utils;
	private MySQL mySQL;
	private PunishmentManager punishmentManager;

	public static SCCore getInstance() {
		return SCCore.instance;
	}

	public MySQL getMySQL() {
		return this.mySQL;
	}

	public RankManager getRankManager() {
		return this.rankManager;
	}

	public HelpRequest getHelpRequest() {
		return this.helpRequest;
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

	public PunishmentManager getPunishmentManager() {
		return this.punishmentManager;
	}

	public Utils getUtils() {
		return this.utils;
	}

	public Essentials getEssentials() {
		return this.essentials;
	}

	@Override
	public void onEnable() {

		SCCore.instance = this;
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
		for (Player player : Bukkit.getOnlinePlayers())
			player.kickPlayer("§cSensationCraft §7is restarting. Please wait 30 seconds before re-logging.");

		SCCore.instance = null;

		this.getLogger().info("[SCCore] Plugin has been disabled.");

	}

	public void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		pm.registerEvents(this.scPlayerManager, this);
		pm.registerEvents(this, this);
		pm.registerEvents(new LockpickListeners(this), this);
		pm.registerEvents(new DuelListeners(this), this);
		pm.registerEvents(new StatListeners(this), this);
		pm.registerEvents(new PunishmentListeners(this), this);
	}

	public void registerCommands() {
		this.getCommand("shout").setExecutor(new ShoutCommand(this));
		this.getCommand("staff").setExecutor(new StaffCommand(this));
		this.getCommand("rank").setExecutor(new RankCommand(this));
		this.getCommand("perms").setExecutor(new PermsCommand(this));
		this.getCommand("arena").setExecutor(new ArenaCommand(this));
		this.getCommand("duel").setExecutor(new DuelCommand(this));
		this.getCommand("spectate").setExecutor(new SpectateCommand(this));
		this.getCommand("ban").setExecutor(new BanCommand(this));
		this.getCommand("tempban").setExecutor(new TempbanCommand(this));
		this.getCommand("mute").setExecutor(new MuteCommand(this));
		this.getCommand("tempmute").setExecutor(new TempmuteCommand(this));
		this.getCommand("unban").setExecutor(new UnbanCommand(this));
		this.getCommand("unmute").setExecutor(new UnmuteCommand(this));
		this.getCommand("kick").setExecutor(new KickCommand(this));
		this.getCommand("warn").setExecutor(new WarnCommand(this));
		this.getCommand("helpaccept").setExecutor(new HelpAccept(this.helpRequest));
		this.getCommand("helpcancel").setExecutor(new HelpCancel(this.helpRequest));
		this.getCommand("helpdeny").setExecutor(new HelpDeny(this.helpRequest));
		this.getCommand("helplist").setExecutor(new HelpList(this.helpRequest));
		this.getCommand("helpread").setExecutor(new HelpRead(this.helpRequest));
		this.getCommand("helprequest").setExecutor(new HelpRequest());

	}

	public void registerManagers() {
		this.mySQL = new MySQL(this);
		this.rankManager = new RankManager(this);
		this.permissionsManager = new PermissionsManager(this);
		this.statsManager = new StatsManager(this);
		this.punishmentManager = new PunishmentManager(this);
		this.scPlayerManager = new SCPlayerManager(this);
		this.arenaManager = new ArenaManager(this);
		this.helpRequest = new HelpRequest();
		this.utils = new Utils();
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		event.setMotd("                §c§lSensation§4§lCraft");
	}
}