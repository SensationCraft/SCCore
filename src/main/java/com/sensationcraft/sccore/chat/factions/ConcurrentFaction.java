package com.sensationcraft.sccore.chat.factions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.OfflinePlayer;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.sensationcraft.sccore.utils.fanciful.FancyMessage;

import lombok.val;

/**
 * Class to relay Faction data without breaking due concurrency problems
 */
public class ConcurrentFaction
{

	private final AtomicReference<String> tag = new AtomicReference<String>("");

	private final AtomicReference<String> id = new AtomicReference<String>("");

	private final Set<String> allies = Collections.synchronizedSet(new HashSet<String>());

	private final Set<ConcurrentFPlayer> members = Collections.synchronizedSet(new HashSet<ConcurrentFPlayer>());


	/**
	 * Please construct it on the main thread.
	 * @param fac the Faction
	 */
	public ConcurrentFaction(final Faction fac)
	{
		this.tag.set(fac.getTag());
		this.id.set(fac.getId());
		for(val fother : Factions.getInstance().getAllFactions())
			if(fother.getRelationTo(fac, true).isAlly())
				this.allies.add(fother.getId());
	}

	public String getTag()
	{
		return this.tag.get();
	}

	public void setTag(final String tag)
	{
		this.tag.set(tag);
	}

	public String getId()
	{
		return this.id.get();
	}

	public void setId(final String id)
	{
		this.id.set(id);
	}

	public Set<OfflinePlayer> messageAlliance(final FancyMessage message)
	{
		val players = new HashSet<OfflinePlayer>();
		List<String> copyOfAllies;
		synchronized(this.allies)
		{
			copyOfAllies = new ArrayList<String>(this.allies);
		}
		for(val ally : copyOfAllies)
		{
			val cfac = FactionsListener.getFaction(ally);
			ConcurrentFPlayer[] mems;
			synchronized(cfac.members)
			{
				mems = cfac.members.toArray(new ConcurrentFPlayer[cfac.members.size()]);
			}
			for(val cfother : mems)
			{
				val cother = cfother.getPlayer();
				players.add(cother);
				if(cother.isOnline())
					message.send(cother.getPlayer());
			}
		}

		// Might be doubling alliance messages
		players.addAll(this.messageFaction(message, true));
		return players;
	}

	public Set<OfflinePlayer> messageFaction(final FancyMessage message)
	{
		return this.messageFaction(message, false);
	}

	public Set<OfflinePlayer> messageFaction(final FancyMessage message, final boolean alliance)
	{
		val players = new HashSet<OfflinePlayer>();
		ConcurrentFPlayer[] mems;
		synchronized(this.members)
		{
			mems = this.members.toArray(new ConcurrentFPlayer[this.members.size()]);
		}
		for(val cfother : mems)
		{
			val cother = cfother.getPlayer();
			players.add(cother);
			if(cother.isOnline())
				message.send(cother.getPlayer());
		}
		return players;
	}

	public void addPlayer(final ConcurrentFPlayer player)
	{
		synchronized(this.members)
		{
			this.members.add(player);
		}
	}

	public void removePlayer(final ConcurrentFPlayer player)
	{
		synchronized(this.members)
		{
			this.members.remove(player);
		}
	}

	public void addAlly(final ConcurrentFaction cfac)
	{
		synchronized(this.allies)
		{
			this.allies.add(cfac.getId());
		}
	}

	public void removeAlly(final ConcurrentFaction cfac)
	{
		synchronized(this.allies)
		{
			this.allies.remove(cfac.getId());
		}
	}

	public void disband()
	{
		String[] a;
		synchronized(this.allies)
		{
			a = this.allies.toArray(new String[this.allies.size()]);
		}
		for(val ally : a)
		{
			val cfac = FactionsListener.getFaction(ally);
			if(cfac != null)
				cfac.removeAlly(this);
		}
	}

}
