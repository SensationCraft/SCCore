package org.sensationcraft.sccore.duels;

import org.bukkit.Location;
import org.sensationcraft.sccore.SCCore;

/**
 * Created by Anml on 1/3/16.
 */
public class ArenaManager {

	private SCCore instance;
	private Arena arena;

	public ArenaManager(SCCore instance) {
		this.instance = instance;
		this.arena = new Arena(instance);
	}

	public Arena getArena() {
		return this.arena;
	}

	public boolean insideBorders(Location location) {

		if (!this.arena.allValidLocations())
			return false;

		Location primaryCorner = this.arena.getLocation(ArenaLocationType.PrimaryCorner);
		Location secondaryCorner = this.arena.getLocation(ArenaLocationType.SecondaryCorner);

		int x1 = Math.min(primaryCorner.getBlockX(), secondaryCorner.getBlockX());
		int y1 = Math.min(primaryCorner.getBlockY(), secondaryCorner.getBlockY());
		int z1 = Math.min(primaryCorner.getBlockZ(), secondaryCorner.getBlockZ());
		int x2 = Math.max(primaryCorner.getBlockX(), secondaryCorner.getBlockX());
		int y2 = Math.max(primaryCorner.getBlockY(), secondaryCorner.getBlockY());
		int z2 = Math.max(primaryCorner.getBlockZ(), secondaryCorner.getBlockZ());
		Location primary = new Location(primaryCorner.getWorld(), x1, y1, z1);
		Location secondary = new Location(primaryCorner.getWorld(), x2, y2, z2);

		return location.getBlockX() >= primary.getBlockX() && location.getBlockX() <= secondary.getBlockX()
				&& location.getBlockY() >= primary.getBlockY() && location.getBlockY() <= secondary.getBlockY()
				&& location.getBlockZ() >= primary.getBlockZ() && location.getBlockZ() <= secondary.getBlockZ();
	}
}
