package com.sensationcraft.sccore.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.earth2me.essentials.User;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.chat.commands.ShoutCommand;
import com.sensationcraft.sccore.chat.commands.StaffCommand;
import com.sensationcraft.sccore.punishments.Punishment;
import com.sensationcraft.sccore.punishments.PunishmentManager;
import com.sensationcraft.sccore.punishments.PunishmentType;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;
import com.sensationcraft.sccore.utils.FactionUtil;
import com.sensationcraft.sccore.utils.Utils;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

public class ChatListener implements Listener {

	private final Map<UUID, UUID> pm = new ConcurrentHashMap<>();
	private final Map<String, String> cmds = new HashMap<String, String>();
	private final String to = "&6[me -> %s&6]&r %s".replace('&',
			ChatColor.COLOR_CHAR);
	private final String from = "&6[%s&6 -> me]&r %s".replace('&',
			ChatColor.COLOR_CHAR);
	private final String ss = "&a[SPY] &6[%s -> %s]&r %s";
	private final String me = "&5* %s %s".replace('&', ChatColor.COLOR_CHAR);
	private final String at = "@%s";

	private final SCCore instance;
	private final ShoutCommand shout;
	private final Utils utils;
	private final SCPlayerManager playerManager;
	private PunishmentManager punishmentManager;
	private PacketAdapter commandFilter;
	private Set<UUID> fchatspy = Collections.synchronizedSet(new HashSet<>());
	private Set<UUID> lchatspy = Collections.synchronizedSet(new HashSet<>());

	{
		this.cmds.put("m", "@");
		this.cmds.put("t", "@");
		this.cmds.put("w", "@");
		this.cmds.put("tell", "@");
		this.cmds.put("whisper", "@");
		this.cmds.put("msg", "@");
		this.cmds.put("r", "@r ");
		this.cmds.put("emsg", "@");
		this.cmds.put("etell", "@");
		this.cmds.put("ewhisper", "@");
		this.cmds.put("er", "@r ");
		this.cmds.put("ereply", "@r ");
		this.cmds.put("reply", "@r ");
		this.cmds.put("s", "@s ");
		this.cmds.put("S", "@s ");
		this.cmds.put("shout", "@s ");
		this.cmds.put("me", "!");
		this.cmds.put("eme", "!");
		this.cmds.put("bukkit:me", "!");
	}

