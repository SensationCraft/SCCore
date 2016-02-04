package com.sensationcraft.sccore.utils;

import com.massivecraft.factions.FPlayer;

public class FactionUtil {

	public static String getAsteriskPrefix(FPlayer player) {
		if (!player.hasFaction())
			return "";
		player.getChatTag();
		return player.getRole().getPrefix();
	}

}
