package com.sensationcraft.sccore.shop;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.sensationcraft.sccore.SCCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Anml on 1/23/16.
 */
public class BuyListeners implements Listener {

    private SCCore instance;
    private Essentials essentials;
    private ItemManager itemManager;

    public BuyListeners(SCCore instance) {
        this.instance = instance;
        this.essentials = instance.getEssentials();
        this.itemManager = instance.getItemManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack stack = e.getCurrentItem();

        if (!e.getInventory().getName().contains("§b§lBuy Shop §f§bl- "))
            return;

        e.setCancelled(true);

        if (stack == null || stack.getType().equals(Material.AIR))
            return;

        Material material = stack.getType();
        byte b = stack.getData().getData();

        String name = ChatColor.stripColor(e.getInventory().getName()).split(" ")[3];

        ItemCategory category = null;
        try {
            category = ItemCategory.valueOf(name.toUpperCase());
        } catch (Exception ex) {
            player.sendMessage("§cCategory name was not able to be determined. Contact an administrator.");
            return;
        }

        if (e.getRawSlot() <= 44) {
            List<Item> items = itemManager.getCategorialItems(category);

            for (Item item : items) {
                if (item.getMaterial().equals(material) && item.getB() == b) {
                    if (item.isBulk()) {
                        player.openInventory(itemManager.getBulkInventory(item, player));
                        return;
                    } else {
                        User user = essentials.getUser(player);

                        //CAN AFFORD
                    }
                }
            }
        }
    }
}
