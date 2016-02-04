package com.sensationcraft.sccore.chat.factions;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.MapMaker;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Role;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.scplayer.SCPlayer;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

import lombok.val;

public class ConcurrentFPlayer
{
	static final Set<Player> spies = Collections.newSetFromMap(new MapMaker().weakKeys().concurrencyLevel(4).<Player, Boolean>makeMap());
	static final String aspy = "[" + ChatColor.LIGHT_PURPLE + "%s" + ChatColor.RESET + "]";
	static final String fspy = "[" + ChatColor.GREEN + "%s" + ChatColor.RESET + "]";

	public static void noSpies() {
		ConcurrentFPlayer.spies.clear();
	}

	public static void removeSpy(final Player player) {
		ConcurrentFPlayer.spies.remove(player);
		player.sendMessage(ChatColor.RED + "Factions Chatspy disabled!");
	}

	public static void toggleSpy(final Player player) {
		if (!ConcurrentFPlayer.spies.contains(player))
			ConcurrentFPlayer.addSpy(player);
		else
			ConcurrentFPlayer.removeSpy(player);
	}

	public static void addSpy(final Player player) {
		ConcurrentFPlayer.spies.add(player);
		player.sendMessage(ChatColor.GREEN + "Factions Chatspy enabled!");
	}
	private final AtomicReference<String> title = new AtomicReference<String>("");

	private final AtomicReference<String> factionId = new AtomicReference<String>("");

	private final AtomicReference<Role> role = new AtomicReference<Role>(Role.NORMAL);

	private WeakReference<Player> player;

	private WeakReference<SCPlayer> scPlayer;

	private final UUID ID;

	public ConcurrentFPlayer(final FPlayer fme)
	{
		this.setTitle(fme.getTitle());
		this.player = new WeakReference<Player>(fme.getPlayer());
		this.ID = fme.getPlayer().getUniqueId();
		this.factionId.set(fme.getFactionId());
		this.role.set(fme.getRole());
		this.scPlayer = new WeakReference<SCPlayer>(SCCore.instance.getSCPlayerManager().getSCPlayer(this.ID));
	}

	public final void setTitle(final String title)
	{
		this.title.set(title);
	}

	public String getTitle()
	{
		return String.format("%s ", this.title.get());
	}

	public void leaveFaction()
	{
		val prev = this.factionId.getAndSet(Factions.getInstance().getNone().getId());
		val cfac = FactionsListener.getFaction(prev != null ? prev:"");
		if(cfac != null)
			cfac.removePlayer(this);
		this.role.set(Role.NORMAL);
		this.title.set("");
	}

	public void joinFaction(final ConcurrentFaction fac)
	{
		val prev = this.factionId.getAndSet(fac.getId());
		val cfac = FactionsListener.getFaction(prev != null ? prev:"");
		if(cfac != null)
			cfac.removePlayer(this);
		fac.addPlayer(this);
	}

	public void setRole(final Role role)
	{
		this.role.set(role);
	}

	public Role getRole()
	{
		return this.role.get();
	}

	public String getRolePrefix()
	{
		switch(this.role.get())
		{
		case ADMIN:
			return "**";
		case MODERATOR:
			return "*";
		default:
			return "";
		}
	}

	public OfflinePlayer getPlayer()
	{
		val player = this.player.get();
		if(player == null)
		{
			val op = Bukkit.getOfflinePlayer(this.ID);
			if(op.isOnline())
				this.player = new WeakReference<Player>(op.getPlayer());
			return op;
		}
		return player;
	}

	public SCPlayer getSCPlayer(){
		val player = this.scPlayer.get();
		if(player == null)
		{
			val op = Bukkit.getOfflinePlayer(this.ID);
			if(op.isOnline())
				this.scPlayer = new WeakReference<SCPlayer>(SCCore.getInstance().getSCPlayerManager().getSCPlayer(op.getUniqueId()));
			return this.scPlayer.get();
		}
		return player;
	}

	public void messageFaction(final String message)
	{
		val cfac = FactionsListener.getFaction(this.factionId.get());
		if(this.getPlayer() == null || this.getSCPlayer() == null)
			return;
		SCPlayer p = this.getSCPlayer();
		FancyMessage fMessage = new FancyMessage(this.getRolePrefix()).color(ChatColor.GREEN).then(ChatColor.stripColor(p.getTag())).color(ChatColor.GREEN)
				.tooltip(p.getHoverText()).then(": " + message).color(ChatColor.GREEN);
		//ChatManager.log.info(msg);
		val players = cfac.messageFaction(fMessage);
		this.messageSpies(cfac, players, message, false);
	}

	public void messageAlliance(final String message)
	{
		val cfac = FactionsListener.getFaction(this.factionId.get());
		if(this.getPlayer() == null || this.getSCPlayer() == null)
			return;
		SCPlayer p = this.getSCPlayer();
		FancyMessage fMessage = new FancyMessage("[").color(ChatColor.DARK_PURPLE).then(cfac.getTag()).color(ChatColor.DARK_PURPLE)
				.then("] " + this.getRolePrefix()).color(ChatColor.DARK_PURPLE).then(ChatColor.stripColor(p.getTag())).color(ChatColor.DARK_PURPLE)
				.tooltip(p.getHoverText()).then(": " +message).color(ChatColor.DARK_PURPLE);
		//ChatManager.log.info(msg);
		val players = cfac.messageAlliance(fMessage);
		this.messageSpies(cfac, players, message, true);
	}

	private void messageSpies(final ConcurrentFaction cfac, final Set<OfflinePlayer> players, String message, final boolean alliance)
	{
		message = String.format("%s %s: %s", alliance ? ConcurrentFPlayer.aspy : ConcurrentFPlayer.fspy, this.getPlayer().getName(), message);
		message = String.format(message, cfac.getTag());
		for(val spy : ConcurrentFPlayer.spies)
			if(!players.contains(spy))
				spy.sendMessage(message);
	}

}
