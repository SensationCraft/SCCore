package org.sensationcraft.sccore.stats;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Anml on 1/12/16.
 */
@Getter
@RequiredArgsConstructor
public enum Stat {

	KILLS("Kills"),
	DEATHS("Deaths"),
	WINS("Wins"),
	LOSSES("Losses");

	private final String name;
}
