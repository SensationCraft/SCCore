package com.sensationcraft.sccore.utils;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;

public class FactionUtil {

	public static String getAsteriskPrefix(MPlayer player){
		if(!player.hasFaction())
			return "";
		return player.getRole() == Rel.LEADER ? "**":player.getRole() == Rel.OFFICER ? "*":"";
	}

}
