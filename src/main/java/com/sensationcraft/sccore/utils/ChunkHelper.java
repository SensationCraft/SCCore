package com.sensationcraft.sccore.utils;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ChunkHelper {

	private static Map<Direction, List<Location>> relativeLocs = Maps.newEnumMap(Direction.class);

	static {
		Location current = new Location(null, 0, 0, 0);
		List<Location> locs = Lists.newArrayList();
		for(int i = 0; i < 16; i++){
			locs.add(current);
			current = Direction.EAST.step(current.clone());
		}

		for(int i = 1; i < 16; i++){
			current = Direction.SOUTH.step(current.clone());
			locs.add(current);
		}

		for(int i = 1; i < 16; i++){
			current = Direction.WEST.step(current.clone());
			locs.add(current);
		}

		for(int i = 1; i < 15; i++){
			current = Direction.NORTH.step(current.clone());
			locs.add(current);
		}
	}

	public static Map<Direction, List<Location>> outlineChunk(Location chunkLoc){
		Map<Direction, List<Location>> locs = Maps.newEnumMap(ChunkHelper.relativeLocs);
		World world = chunkLoc.getWorld();
		for(List<Location> list:locs.values()){
			ListIterator<Location> lit = list.listIterator();
			Location loc = lit.next().clone();
			loc.setWorld(world);
			loc.setY(world.getHighestBlockYAt(loc)+1);
			lit.set(loc.add(chunkLoc));
		}
		return locs;
	}

}
