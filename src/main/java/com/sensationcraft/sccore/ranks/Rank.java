package com.sensationcraft.sccore.ranks;

import lombok.Getter;

/**
 * Created by kishanpatel on 12/6/15.
 */

@Getter
public enum Rank {

    OWNER(10, "§6Owner", "§6%s", 100, 2.0),
    DEV(9, "§6Dev", "Developer", "§6%s", 100, 2.0),
    ADMINPLUS(8, "§4Admin§e+", "Admin+", "§4%s§e+", 100, 2.0),
    ADMIN(7, "§4Admin", "Administrator", "§4%s", 100, 2.0),
    MOD(6, "§5Mod", "Moderator", "§5%s", 80, 2.0),

    YOUTUBER(5, "§cYoutuber", "Youtube", "§c%s", 80, 2.0),

    PREMIUMPLUS(4, "§9Premium§e+", "Premium+", "§9%s§e+", 80, 2.0),
    PREMIUM(3, "§9Premium", "§9%s", 60, 1.75),
    VIPPLUS(2, "§aVIP§e+", "VIP+", "§a%s§e+", 40, 1.50),
    VIP(1, "§aVIP", "§a%s", 25, 1.25),

    DEFAULT(0, "§fDefault", "§f%s", 10, 1);

    private final int id;
    private final String name;
    private final String alias;
    private final String tag;
    private final int lockpickChance;
    private final double sellBoost;

    Rank(int id, String name, String tag, int lockpickChance, double sellBoost) {
        this.id = id;
        this.name = name;
        this.alias = this.name();
        this.tag = tag;
        this.lockpickChance = lockpickChance;
        this.sellBoost = sellBoost;
    }

    Rank(int id, String name, String alias, String tag, int lockpickChance, double sellBoost) {
        this.id = id;
        this.name = name;
        this.alias = alias;
        this.tag = tag;
        this.lockpickChance = lockpickChance;
        this.sellBoost = sellBoost;
    }
}
