package com.sensationcraft.sccore.lockpicks;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Created by Anml on 12/31/15.
 */
public class LockpickRunnable extends BukkitRunnable {
	private int counter;
	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private SCPlayer scPlayer;
	private Block block;
	private UUID uuid;

	public LockpickRunnable(SCCore instance, Block block, UUID uuid, int counter) {
		this.counter = counter;
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.block = block;
		this.uuid = uuid;
		this.scPlayer = this.scPlayerManager.getSCPlayer(uuid);
	}

	@Override
	public void run() {
		if (this.counter++ == 5) {
			Player player = Bukkit.getPlayer(this.uuid);
			if (this.scPlayer.lockpickAttempt()) {
				this.block.breakNaturally();
				player.sendMessage("§aThe luck was in your favor, resulting in a successful lockpick.");
				player.playSound(player.getLocation(), Sound.BURP, 1F, 1F);
				player.playSound(player.getLocation(), Sound.BURP, 1F, 1F);
				player.playSound(player.getLocation(), Sound.BURP, 1F, 1F);
			} else
				player.sendMessage("§cThe luck was not in your favor, resulting in an unsuccessful lockpick.");
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
			final LockpickRunnable task = this.scPlayerManager.getLockpicking().remove(player.getUniqueId());
			if (task != null) {
				task.cancel();
			}
		} else {
			Location smoke = this.block.getLocation();
			smoke.setY(smoke.getY() + 1);
			smoke.getWorld().playEffect(smoke, Effect.SMOKE, 80);
			smoke.getWorld().playEffect(smoke, Effect.SMOKE, 80);
			smoke.getWorld().playEffect(smoke, Effect.SMOKE, 80);
		}
	}

}
