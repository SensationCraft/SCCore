package com.sensationcraft.sccore.crates;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Anml on 1/27/16.
 */
public class CratesListener implements Listener {

    final private String name = "§6Vote Key Fragment";
    final private List<String> lore = Arrays.asList("§a§l----------------------", "§e§oUse 4 of these fragmented", "§e§okey shards to combine a", "§e§oa complete §6§oVote Key§6§o.", "", "§e§lRight-click to combine!", "§a§l----------------------");

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getItemInHand();
        Action action = e.getAction();

        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (item.getType().getId() != Material.PRISMARINE_SHARD.getId())
            return;

        if (item.getAmount() != 4)
            return;

        if (!item.getItemMeta().getDisplayName().equalsIgnoreCase(name))
            return;

        if (!item.getItemMeta().getEnchants().keySet().contains(Enchantment.getById(300)))
            return;

        if (item.getItemMeta().getLore() == null)
            return;

        if (!item.getItemMeta().getLore().containsAll(lore))
            return;

        player.getInventory().clear(player.getInventory().getHeldItemSlot());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate givekey " + player.getName() + " RouletteKey 1");

        Location heart = player.getLocation();
        heart.setY(heart.getY() + 1);
        heart.getWorld().playEffect(heart, Effect.HEART, 80);
        heart.getWorld().playEffect(heart, Effect.HEART, 80);
        heart.getWorld().playEffect(heart, Effect.HEART, 80);
        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
        player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);

        return;
    }
}
