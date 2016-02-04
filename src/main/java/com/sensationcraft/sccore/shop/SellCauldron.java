package com.sensationcraft.sccore.shop;

import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.utils.ProtocolUtil;
import net.ess3.api.MaxMoneyException;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Anml on 1/21/16.
 */
public class SellCauldron implements Listener {

	private SCCore instance;
	private Essentials essentials;
	private RankManager rankManager;
	private ItemManager itemManager;
	private List<UUID> inCauldron;

	public SellCauldron(SCCore instance) {
		this.instance = instance;
		this.essentials = instance.getEssentials();
		this.rankManager = instance.getRankManager();
		this.itemManager = instance.getItemManager();
		this.inCauldron = new ArrayList<>();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {

		Player player = e.getPlayer();
		World world = e.getFrom().getWorld();
		Location from = e.getFrom();
		Location to = e.getTo();


		if (!this.validCauldrion(to)) {
			if (this.inCauldron.contains(player.getUniqueId()))
				this.inCauldron.remove(player.getUniqueId());
			return;
		}

		if (player.isSneaking()) {
			if (from.getX() != to.getX() && from.getY() != to.getY() && from.getZ() != to.getZ() && from.getPitch() != to.getPitch() && from.getYaw() != to.getYaw())
				this.sellProcess(player);
			return;
		}

		if (!this.inCauldron.contains(player.getUniqueId())) {
			player.sendMessage("§aTo confirm the selling process you must §6SNEAK§a.");
			this.inCauldron.add(player.getUniqueId());
			return;
		}
	}

	@EventHandler
	public void onSneakToggle(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		Location location = e.getPlayer().getLocation();

		if (!this.validCauldrion(location)) {
			if (this.inCauldron.contains(player.getUniqueId()))
				this.inCauldron.remove(player.getUniqueId());
			return;
		}

		if (!this.inCauldron.contains(player.getUniqueId()))
			return;

		if (e.isSneaking()) {
			this.sellProcess(e.getPlayer());
			this.inCauldron.remove(e.getPlayer().getUniqueId());
		}
	}

	public boolean validCauldrion(Location location) {

		if (location == null)
			return false;

		Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
		if (!faction.isSafeZone()) {
			return false;
		}

		return !(!location.getBlock().getType().equals(Material.CAULDRON) || location.getY() != 53.31250);

	}

	public void sellProcess(Player player) {
		if (player == null)
			return;

		Map<Item, Integer> result = new HashMap<>();
		Inventory inventory = player.getInventory();
		for (ItemStack stack : inventory.getContents()) {
			if (stack != null) {

				if (!stack.getItemMeta().hasEnchants()) {

					Material material = stack.getType();
					byte b = stack.getData().getData();

					for (Item item : this.itemManager.getSell()) {
						if (item.getMaterial().equals(material) && item.getB() == b) {
							inventory.remove(stack);
							if (result.containsKey(item))
								result.replace(item, result.get(item) + stack.getAmount());
							else
								result.put(item, stack.getAmount());
						}
					}
				}
			}
		}

		if (result.isEmpty()) {
			player.sendMessage("§cNo items in your inventory are able to be sold.");
			return;
		}

		Location crit = player.getLocation();
		crit.setY(player.getLocation().getY() + 1);
		crit.getWorld().playEffect(crit, Effect.MAGIC_CRIT, 80, 7);
		crit.getWorld().playEffect(crit, Effect.MAGIC_CRIT, 80, 7);
		crit.getWorld().playEffect(crit, Effect.MAGIC_CRIT, 80, 7);


		for (Item item : result.keySet()) {
			int amount = result.get(item);
			DecimalFormat df = new DecimalFormat("#.##");
			double charge = Double.valueOf(df.format((item.getPrice() / item.getAmount() * this.rankManager.getRank(player.getUniqueId()).getSellBoost()) * amount));
			try {
				this.essentials.getUser(player).giveMoney(BigDecimal.valueOf(charge));
			} catch (MaxMoneyException e) {
				player.sendMessage("§cYou have reached the maximum balance possible.");
				return;
			}
			String msg = "§6[SELL] " + amount + "x " + ProtocolUtil.getItemStackName(item.getItemStack()) + " §6(§4+$" + charge + "§6)";
			Rank rank = this.rankManager.getRank(player.getUniqueId());
			if (!rank.equals(Rank.DEFAULT)) {
				if (rank.getId() >= Rank.MOD.getId())
					rank = Rank.PREMIUMPLUS;
				msg += " (" + rank.getName() + "§6)";
			}
			player.sendMessage(msg);
		}

		player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1F, 1F);
		player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1F, 1F);
		player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1F, 1F);


	}
}
