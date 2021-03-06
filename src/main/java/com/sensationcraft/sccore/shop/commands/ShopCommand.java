package com.sensationcraft.sccore.shop.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.shop.Item;
import com.sensationcraft.sccore.shop.ItemCategory;
import com.sensationcraft.sccore.shop.ItemManager;
import com.sensationcraft.sccore.shop.ItemType;
import com.sensationcraft.sccore.utils.ProtocolUtil;

/**
 * Created by Anml on 1/22/16.
 */
public class ShopCommand implements CommandExecutor {

	SCCore instance;
	ItemManager itemManager;

	public ShopCommand(SCCore instance) {
		this.instance = instance;
		this.itemManager = instance.getItemManager();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command is only accessible to players.");
			return false;
		}

		Player player = (Player) sender;

		if (!sender.hasPermission("sccore.shop")) {
			sender.sendMessage("§cYou do not have permission to execute this command.");
			return false;
		}

		String usage = "§4Usage: §c/shop s:add <price>\n" +
				"         /shop b:add <price> <category> <bulk>\n" +
				"         /shop remove <type>\n" +
				"         /shop list <type>";

		if (args.length == 0 || args.length < this.getMinArgs(args[0])) {
			sender.sendMessage(usage);
			return false;
		}


		if (args[0].equalsIgnoreCase("list")) {

			ItemType type = null;
			try {
				type = ItemType.valueOf(args[1].toUpperCase());
			} catch (Exception e) {
				sender.sendMessage("§cYou have entered a invalid item type.");
				return false;
			}

			if (type.equals(ItemType.BUY) && this.itemManager.getBuy().size() == 0) {
				sender.sendMessage("§cThere are currently no item's listed under the BUY type.");
				return false;
			}
			if (type.equals(ItemType.SELL) && this.itemManager.getSell().size() == 0) {
				sender.sendMessage("§cThere are currently no item's listed under the SELL type.");
				return false;
			}

			sender.sendMessage("§aCurrent items listed under " + type.name() + " type:");
			if (type.equals(ItemType.BUY)) {
				for (Item item : this.itemManager.getBuy()) {
					String s = "§f   - §6" + item.getAmount() + "x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " with a price of $" + item.getPrice() + " (Category: " + item.getCategory().name() + ")";
					sender.sendMessage(s);
				}
				return true;
			}
			if (type.equals(ItemType.SELL)) {
				for (Item item : this.itemManager.getSell()) {
					String s = "§f   - §6" + item.getAmount() + "x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " with a price of $" + item.getPrice();
					sender.sendMessage(s);
				}
				return true;
			}
			return false;
		}

		ItemStack stack = player.getItemInHand();

		if (stack == null || stack.getType().equals(Material.AIR)) {
			sender.sendMessage("§cYou are not holding an item in your hand.");
			return false;
		}

		if (stack.getItemMeta().hasEnchants()) {
			sender.sendMessage("§cThe item in your hand currently contains enchantments disallowing you from adding/removing it in shop.");
			return false;
		}

		if (args[0].equalsIgnoreCase("s:add") || args[0].equalsIgnoreCase("b:add")) {

			ItemType type = ItemType.BUY;
			if (args[0].equalsIgnoreCase("s:add"))
				type = ItemType.SELL;

			double price = 0;
			try {
				price = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§cYou have entered an invalid price.");
				return false;
			}

			if (price == 0) {
				sender.sendMessage("§cYou are not allowed to enter a 0 as the price.");
				return false;
			}

			ItemCategory category = null;
			boolean bulk = false;
			if (type.equals(ItemType.BUY)) {
				try {
					category = ItemCategory.valueOf(args[2].toUpperCase());
				} catch (Exception e) {
					sender.sendMessage("§cYou have entered a invalid category.");
					return false;
				}
				try {
					bulk = Boolean.parseBoolean(args[3]);
				} catch (Exception e) {
					sender.sendMessage("§cYou have entered a invalid boolean value.");
					return false;
				}
			}

			boolean task;
			if (type.equals(ItemType.BUY)) {
				task = this.itemManager.addItem(new Item(stack.getType(), stack.getData().getData(), stack.getAmount(), price, category, bulk));
			} else {
				task = this.itemManager.addItem(new Item(stack.getType(), stack.getData().getData(), stack.getAmount(), price));
			}

			if (task) {
				if (type.equals(ItemType.SELL)) {
					sender.sendMessage("§aYou have added the item " + stack.getAmount() + "x " + ProtocolUtil.getItemStackName(stack) + " to the " + type.name() + " shop for a price of $" + price + ".");
				} else {
					sender.sendMessage("§aYou have added the item " + stack.getAmount() + "x " + ProtocolUtil.getItemStackName(stack) + " to the " + type.name() + " shop for a price of $" + price + " (Category: " + category.name() + ", Bulk: " + bulk + ").");
				}
				return true;
			} else {
				sender.sendMessage("§cThe material you are trying to add to the shop already exists.");
				return false;
			}
		} else if (args[0].equalsIgnoreCase("remove")) {

			ItemType type = null;
			try {
				type = ItemType.valueOf(args[1].toUpperCase());
			} catch (Exception e) {
				sender.sendMessage("§cYou have entered a invalid item type.");
				return false;
			}

			Item item = this.itemManager.removeItem(type, stack.getType(), stack.getData().getData());

			if (item == null) {
				sender.sendMessage("§aThe material you are trying to remove doesn't exist.");
				return false;
			}

			sender.sendMessage("§aYou have removed the item " + item.getAmount() + "x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " which had a price of $" + item.getPrice() + " from the " + type.name() + " shop.");
			return true;
		}

		return false;
	}

	public int getMinArgs(String subcommand) {
		switch (subcommand.toLowerCase()) {
		case "s:add":
			return 2;
		case "b:add":
			return 4;
		case "remove":
			return 2;
		case "list":
			return 2;
		default:
			return 100;
		}
	}
}
