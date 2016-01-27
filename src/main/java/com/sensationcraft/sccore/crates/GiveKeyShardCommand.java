package com.sensationcraft.sccore.crates;

import com.earth2me.essentials.Console;
import com.sensationcraft.sccore.SCCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Anml on 1/27/16.
 */
public class GiveKeyShardCommand implements CommandExecutor {

    final private String name = "§6Vote Key Fragment";
    final private List<String> lore = Arrays.asList("§a§l----------------------", "§e§oUse 4 of these fragmented", "§e§okey shards to combine a", "§e§oa complete §6§oVote Key§6§o.", "", "§e§lRight-click to combine!", "§a§l----------------------");
    private ItemStack item = new ItemStack(Material.PRISMARINE_SHARD, 1);

    public GiveKeyShardCommand() {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.addEnchant(Enchantment.getById(300), 1, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.givekeyshard")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/givekeyshard <player>";

        if (args.length < 1) {
            sender.sendMessage(usage);
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("§cYou have entered an invalid online player name.");
            return false;
        }

        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.addEnchant(Enchantment.getById(300), 1, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        target.getInventory().addItem(item);

        String name = sender instanceof Player ? sender.getName() : Console.NAME;
        SCCore.getInstance().getLogger().log(Level.INFO, target.getName() + " has received 1 Key Shard from " + name + ".");
        return false;
    }
}
