package com.sensationcraft.sccore.shop;

import com.sensationcraft.sccore.SCCore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anml on 1/20/16.
 */

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
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (material != null && type != null && amount != 0 && price != 0) {
                    if (type.equals(ItemType.BUY)) {
                        boolean put = true;
                        for (Item i : sell) {
                            if (material.equals(i.getMaterial())) put = false;
                        }
                        if (put)
                            buy.add(new Item(material, b, amount, type, price));
                    } else {
                        boolean put = true;
                        for (Item i : sell) {
                            if (material.equals(i.getMaterial())) put = false;
                        }
                        if (put)
                            sell.add(new Item(material, b, amount, type, price));
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
                input += " " + item.getAmount() + " " + item.getType() + " " + item.getPrice();

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


    public SCCore getInstance() {
        return this.instance;
    }

    public List<Item> getBuy() {
        return this.buy;
    }

    public List<Item> getSell() {
        return this.sell;
    }
}
