package com.sensationcraft.sccore.factions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.sensationcraft.sccore.utils.ChunkHelper;
import com.sensationcraft.sccore.utils.ColoredParticle;
import com.sensationcraft.sccore.utils.Direction;

import lombok.Getter;

public class ParticleBorder extends BukkitRunnable{

	@Getter
	public enum BorderType{
		ENEMY(Color.RED),
		ALLY(Color.PURPLE),
		OWN(Color.GREEN),
		SAFE(Color.YELLOW),
		WAR(Color.RED),
		NONE(null);

		private final Color color;

		private BorderType(Color color) {
			this.color = color;
		}

	}

	private final Map<Direction, BorderType> borderTypes;
	private final Map<Direction, List<Location>> borderLocs;

	public ParticleBorder(Location chunkPos, Map<Direction, BorderType> borderTypes) {
		this.borderLocs = ChunkHelper.outlineChunk(chunkPos);
		this.borderTypes = borderTypes;
	}

	@Override
	public void run() {
		for(Entry<Direction, BorderType> entry:this.borderTypes.entrySet()){
			Color color = entry.getValue().getColor();
			if(color == null)
				continue;
			for(Location loc:this.borderLocs.get(entry.getKey())){
				ColoredParticle.RED_DUST.send(loc, 100, color.getRed(), color.getGreen(), color.getBlue());
			}
		}
	}

}
