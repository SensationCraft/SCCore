package org.sensationcraft.sccore.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.sensationcraft.sccore.SCCore;
import org.sensationcraft.sccore.scplayer.SCPlayer;
import org.sensationcraft.sccore.scplayer.SCPlayerManager;

import java.util.Arrays;

/**
 * Created by Anml on 1/12/16.
 */
public class StatListeners implements Listener {

    private SCCore instance;
    private SCPlayerManager scPlayerManager;
    private StatsManager statsManager;

    public StatListeners(SCCore instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        statsManager = instance.getStatsManager();
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(final PlayerDeathEvent e) {

        e.setDeathMessage(null);

        if (!(e.getEntity().getKiller() instanceof Player))
            return;

        Player playerKilled = e.getEntity();
        SCPlayer killed = scPlayerManager.getSCPlayer(playerKilled.getUniqueId());
        Player playerKiller = e.getEntity().getKiller();
        SCPlayer killer = scPlayerManager.getSCPlayer(playerKiller.getUniqueId());

        statsManager.setIntegerStat(playerKilled.getUniqueId(), Stat.DEATHS, statsManager.getIntegerStat(playerKilled.getUniqueId(), Stat.DEATHS) + 1);
        statsManager.setIntegerStat(playerKiller.getUniqueId(), Stat.KILLS, statsManager.getIntegerStat(playerKilled.getUniqueId(), Stat.KILLS) + 1);

        killed.removeCombatTag();

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1);
        head.setDurability((short) 3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setLore(Arrays.asList("§aKiller: " + killer.getTag()));
        headMeta.setOwner(playerKilled.getName());
        headMeta.setDisplayName(killed.getTag() + "§f's Head");
        head.setItemMeta(headMeta);
        e.getDrops().add(head);
    }
}
