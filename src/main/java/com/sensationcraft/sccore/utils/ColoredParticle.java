package com.sensationcraft.sccore.utils;

import org.bukkit.Location;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

public enum ColoredParticle {

	RED_DUST(30);

	private final int id;

	private ColoredParticle(int id) {
		this.id = id;
	}

	public void send(Location location, int distance, int r, int g, int b) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WORLD_PARTICLES);
		StructureModifier<Integer> ints = packet.getIntegers().writeDefaults();
		StructureModifier<Float> floats = packet.getFloat().writeDefaults();
		packet.getBooleans().writeDefaults();
		ints.write(0, this.id).write(1, 1);
		floats.write(0, Float.valueOf(location.getBlockX())).write(0, Float.valueOf(location.getBlockY()))
		.write(0, Float.valueOf(location.getBlockZ())).write(0, Float.valueOf(r))
		.write(0, Float.valueOf(g)).write(0, Float.valueOf(b));
		packet.getIntegers().write(0, r).write(1, g).write(2, b);
		ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet, location, distance);
	}

}
