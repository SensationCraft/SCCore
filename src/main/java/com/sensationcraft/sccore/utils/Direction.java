package com.sensationcraft.sccore.utils;

import org.bukkit.Location;

public enum Direction {

	UP(0, 1, 0),
	DOWN(0, -1, 0),
	EAST(1, 0, 0),
	WEST(-1 ,0 ,0),
	NORTH(0, 0, -1),
	SOUTH(0, 0, 1);

	private final int x, y, z;

	private Direction(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location step(Location loc){
		return loc.add(this.x, this.y, this.z);
	}

}
