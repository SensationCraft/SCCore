package com.sensationcraft.sccore.factions;

import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.sensationcraft.sccore.factions.ParticleBorder.BorderType;
import com.sensationcraft.sccore.utils.Direction;

public class BorderManager implements Listener{

	private final Map<Chunk, ParticleBorder> borders = new MapMaker().weakKeys().makeMap();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e){
		int x = e.getChunk().getX();
		int z = e.getChunk().getZ();
		World world = e.getWorld();
		Map<Direction, BorderType> types = Maps.newEnumMap(Direction.class);
		types.put(Direction.EAST, BorderType.OWN);
		types.put(Direction.WEST, BorderType.OWN);
		types.put(Direction.NORTH, BorderType.OWN);
		types.put(Direction.SOUTH, BorderType.OWN);
		Faction fac = Board.getInstance().getFactionAt(new FLocation(e.getChunk().getBlock(0, 0, 0)));
		if(fac != null && !fac.isWilderness()){
			if(world.isChunkLoaded(x+1, z)){
				
			}

			if(world.isChunkLoaded(x, z+1)){

			}

			if(world.isChunkLoaded(x-1, z)){

			}

			if(world.isChunkLoaded(x, z-1)){

			}
		}

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent e){
		ParticleBorder border = this.borders.remove(e.getChunk());
		if(border != null)
			border.cancel();
	}

}
