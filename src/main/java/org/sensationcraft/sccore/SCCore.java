package org.sensationcraft.sccore;

import com.earth2me.essentials.Essentials;

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
        return mySQL;
    }

    public RankManager getRankManager() {
        return rankManager;
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
    }

    public void registerCommands() {
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("perms").setExecutor(new PermsCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));

    }

    public void registerManagers() {
        mySQL = new MySQL(this);
        scPlayerManager = new SCPlayerManager(this);
        arenaManager = new ArenaManager(this);
        rankManager = new RankManager(this);
        permissionsManager = new PermissionsManager(this);
        statsManager = new StatsManager(this);
        utils = new Utils();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd("                §c§lSensation§4§lCraft");
    }
}