package org.sensationcraft.sccore.ranks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by kishanpatel on 12/6/15.
 */

@Getter
@RequiredArgsConstructor
public enum Rank {

	OWNER(9, "§6Owner", "§6%s", 100),
	DEV(8, "§6Dev", "§6%s", 100),
	ADMINPLUS(7, "§4Admin§e+", "Admin+", "§4%s§e+", 100),
	ADMIN(6, "§4Admin", "§4%s", 100),
	MOD(5, "§1Mod", "§1%s", 80),

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
	//Dangerous, may not be defined when this class is initialized (on first reference), taking it out since not needed elsewhere
	//final SCPlayerManager scPlayerManager = SCCore.getInstance().getSCPlayerManager();

	Rank(int id, String name, String tag, int lockpickChance) {
		this.id = id;
		this.name = name;
		this.alias = this.name();
		this.tag = tag;
		this.lockpickChance = lockpickChance;
	}

}
