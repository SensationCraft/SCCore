package com.sensationcraft.sccore.help;

import com.sensationcraft.sccore.SCCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Anml on 1/31/16.
 */

@Getter
public class TutorialManager implements Listener {

    private SCCore instance;
    private FileConfiguration config;
    private Map<UUID, Location> tutorialedPlayers;
    private Inventory inventory;

    public TutorialManager(SCCore instance) {
        this.instance = instance;
        config = instance.getConfig();

        createInventory();
        tutorialedPlayers = new HashMap<>();
    }

    public void start(Player player) {
        UUID uuid = player.getUniqueId();

        if (getSpawnLocation() == null) {
            player.sendMessage("§cThe spawn location for the tutorial has not been set.");
            return;
        }

        Location location = player.getLocation();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.getUniqueId().equals(uuid))
                player.hidePlayer(target);
        }

        player.teleport(getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        player.sendMessage("§aYou have started the tutorial.");

        tutorialedPlayers.put(uuid, location);
    }

    public void remove(Player player) {
        if (!tutorialedPlayers.containsKey(player.getUniqueId())) {
            player.sendMessage("§cYou are not currently viewing the tutorial.");
            return;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.getName() != target.getName())
                player.showPlayer(target);
        }

        Location location = tutorialedPlayers.get(player.getUniqueId());

        tutorialedPlayers.remove(player.getUniqueId());

        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.sendMessage("§aYou have completed the tutorial, and now may resume gameplay as usual.");

    }

    public Location getSpawnLocation() {
        String path = "Tutorial.SpawnLocation";
        if (!this.config.contains(path)) {
            return null;
        } else {
            String[] loc = this.config.getString(path).split(",");
            try {
                World w = Bukkit.getWorld(loc[0]);
                Double x = Double.parseDouble(loc[1]);
                Double y = Double.parseDouble(loc[2]);
                Double z = Double.parseDouble(loc[3]);
                float yaw = Float.parseFloat(loc[4]);
                float pitch = Float.parseFloat(loc[5]);
                Location location = new Location(w, x, y, z, yaw, pitch);
                return location;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void setSpawnLocation(Location loc) {
        String path = "Tutorial.SpawnLocation";

        if (loc == null) {
            this.config.set(path, null);
            this.instance.saveConfig();
            return;
        }

        String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        this.config.set(path, location);
        this.instance.saveConfig();
    }

    private void createInventory() {
        inventory = Bukkit.createInventory(null, 9, "§b§lHelp Menu");

        ItemStack item = new ItemStack(Material.LEASH, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.getById(300), 1, true);
        meta.setDisplayName("§6§lWalkthrough Tutorial");
        meta.setLore(Arrays.asList("§a§l-----------------", "          §eClick to", "   §ebegin the tutorial.", "§a§l-----------------"));
        item.setItemMeta(meta);
        inventory.setItem(3, item);

        item = new ItemStack(Material.BOOK, 1);
        meta = item.getItemMeta();
        meta.addEnchant(Enchantment.getById(300), 1, true);
        meta.setDisplayName("   §6§lText-Based Help");
        meta.setLore(Arrays.asList("§a§l------------------", "  §eDoes the same as the", "§eregular '/help' command.", "§a§l------------------"));
        item.setItemMeta(meta);
        inventory.setItem(5, item);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPre(PlayerCommandPreprocessEvent e) {
        if (tutorialedPlayers.containsKey(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage("§cYou are not permitted to execute commands while in tutorial mode.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (UUID uuid : tutorialedPlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null)
                player.hidePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (tutorialedPlayers.containsKey(e.getPlayer().getUniqueId())) {
            remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (tutorialedPlayers.containsKey(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage("§cYou are not permitted to teleport out while in tutorial mode.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack stack = e.getCurrentItem();

        if (!e.getInventory().getName().equalsIgnoreCase("§b§lHelp Menu"))
            return;

        e.setCancelled(true);

        if (stack == null || stack.getType().equals(Material.AIR))
            return;

        if (stack.getType().equals(Material.LEASH)) {
            if (!tutorialedPlayers.containsKey(player.getUniqueId()))
                start(player);
            else
                player.sendMessage("§cYou are already currently in tutorial mode.");
            player.closeInventory();

        }

        if (stack.getType().equals(Material.BOOK)) {
            player.performCommand("essentials:help 1");
            player.closeInventory();
            return;
        }
    }
}
