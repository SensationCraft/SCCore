package org.sensationcraft.sccore.lockpicks;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.sccore.Main;
import org.sensationcraft.sccore.scplayer.SCPlayer;
import org.sensationcraft.sccore.scplayer.SCPlayerManager;

import java.util.UUID;

/**
 * Created by Anml on 12/31/15.
 */
public class LockpickRunnable extends BukkitRunnable {
    private int counter;
    private Main instance;
    private SCPlayerManager scPlayerManager;
    private SCPlayer scPlayer;
    private Block block;
    private UUID uuid;

    public LockpickRunnable(Main instance, Block block, UUID uuid, int counter) {
        this.counter = counter;
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        this.block = block;
        this.uuid = uuid;
        scPlayer = scPlayerManager.getSCPlayer(uuid);
    }

    @Override
    public void run() {
        if (counter == 5) {
            Player player = Bukkit.getPlayer(uuid);
            if (scPlayer.lockpickAttempt()) {
                block.breakNaturally();
                player.sendMessage("§aThe luck was in your favor, resulting in a successful lockpick.");
            } else
                player.sendMessage("§cThe luck was not in your favor, resulting in an unsuccessful lockpick.");
            final LockpickRunnable task = scPlayerManager.getLockpicking().remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
            }
        } else {
            counter++;
            Location smoke = block.getLocation();
            smoke.setY(smoke.getY() + 1);
            smoke.getWorld().playEffect(smoke, Effect.SMOKE, 80);

        }
    }

}
