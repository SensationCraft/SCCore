package com.sensationcraft.sccore.shop;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.earth2me.essentials.Essentials;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.utils.ProtocolUtil;

import lombok.Getter;

/**
 * Created by Anml on 1/20/16.
 */

@Getter
public class ItemManager {

	private SCCore instance;
	private Essentials essentials;
	private List<Item> buy;
	private List<Item> sell;

	public ItemManager(SCCore instance) {
		this.instance = instance;
		this.essentials = instance.getEssentials();
		this.buy = new ArrayList<>();
		this.sell = new ArrayList<>();
	}

	public void load() {

		this.buy = new ArrayList<>();
		this.sell = new ArrayList<>();

		String path = "Shop.Items";
		FileConfiguration config = this.instance.getConfig();

		Material material = null;
		byte b = 0;
		ItemType type = null;
		int amount = 0;
		double price = 0;
		ItemCategory category = null;
		boolean bulk = false;

		if (!config.contains(path))
			return;

		for (String string : config.getStringList(path)) {

			String[] parts = string.split(" ");
			if (parts.length >= 4) {

				if (parts[0].contains(":")) {
					String[] idParts = parts[0].split(":");
					try {
						int id = Integer.parseInt(idParts[0]);
						byte bid = Byte.parseByte(idParts[1]);

						material = Material.getMaterial(id);
						b = bid;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						int id = Integer.parseInt(parts[0]);
						material = Material.getMaterial(id);
					} catch (Exception e) {
						e.printStackTrace();
					}
					b = 0;
				}

				try {
					amount = Integer.parseInt(parts[1]);
					type = ItemType.valueOf(parts[2].toUpperCase());
					price = Double.parseDouble(parts[3]);
					if (parts.length >= 6) {
						category = ItemCategory.valueOf(parts[4].toUpperCase());
						bulk = Boolean.parseBoolean(parts[6]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (material != null && type != null && amount != 0 && price != 0) {
					if (type.equals(ItemType.BUY)) {
						if (category != null) {
							boolean put = true;
							for (Item i : this.buy) {
								if (material.equals(i.getMaterial()) && b == i.getB()) put = false;
							}
							if (put)
								this.buy.add(new Item(material, b, amount, price, category, bulk));
						}
					} else {
						boolean put = true;
						for (Item i : this.sell) {
							if (material.equals(i.getMaterial()) && b == i.getB()) put = false;
						}
						if (put)
							this.sell.add(new Item(material, b, amount, price));
					}
				}
			}
		}
	}

	public void save() {

		String path = "Shop.Items";
		FileConfiguration config = this.instance.getConfig();

		if (this.buy.size() == 0 && this.sell.size() == 0)
			return;

		List<String> beingStored = new ArrayList<>();

		for (Item item : this.buy) {
			if (item.getMaterial() != null && item.getPrice() != 0 && item.getPrice() != 0) {
				String input = "" + item.getMaterial().getId();
				if (item.getB() != 0)
					input += ":" + item.getB();
				input += " " + item.getAmount() + " " + item.getType() + " " + item.getPrice() + " " + item.getCategory().name() + " " + item.isBulk();

				beingStored.add(input);
			}
		}

		for (Item item : this.sell) {
			if (item.getMaterial() != null && item.getPrice() != 0 && item.getPrice() != 0) {
				String input = "" + item.getMaterial().getId();
				if (item.getB() != 0)
					input += ":" + item.getB();
				input += " " + item.getAmount() + " " + item.getType() + " " + item.getPrice();

				beingStored.add(input);

			}
		}

		config.set(path, beingStored);
		this.instance.saveConfig();
	}

	public boolean addItem(Item item) {

		if (item.getType().equals(ItemType.BUY)) {
			if (this.buy.contains(item)) return false;

			for (Item i : this.buy) {
				if (i.getMaterial().equals(item.getMaterial()) && i.getB() == item.getB())
					return false;
			}

			this.buy.add(item);
			this.save();
			return true;
		}

		if (item.getType().equals(ItemType.SELL)) {

			if (this.sell.contains(item)) return false;

			for (Item i : this.sell) {
				if (i.getMaterial().equals(item.getMaterial()) && i.getB() == item.getB())
					return false;
			}

			this.sell.add(item);
			this.save();
			return true;
		}

		return false;
	}

	public Item removeItem(ItemType type, Material material, byte b) {

		if (type.equals(ItemType.SELL)) {
			for (Item i : this.sell) {
				if (i.getMaterial().equals(material) && i.getB() == b) {
					this.sell.remove(i);
					this.save();
					return i;
				}
			}
			return null;
		}

		if (type.equals(ItemType.BUY)) {
			for (Item i : this.sell) {
				if (i.getMaterial().equals(material) && i.getB() == b) {
					this.sell.remove(i);
					this.save();
					return i;
				}
			}
			return null;
		}

		return null;
	}

	public List<Item> getCategorialItems(ItemCategory category) {
		List<Item> categorialItems = new ArrayList<>();
		for (Item item : this.buy) {
			if (item.getCategory().equals(category))
				categorialItems.add(item);
		}
		return categorialItems;
	}

	public Inventory getMainInvenotory() {
		Inventory inventory = Bukkit.createInventory(null, 9, "§b§lBuy Shop §6§l(Main Menu)");

		return inventory;
	}

	public List<Inventory> getInventories(ItemCategory category) {
		List<Item> categorialItems = this.getCategorialItems(category);

		if (categorialItems.size() == 0)
			return null;

		int invCount = (int) Math.round((categorialItems.size() / 45) + .5);

		List<Inventory> inventories = new ArrayList<>();

		for (int count = 0; count <= invCount; count++) {
			inventories.add(Bukkit.createInventory(null, 54, "§b§lBuy Shop §f§bl- §6§l" + category.name() + " (" + (count + 1) + "/" + invCount + ")"));

			for (int x = 0; x < invCount; x++) {
				for (int y = 0; x < 45; x++) {
					if (categorialItems.get(y) != null) {
						inventories.get(x).setItem(y, categorialItems.get(0).getItemStack());
						categorialItems.remove(0);
					}
				}

				ItemStack item = new ItemStack(Material.ARROW, 1);
				ItemMeta meta = item.getItemMeta();
				if (x != (invCount - 1)) {
					meta.setDisplayName("§bNext Page (" + (x + 2) + "/" + invCount + ")");
					item.setItemMeta(meta);
					inventories.get(x).setItem(50, item);
				}
				if (x != 0) {
					meta.setDisplayName("§bPrevious Page (" + (x) + "/" + invCount + ")");
					item.setItemMeta(meta);
					inventories.get(x).setItem(48, item);
				} else {
					meta.setDisplayName("§bPrevious Page (Main Menu)");
					item.setItemMeta(meta);
					inventories.get(x).setItem(48, item);
				}
			}
		}

		return inventories;
	}

	public ItemStack getBulkItem(Item item, int amount, Player player) {
		ItemStack stack = new ItemStack(item.getMaterial(), amount, item.getB());
		ItemMeta meta = stack.getItemMeta();
		DecimalFormat df = new DecimalFormat("#.##");
		double cost = Double.valueOf(df.format(item.getPrice() / item.getAmount() * amount));
		boolean afford = this.essentials.getUser(player).canAfford(BigDecimal.valueOf(cost));
		meta.setDisplayName("§6" + ProtocolUtil.getItemStackName(stack));
		meta.setLore(Arrays.asList("§e--------------------", "§aPrice: §f$" + cost, "§aCan Afford: §f$" + afford, "§e--------------------"));
		stack.setItemMeta(meta);

		return stack;
	}

	public Inventory getBulkInventory(Item item, Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, "§b§l" + ProtocolUtil.getItemStackName(item.getItemStack()) + "§6§l(Bulk Menu)");
		inventory.setItem(1, this.getBulkItem(item, 1, player));
		inventory.setItem(2, this.getBulkItem(item, 2, player));
		inventory.setItem(3, this.getBulkItem(item, 4, player));
		inventory.setItem(4, this.getBulkItem(item, 8, player));
		inventory.setItem(5, this.getBulkItem(item, 16, player));
		inventory.setItem(6, this.getBulkItem(item, 32, player));
		inventory.setItem(7, this.getBulkItem(item, 64, player));

		return inventory;
	}
}
