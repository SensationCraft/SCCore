package com.sensationcraft.sccore;

import com.earth2me.essentials.Essentials;
import com.sensationcraft.sccore.chat.ChatListener;
import com.sensationcraft.sccore.chat.commands.ChannelCommand;
import com.sensationcraft.sccore.chat.commands.StaffCommand;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.duels.DuelListeners;
import com.sensationcraft.sccore.duels.commands.ArenaCommand;
import com.sensationcraft.sccore.duels.commands.DuelCommand;
import com.sensationcraft.sccore.duels.commands.SpectateCommand;
import com.sensationcraft.sccore.helprequests.HelpRequest;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.helprequests.commands.HelpRequestCommand;
import com.sensationcraft.sccore.lockpicks.LockpickListeners;
import com.sensationcraft.sccore.mysql.MySQL;
import com.sensationcraft.sccore.punishments.PunishmentListeners;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.commands.*;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.ranks.commands.PermsCommand;
import com.sensationcraft.sccore.ranks.commands.RankCommand;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.stats.StatListeners;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Anml on 12/26/15.
 */

@Getter
public class SCCore extends JavaPlugin implements Listener {

	public static SCCore instance;
	private Essentials essentials;
	private SCPlayerManager scPlayerManager;
	private ArenaManager arenaManager;
	private RankManager rankManager;
	private StatsManager statsManager;
	private PermissionsManager permissionsManager;
	private HelpRequestManager helpRequestManager;
	private HelpRequest helpRequest;
	private Utils utils;
	private MySQL mySQL;
	private PunishmentManager punishmentManager;

	public SCPlayerManager getSCPlayerManager() {
		return scPlayerManager;
	}

	public static SCCore getInstance() {
		return SCCore.instance;
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
		pm.registerEvents(new ChatListener(this), this);
	}

	public void registerCommands() {
		this.getCommand("channel").setExecutor(new ChannelCommand(this));
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
		this.getCommand("helprequest").setExecutor(new HelpRequestCommand(this));

	}

	public void registerManagers() {
		this.mySQL = new MySQL(this);
		this.rankManager = new RankManager(this);
		this.permissionsManager = new PermissionsManager(this);
		this.statsManager = new StatsManager(this);
		this.punishmentManager = new PunishmentManager(this);
		this.scPlayerManager = new SCPlayerManager(this);
		this.arenaManager = new ArenaManager(this);
		this.helpRequestManager = new HelpRequestManager(this);
		this.utils = new Utils();
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		event.setMotd("                §c§lSensation§4§lCraft");
	}
}