package com.sensationcraft.sccore.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

/**
 * Created by Anml on 1/20/16.
 */

@Getter
public class Item {

	int amount;
	private Material material;
	private byte b;
	private ItemType type;
	private double price;
	private ItemCategory category;
	private boolean bulk;

	public Item(Material material, byte b, int amount, double price) {
		this.material = material;
		this.b = b;
		this.amount = amount;
		this.type = ItemType.SELL;
		this.price = price;
		this.category = null;
		this.bulk = false;
	}

	public Item(Material material, byte b, int amount, double price, ItemCategory category, boolean bulk) {
		this.material = material;
		this.b = b;
		this.amount = amount;
		this.type = ItemType.BUY;
		this.price = price;
		this.category = category;
		this.bulk = bulk;
	}

	public ItemStack getItemStack() {
		return new ItemStack(this.material, 1, this.b);
	}
}
