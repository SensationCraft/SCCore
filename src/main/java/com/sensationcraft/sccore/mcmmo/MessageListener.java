package com.sensationcraft.sccore.mcmmo;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.sensationcraft.sccore.utils.fanciful.Reflection;
import org.bukkit.plugin.Plugin;

public class MessageListener extends PacketAdapter{

	private static final String MCMMO_PLAYER_CLASS = "com.gmail.nossr50.datatypes.player.McMMOPlayer";
	private static final String MCMMO_MMOEDIT_CLASS = "com.gmail.nossr50.commands.experience.MmoeditCommand";
	private static final String MCMMO_TOOLLOWER_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";

	public MessageListener(Plugin plugin) {
		super(plugin, PacketType.Play.Server.CHAT);
	}

	@Override
	public void onPacketSending(final PacketEvent event){
		StackTraceElement stacktrace = Reflection.findCallingClass("sendMessage", MinecraftReflection.getCraftPlayerClass().getName());
		if(stacktrace != null){
			//this.plugin.getLogger().info(String.valueOf(stacktrace));
			if(stacktrace.getClassName().equals(MessageListener.MCMMO_PLAYER_CLASS) || stacktrace.getClassName().equals(MessageListener.MCMMO_MMOEDIT_CLASS)  || stacktrace.getClassName().equals(MessageListener.MCMMO_TOOLLOWER_CLASS)){
				this.plugin.getLogger().info("Reidrected message from McMMO to saddle text for player "+event.getPlayer().getDisplayName());
				event.getPacket().getBytes().write(0, (byte) 2);
			}
		}

	}

}
