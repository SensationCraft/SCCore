package com.sensationcraft.sccore.shop;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.utils.ProtocolUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Anml on 1/23/16.
 */
public class BuyListeners implements Listener {

	private SCCore instance;
	private Essentials essentials;
	private ItemManager itemManager;

	private String prefix = "§b§lBuy Shop";

	public BuyListeners(SCCore instance) {
		this.instance = instance;
		this.essentials = instance.getEssentials();
		this.itemManager = instance.getItemManager();
	}

	@EventHandler
	public void perCategoryMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack stack = e.getCurrentItem();

		if (!e.getInventory().getName().contains(prefix + " §6["))
			return;

		e.setCancelled(true);

		if (stack == null || stack.getType().equals(Material.AIR))
			return;

		Material material = stack.getType();
		byte b = stack.getData().getData();

		String name = ChatColor.stripColor(e.getInventory().getName()).split(" ")[2];
		name = name.substring(1, name.length() - 1);

		ItemCategory category = null;
		try {
			category = ItemCategory.valueOf(name.toUpperCase());
		} catch (Exception ex) {
			player.closeInventory();
			player.sendMessage("§cCategory name was not able to be determined. Contact an administrator.");
			return;
		}

		if (e.getRawSlot() <= 44) {
			List<Item> items = this.itemManager.getCategorialItems(category);

			for (Item item : items) {
				if (item.getMaterial().equals(material) && item.getB() == b) {
					if (item.isBulk()) {
						player.openInventory(this.itemManager.getBulkInventory(item, player));
						return;
					} else {
						User user = this.essentials.getUser(player);
						double price = item.getPrice() / item.getAmount();
						if (!user.canAfford(BigDecimal.valueOf(price))) {
							player.sendMessage("§cYou do not have enough money to purchase this item.");
							return;
						} else {
							for (ItemStack s : player.getInventory().getContents()) {
								if (s.getType().equals(item.getMaterial()) && item.getItemStack().getData().getData() == item.getB() && s.getMaxStackSize() != (s.getAmount() + 1)) {
									s.setAmount(s.getAmount() + 1);
									player.sendMessage("§aYou have purchased §61x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " §afor §6$" + price + "§a.");
									user.takeMoney(BigDecimal.valueOf(price));
									return;
								}
							}
							if (player.getInventory().firstEmpty() == -1) {
								player.sendMessage("§cYou currently have a full inventory!");
								return;
							}

							player.getInventory().addItem(item.getItemStack());
							player.sendMessage("§aYou have purchased §61x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " §afor §6$" + price + "§a.");
							user.takeMoney(BigDecimal.valueOf(price));
							return;
						}
					}
				}
			}
		}
	}

	//@EventHandler
	public void onMainMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inventory = e.getClickedInventory();
		//TODO

	}
}
