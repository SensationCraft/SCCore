package com.sensationcraft.sccore.stats;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
		this.scPlayerManager = instance.getSCPlayerManager();
		this.statsManager = instance.getStatsManager();
	}


	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(final PlayerDeathEvent e) {

		e.setDeathMessage(null);

		if (!(e.getEntity().getKiller() instanceof Player))
			return;

		Player playerKilled = e.getEntity();
		SCPlayer killed = this.scPlayerManager.getSCPlayer(playerKilled.getUniqueId());
		Player playerKiller = e.getEntity().getKiller();
		SCPlayer killer = this.scPlayerManager.getSCPlayer(playerKiller.getUniqueId());

		this.statsManager.setIntegerStat(playerKilled.getUniqueId(), Stat.DEATHS, this.statsManager.getIntegerStat(playerKilled.getUniqueId(), Stat.DEATHS) + 1);
		this.statsManager.setIntegerStat(playerKiller.getUniqueId(), Stat.KILLS, this.statsManager.getIntegerStat(playerKiller.getUniqueId(), Stat.KILLS) + 1);

		killed.removeCombatTag();

		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1);
		head.setDurability((short) 3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		headMeta.setLore(Arrays.asList("§aKiller: " + killer.getTag()));
		headMeta.setOwner(playerKilled.getName());
		headMeta.setDisplayName(killed.getTag() + "§f's Head");
		head.setItemMeta(headMeta);
		e.getDrops().add(head);

		FancyMessage message = new FancyMessage("§cYou were killed by ").then(killer.getTag()).tooltip(killer.getHoverText()).then("§c.");
		message.send(playerKilled);
		message = new FancyMessage("§aYou have killed ").then(killed.getTag()).tooltip(killed.getHoverText()).then("§a.");
		message.send(playerKiller);
		playerKilled.playSound(playerKilled.getLocation(), Sound.NOTE_PLING, 3.0F, 0.533F);
		playerKiller.playSound(playerKiller.getLocation(), Sound.NOTE_PLING, 3.0F, 0.7F);
	}
}
