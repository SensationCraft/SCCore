package com.sensationcraft.sccore.xrayspy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.ProtocolUtil;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

import lombok.Getter;

/**
 * Created by Anml on 1/24/16.
 */

@Getter
public class XrayManager implements Listener {

	private SCCore instance;
	private SCPlayerManager scPlayerManager;
	private RankManager rankManager;
	private List<UUID> xraySpyers = new ArrayList<>();

	public XrayManager(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
		this.rankManager = instance.getRankManager();
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {

		Player player = e.getPlayer();
		SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());
		Material material = e.getBlock().getType();

		Rank rank = this.rankManager.getRank(player.getUniqueId());
		if (rank.getId() >= Rank.MOD.getId()) {
			return;
		}

		List<Material> materials = new ArrayList<>();
		materials.add(Material.EMERALD_ORE);
		materials.add(Material.DIAMOND_ORE);
		materials.add(Material.GOLD_ORE);
		materials.add(Material.IRON_ORE);
		materials.add(Material.REDSTONE_ORE);
		materials.add(Material.GLOWING_REDSTONE_ORE);
		materials.add(Material.LAPIS_ORE);

		for (Material m : materials) {
			if (material.equals(m)) {
				String itemName = ProtocolUtil.getItemStackName(new ItemStack(m, 1));
				if (itemName.equalsIgnoreCase("ERROR")) itemName = "Redstone Ore";
				FancyMessage message = new FancyMessage("§e[XRAY] ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §7has mined §b" + itemName + "§7.", true);
				for (UUID uuid : this.xraySpyers) {
					OfflinePlayer spyer = Bukkit.getOfflinePlayer(uuid);

					if (spyer != null && spyer.isOnline()) {
						message.send(spyer.getPlayer());
					}
				}
			}
		}


	}
}
