package com.sensationcraft.sccore;

import com.comphenix.protocol.ProtocolLibrary;
import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.sensationcraft.sccore.chat.ChatListener;
import com.sensationcraft.sccore.chat.commands.ChannelCommand;
import com.sensationcraft.sccore.chat.commands.ShoutCommand;
import com.sensationcraft.sccore.chat.commands.StaffCommand;
import com.sensationcraft.sccore.chat.factions.FactionsListener;
import com.sensationcraft.sccore.crates.CratesListener;
import com.sensationcraft.sccore.crates.GiveKeyShardCommand;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.duels.DuelListeners;
import com.sensationcraft.sccore.duels.commands.ArenaCommand;
import com.sensationcraft.sccore.duels.commands.DuelCommand;
import com.sensationcraft.sccore.duels.commands.SpectateCommand;
import com.sensationcraft.sccore.help.TutorialManager;
import com.sensationcraft.sccore.help.commands.HelpCommand;
import com.sensationcraft.sccore.help.commands.TutorialCommand;
import com.sensationcraft.sccore.helprequests.HelpRequestManager;
import com.sensationcraft.sccore.helprequests.commands.HelpRequestCommand;
import com.sensationcraft.sccore.lockpicks.LockpickListeners;
import com.sensationcraft.sccore.mcmmo.MessageListener;
import com.sensationcraft.sccore.mysql.MySQL;
import com.sensationcraft.sccore.punishments.PunishmentListeners;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.commands.*;
import com.sensationcraft.sccore.ranks.PermissionsManager;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.ranks.commands.PermsCommand;
import com.sensationcraft.sccore.ranks.commands.RankCommand;
import com.sensationcraft.sccore.scplayer.SCPlayerListeners;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.shop.ItemManager;
import com.sensationcraft.sccore.shop.SellCauldron;
import com.sensationcraft.sccore.shop.commands.ShopCommand;
import com.sensationcraft.sccore.stats.StatListeners;
import com.sensationcraft.sccore.stats.StatsManager;
import com.sensationcraft.sccore.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by Anml on 12/26/15.
 */

@Getter
public class SCCore extends JavaPlugin implements Listener {

	public static SCCore instance;
	private Essentials essentials;
	private SCPlayerManager scPlayerManager;
	private ArenaManager arenaManager;
	private ItemManager itemManager;
	private RankManager rankManager;
	private StatsManager statsManager;
	private PermissionsManager permissionsManager;
	private PunishmentManager punishmentManager;
	private TutorialManager tutorialManager;
	private HelpRequestManager helpRequestManager;
	private ShoutCommand shoutCommand;
	private Utils utils;
	private MySQL mySQL;

	public static SCCore getInstance() {
		return SCCore.instance;
	}

	public SCPlayerManager getSCPlayerManager() {
		return this.scPlayerManager;
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

		for (UUID uuid : this.scPlayerManager.getScPlayers().keySet()) this.scPlayerManager.removeSCPlayer(uuid);

		this.mySQL.close();

		SCCore.instance = null;

		this.getLogger().info("[SCCore] Plugin has been disabled.");

	}

	public void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		shoutCommand = new ShoutCommand(this);

		pm.registerEvents(new SCPlayerListeners(this), this);
		pm.registerEvents(new LockpickListeners(this), this);
		pm.registerEvents(new DuelListeners(this), this);
		pm.registerEvents(new StatListeners(this), this);
		pm.registerEvents(new PunishmentListeners(this), this);
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new SellCauldron(this), this);
		pm.registerEvents(new CratesListener(), this);
		pm.registerEvents(tutorialManager, this);
		pm.registerEvents(new FactionsListener(), this);

		ProtocolLibrary.getProtocolManager().addPacketListener(new MessageListener(this));
	}

	public void registerCommands() {
		this.getCommand("shop").setExecutor(new ShopCommand(this));
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
		this.getCommand("history").setExecutor(new HistoryCommand(this));
		this.getCommand("helprequest").setExecutor(new HelpRequestCommand(this));
		this.getCommand("givekeyshard").setExecutor(new GiveKeyShardCommand());
		this.getCommand("help").setExecutor(new HelpCommand(this));
		this.getCommand("tutorial").setExecutor(new TutorialCommand(this));
	}

	public void registerManagers() {
		//THESE ARE ORDER SENSETIVE, DON'T CHANGE THEM WILLY-NILLY!
		this.mySQL = new MySQL(this);
		this.utils = new Utils();
		this.rankManager = new RankManager(this);
		this.permissionsManager = new PermissionsManager(this);
		this.statsManager = new StatsManager(this);
		this.helpRequestManager = new HelpRequestManager(this);
		this.punishmentManager = new PunishmentManager(this);
		this.itemManager = new ItemManager(this);
		this.tutorialManager = new TutorialManager(this);
		this.scPlayerManager = new SCPlayerManager(this);
		this.arenaManager = new ArenaManager(this);
	}
}