	public ChatListener(SCCore instance) {
		this.shout = new ShoutCommand(instance);
		this.instance = instance;
		this.playerManager = this.instance.getSCPlayerManager();
		this.punishmentManager = instance.getPunishmentManager();
		this.utils = instance.getUtils();
		this.commandFilter = new PacketAdapter(this.instance,
				PacketType.Play.Client.CHAT) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.CHAT) {
					StructureModifier<String> strings = event.getPacket().getStrings();
					String msg = strings.read(0);
					if (msg.startsWith("/")) {
						final String msg2 = msg.toLowerCase();
						if (msg2.equals("/f c") || msg2.equals("/f chat") || msg2.startsWith("/f chat ") || msg2.startsWith("/f chat ")) {
							String[] cmd = msg2.split(" ");
							if (cmd.length >= 3) {
								char c = cmd[2].charAt(0);
								if (c == 'l')
									c = 'p';
								strings.write(0, "/c " + c);
							} else {
								MPlayer mPlayer = MPlayerColl.get().get(event.getPlayer());
								strings.write(0, "/c " + ChatListener.this.playerManager.getSCPlayer(event.getPlayer().getUniqueId())
								.getChannel().next((mPlayer != null && mPlayer.hasFaction()) ? ChatChannel.NOT_NONE : ChatChannel.NOT_FACTION_NOT_NONE).getCode());
							}
						} else if (msg2.startsWith("/f chatspy")
								&& event.getPlayer().hasPermission(
										"factions.chatspy"))
							strings.write(0, "@fchatspy");
						else {
							msg = msg.substring(1);
							String[] cmd = msg.split(" ", 2);
							cmd[0] = cmd[0].toLowerCase();
							if (ChatListener.this.cmds.containsKey(cmd[0]) && (cmd.length > 1))
								strings.write(0, ChatListener.this.cmds.get(cmd[0]) + cmd[1]);
						}
					}
				}
			}
		};
		ProtocolLibrary.getProtocolManager().addPacketListener(this.commandFilter);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onChat(final AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		Player player = event.getPlayer();
		User euser = this.instance.getEssentials().getUser(player);
		if (message.startsWith("@")) {
			event.setCancelled(true);
			if (message.startsWith("@fchatspy") && player.hasPermission("sccore.factionsspy")) {
				if (this.fchatspy.contains(player.getUniqueId())) {
					this.fchatspy.remove(player.getUniqueId());
					player.sendMessage(ChatColor.RED + "Factions Chatspy disabled!");
				} else {
					this.fchatspy.add(player.getUniqueId());
					player.sendMessage(ChatColor.GREEN + "Factions Chatspy enabled!");
				}
				return;
			}
			if (message.startsWith("@lchatspy") && player.hasPermission("sccore.localspy")) {
				if (this.lchatspy.contains(player.getUniqueId())) {
					this.lchatspy.remove(player.getUniqueId());
					player.sendMessage(ChatColor.RED + "Local Chatspy disabled!");
				} else {
					this.lchatspy.add(player.getUniqueId());
					player.sendMessage(ChatColor.GREEN + "Local Chatspy enabled!");
				}
				return;
			}
			if (!message.contains(" ")) {
				player.sendMessage("@<player> <message>");
				return;
			}
			Player other = null;
			if (message.startsWith("@r ")) {
				// fetch other;
				UUID r = this.pm.containsKey(player.getUniqueId()) ? this.pm.get(player.getUniqueId()) : null;
				if (r == null) {
					player.sendMessage(ChatColor.DARK_RED + "You don't have anyone to reply to.");
					return;
				}
				other = Bukkit.getPlayer(r);
				if (other == null)
					player.sendMessage(ChatColor.DARK_RED + "You don't have anyone to reply to.");
			} else if (message.startsWith("@s ")) {
				this.shout.onCommand(player, null, "s", message.substring(3).split(" "));
				/*if (this.shoutkill.get()
                        && !player.hasPermission("shout.bypass.kill"))
				{
					player.sendMessage(ChatColor.DARK_RED + "The shout chat has been silenced");
					return;
				}

				if (this.hasCooldown(player))
				{
					player.sendMessage(ChatColor.DARK_RED + String.format("You must wait %d between shouts.", (int) (this.SHOUT_COOLDOWN / 1000)));
					return;
				}

				String title = "";
				String rank = this.titles.containsKey(player.getName()) ? this.titles.get(player.getName()) : "";
				if (rank.isEmpty())
					rank = this.pvptitles.containsKey(player.getName()) ? this.pvptitles.get(player.getName()) : "";
					if (!rank.isEmpty())
						if (player.hasPermission("shout.staff"))
							title = String.format(this.st, rank);
						else
							title = String.format(this.t, rank);
					String m = String.format(this.global, title, this.getGlobalTag(player), event.getMessage().substring(3));
					if (player.hasPermission("shout.colors"))
						m = ChatColor.translateAlternateColorCodes('&', m);
					this.getPlugin().getLogger().info(ChatColor.stripColor(m));
					for (val o : event.getRecipients())
						o.sendMessage(m);
					return;*/
			} else {
				String user = message.substring(1, message.indexOf(" "));
				List<Player> matches = Bukkit.matchPlayer(user);
				if (matches.isEmpty())
					player.sendMessage(ChatColor.DARK_RED + "Player not found!");
				else if (matches.size() > 1)
					player.sendMessage(ChatColor.DARK_RED + "Multiple players found!");
				else {
					other = matches.get(0);
					if (!player.canSee(other)) {
						other = null;
						player.sendMessage(ChatColor.DARK_RED + "Player not found!");
					}
				}
			}
			if (other != null) {
				User eother = this.instance.getEssentials().getUser(other);
				if (eother.isIgnoredPlayer(euser))
					return;
				this.pm.put(player.getUniqueId(), other.getUniqueId());
				this.pm.put(other.getUniqueId(), player.getUniqueId());
				String mes = message.substring(message.indexOf(" ") + 1);
				player.sendMessage(String.format(this.to, other.getDisplayName(), mes));
				other.sendMessage(String.format(this.from, player.getDisplayName(), mes));
				mes = String.format(this.ss, player.getName(), other.getName(), mes);
				this.instance.getLogger().info(mes);
				if (player.hasPermission("essentials.socialspy.exempt") && other.hasPermission("essentials.socialspy.exempt"))
					return;
				User espy;
				for (Player spy : Bukkit.getOnlinePlayers()) {
					if ((spy == player) || (spy == other))
						continue;
					espy = this.instance.getEssentials().getUser(spy);
					if (espy != null && espy.isSocialSpyEnabled())
						spy.sendMessage(mes);
				}
			}
		} else if (message.startsWith("!") && player.hasPermission("essentials.me")) {
			event.setCancelled(true);
			/*if (this.shoutkill.get() && !player.hasPermission("shout.bypass.kill"))
            {
				player.sendMessage(ChatColor.DARK_RED + "The shout chat has been silenced");
				return;
			}*/
			message = String.format(this.me, player.getDisplayName(), message.substring(1));
			this.instance.getLogger().info(String.format("emote: %s", ChatColor.stripColor(message)));
			for (Player other : event.getRecipients())
				other.sendMessage(message);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onLateChat(final AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		SCPlayer user = this.playerManager.getSCPlayer(player.getUniqueId());
		MPlayer mPlayer = MPlayerColl.get().get(player);
		event.setCancelled(true);
		switch (user.getChannel()) {
		case FACTION:
			List<MPlayer> faction = MPlayerColl.get().getAll(type -> type.getFactionId().equals(mPlayer.getFactionId()));
			FancyMessage message = new FancyMessage(FactionUtil.getAsteriskPrefix(mPlayer)).color(ChatColor.GREEN).then(user.getTag()).color(ChatColor.GREEN)
					.tooltip(user.getHoverText()).then(": " + event.getMessage()).color(ChatColor.GREEN);
			for (MPlayer other : faction)
				if (other.getSender() != null)
					message.send(other.getSender());
			for (UUID id : this.fchatspy)
				message.send(Bukkit.getPlayer(id));
			break;
		case ALLY:
			List<MPlayer> ally = MPlayerColl.get().getAll(type -> type.getRelationTo(type).isFriend());
			FancyMessage message2 = new FancyMessage("[").color(ChatColor.DARK_PURPLE).then(mPlayer.getFactionName()).color(ChatColor.DARK_PURPLE)
					.then("] " + FactionUtil.getAsteriskPrefix(mPlayer)).color(ChatColor.DARK_PURPLE).then(user.getTag()).color(ChatColor.DARK_PURPLE)
					.tooltip(user.getHoverText()).then(": " + event.getMessage()).color(ChatColor.DARK_PURPLE);
			for (MPlayer other : ally)
				if (other.getSender() != null)
					message2.send(other.getSender());
			for (UUID id : this.fchatspy)
				message2.send(Bukkit.getPlayer(id));
			break;
		case PUBLIC:
			List<Punishment> punishments = this.punishmentManager.getPunishments(player.getUniqueId());

			synchronized (punishments) {
				for (Punishment punishment : punishments) {
					if (punishment.getType().equals(PunishmentType.MUTE)) {
						if (!punishment.hasExpired()) {
							player.sendMessage("§cYou are permanently muted.");
							event.setCancelled(true);
							return;
						}
					}

					if (punishment.getType().equals(PunishmentType.TEMPMUTE)) {
						if (!punishment.hasExpired()) {
							player.sendMessage(
									"§cYou are temporarily muted until §3" + this.utils.getDifference(System.currentTimeMillis(), punishment.getCreated() + punishment.getExpires()) + " §c.");
							event.setCancelled(true);
							return;
						}
					}
				}
			}
			event.setCancelled(true);

			if (player.isOp()) event.setMessage(event.getMessage().replace('&', ChatColor.COLOR_CHAR));

			FancyMessage message3 = new FancyMessage(" - ").color(ChatColor.GRAY).then(user.getTag()).tooltip(user.getHoverText())
					.then(": ").color(ChatColor.DARK_GRAY).then(event.getMessage()).color(ChatColor.GRAY);

			for (final Player other : event.getRecipients()) {
				if (other.getWorld() != player.getWorld())
					continue;
				if (other.getLocation().distanceSquared(player.getLocation()) <= 900 || this.lchatspy.contains(other.getUniqueId()))
					message3.send(other);
			}
			break;
		case SHOUT:
			this.shout.onCommand(player, null, "s", event.getMessage().split(" "));
			break;
		case STAFF:
			StaffCommand cmd = new StaffCommand(this.instance);
			cmd.onCommand(player, null, "st", event.getMessage().split(" "));
			break;
		default:
			player.sendMessage(ChatColor.DARK_RED + "Oops something went wrong... try relogging.");
		}
	}

	@EventHandler
	public void onTab(final PlayerChatTabCompleteEvent event) {
		String msg = event.getChatMessage();

		if (msg.startsWith("@") && !msg.contains(" ")) {
			Player player = event.getPlayer();
			String search = msg.substring(1);
			List<Player> matches = Bukkit.matchPlayer(search);
			List<String> hits = new ArrayList<String>();
			for (Player other : matches)
				if ((other != null) && player.canSee(other))
					hits.add(String.format(this.at, other.getName()));
			event.getTabCompletions().clear();
			event.getTabCompletions().addAll(hits);
		}
	}

}
