package com.sensationcraft.sccore.mcmmo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sensationcraft.sccore.utils.fanciful.Reflection;

public class MessageListener extends PacketAdapter {

	private static final String MCMMO_PACKAGE = "com.gmail.nossr50.";

	private static final String MCMMO_PLAYER_CLASS = "com.gmail.nossr50.datatypes.player.McMMOPlayer";
	private static final String MCMMO_MMOEDIT_CLASS = "com.gmail.nossr50.commands.experience.MmoeditCommand";
	private static final String MCMMO_TOOLLOWER_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";
	private static final String MCMMO_ABILITYENABLE_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";
	private static final String MCMMO_ABILITYDISABLE_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";
	private static final String MCMMO_APRIL_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";
	private static final String MCMMO_BLEEDTIMER_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";
	private static final String MCMMO_KRAKENATTACK_CLASS = "com.gmail.nossr50.runnables.skills.ToolLowerTask";

	private static final List<String> MCMMO_CLASSES = Lists.newArrayList(MessageListener.MCMMO_PLAYER_CLASS, MessageListener.MCMMO_MMOEDIT_CLASS, MessageListener.MCMMO_TOOLLOWER_CLASS, MessageListener.MCMMO_ABILITYENABLE_CLASS,
			MessageListener.MCMMO_ABILITYDISABLE_CLASS, MessageListener.MCMMO_APRIL_CLASS, MessageListener.MCMMO_BLEEDTIMER_CLASS, MessageListener.MCMMO_KRAKENATTACK_CLASS);

	private final JsonParser parser = new JsonParser();
	private final Map<UUID, BukkitTask> packetRefreshers = Maps.newHashMap();

	public MessageListener(Plugin plugin) {
		super(plugin, PacketType.Play.Server.CHAT);
	}

	@Override
	public void onPacketSending(final PacketEvent event) {
		StackTraceElement stacktrace = Reflection.findCallingClass("sendMessage", MinecraftReflection.getCraftPlayerClass().getName());
		if (stacktrace != null && stacktrace.getClassName().startsWith(MessageListener.MCMMO_PACKAGE)) {
			//this.plugin.getLogger().info(String.valueOf(stacktrace));
			if (MessageListener.MCMMO_CLASSES.contains(stacktrace.getClassName())) {
				WrappedChatComponent comp = event.getPacket().getChatComponents().read(0);
				JsonObject obj = this.parser.parse(comp.getJson()).getAsJsonObject();
				JsonObject extra = obj.get("extra").getAsJsonArray().get(0).getAsJsonObject();
				if (extra.has("color")) {
					PacketContainer newPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT);
					newPacket.getChatComponents().write(0, WrappedChatComponent.fromText(ChatColor.valueOf(extra.get("color").getAsString().toUpperCase()) + extra.get("text").getAsString()));
					newPacket.getBytes().write(0, (byte) 2);
					event.setPacket(newPacket);
					Player player = event.getPlayer();
					BukkitTask previous = this.packetRefreshers.remove(player.getUniqueId());
					if (previous != null)
						previous.cancel();
					BukkitRunnable br = new BukkitRunnable() {

						private int runs = 0;

						@Override
						public void run() {
							if (!player.isOnline() || this.runs++ < 5) {
								try {
									ProtocolLibrary.getProtocolManager().sendServerPacket(player, newPacket);
								} catch (InvocationTargetException e) {
									MessageListener.this.plugin.getLogger().severe("Error refreshing saddle text for " + player.getDisplayName());
									e.printStackTrace();
								}
							} else {
								this.cancel();
								MessageListener.this.packetRefreshers.remove(player.getUniqueId());
							}
						}
					};
					this.packetRefreshers.put(player.getUniqueId(), br.runTaskTimer(this.plugin, 20L, 20L));
					//this.plugin.getLogger().info(newPacket.getChatComponents().read(0).getJson());
					//this.plugin.getLogger().info("Redirected message from McMMO to saddle text for player "+event.getPlayer().getDisplayName());
				}
			}
		}

	}

}
