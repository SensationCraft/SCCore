package com.sensationcraft.sccore.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.sensationcraft.sccore.SCCore;
import com.sensationcraft.sccore.chat.ChatChannel;
import com.sensationcraft.sccore.scplayer.SCPlayerManager;

public class ChannelCommand implements CommandExecutor
{

	private final String changed = ChatColor.GREEN+"You are talking in "+ChatColor.YELLOW+"%s"+ChatColor.GREEN+".";

	private final SCCore instance;
	private final SCPlayerManager scPlayerManager;

	public ChannelCommand(SCCore instance) {
		this.instance = instance;
		this.scPlayerManager = instance.getSCPlayerManager();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args)
	{
		if((sender instanceof Player) == false)
		{
			sender.sendMessage("Only ingame players can switch channels");
			return true;
		}

		if(args.length < 1)
		{
			sender.sendMessage("/channel shout|global(shout)|public|ally|faction");
			return true;
		}

		ChatChannel channel;
		if(args[0].isEmpty())
			channel = ChatChannel.NONE;
		else
		{
			char c = args[0].charAt(0);
			switch(c)
			{
			case 'g':
			case 's':
				channel = ChatChannel.SHOUT;
				break;
			case 'p':
				channel = ChatChannel.PUBLIC;
				break;
			case 'a':
				MPlayer mPlayer = MPlayerColl.get().get(sender);
				if(!mPlayer.hasFaction()){
					sender.sendMessage(ChatColor.RED+"You don't have a faction!");
					return true;
				}
				channel = ChatChannel.ALLY;
				break;
			case 'f':
				MPlayer mPlayer2 = MPlayerColl.get().get(sender);
				if(!mPlayer2.hasFaction()){
					sender.sendMessage(ChatColor.RED+"You don't have a faction!");
					return true;
				}
				channel = ChatChannel.FACTION;
				break;
			default:
				channel = ChatChannel.NONE;
			}
		}
		if(channel == ChatChannel.NONE)
		{
			sender.sendMessage(ChatColor.DARK_RED+String.format("Unknown channel '%s'", args[0]));
			return true;
		}

		sender.sendMessage(String.format(this.changed, channel == ChatChannel.SHOUT ? "GLOBAL (SHOUT)" : channel.name()));
		this.scPlayerManager.getSCPlayer(((Player)sender).getUniqueId()).setChannel(channel);
		//Bukkit.getPluginManager().callEvent(new ChannelChangeEvent(sender.getName(), channel));

		return true;
	}

}
