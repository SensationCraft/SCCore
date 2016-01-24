package com.sensationcraft.sccore.shop;

import com.sensationcraft.sccore.SCCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anml on 1/20/16.
 */

@Getter
public class ItemManager {

    private SCCore instance;
    private List<Item> buy;
    private List<Item> sell;

    public ItemManager(SCCore instance) {
        this.instance = instance;
        buy = new ArrayList<>();
        sell = new ArrayList<>();
    }

    public void load() {

        buy = new ArrayList<>();
        sell = new ArrayList<>();

        String path = "Shop.Items";
        FileConfiguration config = instance.getConfig();

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
                            for (Item i : buy) {
                                if (material.equals(i.getMaterial()) && b == i.getB()) put = false;
                            }
                            if (put)
                                buy.add(new Item(material, b, amount, price, category, bulk));
                        }
                    } else {
                        boolean put = true;
                        for (Item i : sell) {
                            if (material.equals(i.getMaterial()) && b == i.getB()) put = false;
                        }
                        if (put)
                            sell.add(new Item(material, b, amount, price));
                    }
                }
            }
        }
    }

    public void save() {

        String path = "Shop.Items";
        FileConfiguration config = instance.getConfig();

        if (buy.size() == 0 && sell.size() == 0)
            return;

        List<String> beingStored = new ArrayList<>();

        for (Item item : buy) {
            if (item.getMaterial() != null && item.getPrice() != 0 && item.getPrice() != 0) {
                String input = "" + item.getMaterial().getId();
                if (item.getB() != 0)
                    input += ":" + item.getB();
                input += " " + item.getAmount() + " " + item.getType() + " " + item.getPrice() + " " + item.getCategory().name() + " " + item.isBulk();

                beingStored.add(input);
            }
        }

        for (Item item : sell) {
            if (item.getMaterial() != null && item.getPrice() != 0 && item.getPrice() != 0) {
                String input = "" + item.getMaterial().getId();
                if (item.getB() != 0)
                    input += ":" + item.getB();
                input += " " + item.getAmount() + " " + item.getType() + " " + item.getPrice();

                beingStored.add(input);

            }
        }

        config.set(path, beingStored);
        instance.saveConfig();
    }

    public boolean addItem(Item item) {

        if (item.getType().equals(ItemType.BUY)) {
            if (buy.contains(item)) return false;

            for (Item i : buy) {
                if (i.getMaterial().equals(item.getMaterial()) && i.getB() == item.getB())
                    return false;
            }

            buy.add(item);
            save();
            return true;
        }

        if (item.getType().equals(ItemType.SELL)) {

            if (sell.contains(item)) return false;

            for (Item i : sell) {
                if (i.getMaterial().equals(item.getMaterial()) && i.getB() == item.getB())
                    return false;
            }

            sell.add(item);
            save();
            return true;
        }

        return false;
    }

    public Item removeItem(ItemType type, Material material, byte b) {

        if (type.equals(ItemType.SELL)) {
            for (Item i : sell) {
                if (i.getMaterial().equals(material) && i.getB() == b) {
                    sell.remove(i);
                    save();
                    return i;
                }
            }
            return null;
        }

        if (type.equals(ItemType.BUY)) {
            for (Item i : sell) {
                if (i.getMaterial().equals(material) && i.getB() == b) {
                    sell.remove(i);
                    save();
                    return i;
                }
            }
            return null;
        }

        return null;
    }

    public List<Item> getCategorialItems(ItemCategory category) {
        List<Item> categorialItems = new ArrayList<>();
        for (Item item : buy) {
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
        List<Item> categorialItems = getCategorialItems(category);

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

                if (x != (invCount - 1)) {
                    ItemStack forward = new ItemStack(Material.ARROW, 1);
                    ItemMeta fMeta = forward.getItemMeta();
                    fMeta.setDisplayName("Next Page (" + (x + 2) + "/" + invCount + ")");
                    forward.setItemMeta(fMeta);
                    inventories.get(x).setItem(50, forward);
                }
                if (x != 0) {
                    ItemStack back = new ItemStack(Material.ARROW, 1);
                    ItemMeta bMeta = back.getItemMeta();
                    bMeta.setDisplayName("Previous Page (" + (x) + "/" + invCount + ")");
                    back.setItemMeta(bMeta);
                    inventories.get(x).setItem(48, back);
                }
            }
        }

        return inventories;
    }
}
