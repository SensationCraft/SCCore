package com.sensationcraft.sccore.chat.factions;

import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.MapMaker;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.struct.Relation;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.chat.ChatChannel;
import com.sensationcraft.sccore.scplayer.SCPlayer;

import lombok.val;

public class FactionsListener implements Listener
{

	public static Map<String, ConcurrentFaction> cfactions = new MapMaker().makeMap();
	public static Map<UUID, ConcurrentFPlayer> cfplayers = new MapMaker().makeMap();

	public FactionsListener()
	{
		for(val fac : Factions.getInstance().getAllFactions())
		{
			val cfac = new ConcurrentFaction(fac);
			FactionsListener.cfactions.put(fac.getId(), cfac);
		}

		for(val fplayer : FPlayers.getInstance().getOnlinePlayers())
		{
			val cfme = new ConcurrentFPlayer(fplayer);
			FactionsListener.cfplayers.put(fplayer.getPlayer().getUniqueId(), cfme);
			if(fplayer.getFaction().isNone())
				continue;
			val cfac = FactionsListener.getFaction(fplayer.getFaction().getId());
			if(cfac != null)
				cfac.addPlayer(cfme);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onJoin(final PlayerJoinEvent event)
	{
		val player = event.getPlayer();
		ConcurrentFPlayer cfme = FactionsListener.getPlayer(player);
		if(cfme == null)
		{
			val fme = FPlayers.getInstance().getByOfflinePlayer(player);
			cfme = new ConcurrentFPlayer(fme);
			FactionsListener.cfplayers.put(player.getUniqueId(), cfme);
			if(fme.getFaction().isNone())
				return;
			val cfac = FactionsListener.getFaction(fme.getFaction().getId());
			if(cfac != null)
				cfac.addPlayer(cfme);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLeave(final PlayerQuitEvent event)
	{
		// Possible free some memory, not sure about that yet
		val cfme = FactionsListener.cfplayers.remove(event.getPlayer().getUniqueId());
		if(cfme != null)
			cfme.leaveFaction();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFacJoin(final FPlayerJoinEvent event)
	{
		val cfme = FactionsListener.getPlayer(event.getfPlayer().getPlayer());
		ConcurrentFaction cfac = FactionsListener.getFaction(event.getFaction().getId());
		if(cfac == null)
		{
			cfac = new ConcurrentFaction(event.getFaction());
			FactionsListener.cfactions.put(event.getFaction().getId(), cfac);
		}
		if(cfme != null)
			cfme.joinFaction(cfac);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFacLeave(final FPlayerLeaveEvent event)
	{
		val cfme = FactionsListener.getPlayer(event.getfPlayer().getPlayer());
		if(cfme != null)
		{
			cfme.leaveFaction();
			event.getfPlayer().getPlayer().sendMessage(ChatColor.GREEN+"You are talking in "+ChatColor.YELLOW+"PUBLIC"+ChatColor.GREEN+".");
			SCPlayer user = SCCore.getInstance().getSCPlayerManager().getSCPlayer(event.getfPlayer().getPlayer().getUniqueId());
			user.setChannel(ChatChannel.PUBLIC);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void OnFacCreate(final FactionCreateEvent event)
	{
		// Basically handled in onFacJoin
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFacDisband(final FactionDisbandEvent event)
	{
		val cfac = FactionsListener.cfactions.remove(event.getFaction().getId());
		if(cfac != null)
			// Post modifications perhaps?
			cfac.disband();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFacRelChange(final FactionRelationEvent event)
	{
		if(event.getOldRelation().isAtMost(Relation.NEUTRAL) && event.getRelation().isAtMost(Relation.NEUTRAL))
			return;
		// Shouldn't this be stopped inside Factions? Idk ;)
		// I just add it 'just in case'
		if(event.getOldRelation() == event.getRelation())
			return;

		val cfac = FactionsListener.getFaction(event.getFaction().getId());
		val ctarget = FactionsListener.getFaction(event.getTargetFaction().getId());

		if((cfac == null) || (ctarget == null))
			return;

		switch(event.getRelation())
		{
		case ENEMY:
		case NEUTRAL:
			cfac.removeAlly(ctarget);
			ctarget.removeAlly(cfac);
			break;
		case ALLY:
			cfac.addAlly(ctarget);
			ctarget.addAlly(cfac);
			break;
		default:
			break;
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onFacRename(final FactionRenameEvent event)
	{
		val cfac = FactionsListener.getFaction(event.getFaction().getId());
		if(cfac != null)
			cfac.setTag(event.getFactionTag());
	}

	public static ConcurrentFPlayer getPlayer(final Player player)
	{
		return FactionsListener.cfplayers.get(player.getUniqueId());
	}

	public static ConcurrentFPlayer getPlayer(final UUID id)
	{
		return FactionsListener.cfplayers.get(id);
	}

	public static ConcurrentFaction getFaction(final String factionId)
	{
		return FactionsListener.cfactions.get(factionId);
	}
}
