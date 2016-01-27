package com.sensationcraft.sccore.xrayspy;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.ranks.Rank;
import com.sensationcraft.sccore.ranks.RankManager;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.ProtocolUtil;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Created by Anml on 1/24/16.
 */

@Getter
public class XrayManager implements Listener {

    private static final List<Material> materials = Lists.newArrayList(Material.EMERALD_ORE, Material.DIAMOND_ORE, Material.GOLD_ORE,
            Material.IRON_ORE, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.LAPIS_ORE);

    private SCCore instance;
    private SCPlayerManager scPlayerManager;
    private RankManager rankManager;
    private Set<Player> xraySpyers = Collections.newSetFromMap(new WeakHashMap<>());

    public XrayManager(SCCore instance) {
        this.instance = instance;
        this.scPlayerManager = instance.getSCPlayerManager();
        this.rankManager = instance.getRankManager();
    }

    //@EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Material material = e.getBlock().getType();
        if (!XrayManager.materials.contains(material))
            return;

        Player player = e.getPlayer();
        SCPlayer scPlayer = this.scPlayerManager.getSCPlayer(player.getUniqueId());

        Rank rank = this.rankManager.getRank(player.getUniqueId());
        if (rank.getId() >= Rank.MOD.getId()) {
            return;
        }

        /*String itemName = ProtocolUtil.getItemStackName(new ItemStack(material, 1));
        if (itemName.equalsIgnoreCase("ERROR")) itemName = "Redstone Ore";*/
        FancyMessage message = new FancyMessage("[XRAY] ").color(ChatColor.YELLOW).then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" has mined ")
                .color(ChatColor.GRAY).then(material.name()).color(ChatColor.AQUA).then(".").color(ChatColor.GRAY);
        for (Player p:this.xraySpyers) {
        	message.send(p);
        }


    }
}
