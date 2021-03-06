package com.sensationcraft.sccore.stats;

import lombok.Getter;

/**
 * Created by Anml on 1/12/16.
 */
@Getter
public enum Stat {

	KILLS("Kills"),
	DEATHS("Deaths"),
	WINS("Wins"),
	LOSSES("Losses");

	private String name;

	Stat(String name) {
		this.name = name;
	}

}
