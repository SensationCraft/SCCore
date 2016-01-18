package com.sensationcraft.sccore;

import com.earth2me.essentials.Essentials;
import com.sensationcraft.sccore.chat.commands.ShoutCommand;
import com.sensationcraft.sccore.chat.commands.StaffCommand;
import com.sensationcraft.sccore.duels.ArenaManager;
import com.sensationcraft.sccore.duels.DuelListeners;
import com.sensationcraft.sccore.duels.commands.ArenaCommand;
import com.sensationcraft.sccore.duels.commands.DuelCommand;
import com.sensationcraft.sccore.duels.commands.SpectateCommand;
import com.sensationcraft.sccore.helprequests.*;
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

public class Main extends JavaPlugin implements Listener {

    public static Main instance;
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

    public static Main getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public HelpRequest getHelpRequest() {
        return helpRequest;
    }
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public SCPlayerManager getSCPlayerManager() {
        return scPlayerManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public Utils getUtils() {
        return utils;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null || !essentials.isEnabled()) {
            this.getLogger().info("Essentials was not found on the server, resulting in the server shutting down.");
            this.getServer().shutdown();
        }

        registerManagers();
        registerEvents();
        registerCommands();

        scPlayerManager.loadSCPlayers();

        this.getLogger().info("[SCCore] Plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer("§cSensationCraft §7is restarting. Please wait 30 seconds before re-logging.");

        instance = null;

        this.getLogger().info("[SCCore] Plugin has been disabled.");

    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvents(scPlayerManager, this);
        pm.registerEvents(this, this);
        pm.registerEvents(new LockpickListeners(this), this);
        pm.registerEvents(new DuelListeners(this), this);
        pm.registerEvents(new StatListeners(this), this);
        pm.registerEvents(new PunishmentListeners(this), this);
    }

    public void registerCommands() {
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("staff").setExecutor(new StaffCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("perms").setExecutor(new PermsCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempban").setExecutor(new TempbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tempmute").setExecutor(new TempmuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("helpaccept").setExecutor(new HelpAccept(helpRequest));
        getCommand("helpcancel").setExecutor(new HelpCancel(helpRequest));
        getCommand("helpdeny").setExecutor(new HelpDeny(helpRequest));
        getCommand("helplist").setExecutor(new HelpList(helpRequest));
        getCommand("helpread").setExecutor(new HelpRead(helpRequest));
        getCommand("helprequest").setExecutor(new HelpRequest());

    }

    public void registerManagers() {
        mySQL = new MySQL(this);
        rankManager = new RankManager(this);
        permissionsManager = new PermissionsManager(this);
        statsManager = new StatsManager(this);
        punishmentManager = new PunishmentManager(this);
        scPlayerManager = new SCPlayerManager(this);
        arenaManager = new ArenaManager(this);
        helpRequest = new HelpRequest();
        utils = new Utils();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd("                §c§lSensation§4§lCraft");
    }
}