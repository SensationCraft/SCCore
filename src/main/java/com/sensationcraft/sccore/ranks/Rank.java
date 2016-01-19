package com.sensationcraft.sccore.ranks;

import lombok.Getter;

/**
 * Created by kishanpatel on 12/6/15.
 */

@Getter
public enum Rank {

	OWNER(9, "§6Owner", "§6%s", 100),
	DEV(8, "§6Dev", "Developer", "§6%s", 100),
	ADMINPLUS(7, "§4Admin§e+", "Admin+", "§4%s§e+", 100),
	ADMIN(6, "§4Admin", "Administrator", "§4%s", 100),
	MOD(5, "§3Mod", "Moderator", "§3%s", 80),

	PREMIUMPLUS(4, "§9Premium§e+", "Premium+", "§9%s§e+", 80),
	PREMIUM(3, "§9Premium", "§9%s", 60),
	VIPPLUS(2, "§aVIP§e+", "VIP+", "§a%s§e+", 40),
	VIP(1, "§aVIP", "§a%s", 25),

	DEFAULT(0, "§fDefault", "§f%s", 10);

	private final int id;
	private final String name;
	private final String alias;
	private final String tag;
	private final int lockpickChance;

	Rank(int id, String name, String tag, int lockpickChance) {
		this.id = id;
		this.name = name;
		this.alias = this.name();
		this.tag = tag;
		this.lockpickChance = lockpickChance;
}

	Rank(int id, String name, String alias, String tag, int lockpickChance) {
		this.id = id;
		this.name = name;
		this.alias = alias;
		this.tag = tag;
		this.lockpickChance = lockpickChance;
	}

}
