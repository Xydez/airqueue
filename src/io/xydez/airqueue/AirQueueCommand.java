package io.xydez.airqueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AirQueueCommand extends Command
{
	private AirQueue plugin;

	public AirQueueCommand(AirQueue plugin)
	{
		super("airqueue", "airqueue", "aq");

		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if (args.length == 0)
		{
			sendHelpMessage(sender);
		}
		else if (args.length == 1 && (args[0].equalsIgnoreCase("position") || args[0].equalsIgnoreCase("pos")))
		{
			if (sender instanceof ProxiedPlayer)
			{
				ProxiedPlayer player = (ProxiedPlayer)sender;

				int index = -1;

				for (int i = 0; i < this.plugin.playerQueue.size(); i++)
				{
					if (this.plugin.playerQueue.get(i).compareTo(player.getUniqueId()) == 0)
					{
						index = i;
						break;
					}
				}

				if (index == -1)
				{
					player.sendMessage(new ComponentBuilder("Error: You are not in the queue.").color(ChatColor.RED).create());
				}
				else
				{
					player.sendMessage(new ComponentBuilder(String.format("You are in queue position %d.", index + 1)).color(ChatColor.GOLD).create());
				}
			}
			else
			{
				sender.sendMessage(new ComponentBuilder("This command cannot be executed from the console!").color(ChatColor.RED).create());
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("version"))
		{
			if (!sender.hasPermission("airqueue.version"))
			{
				sender.sendMessage(new ComponentBuilder("Error: You do not have the permission to run this command!").color(ChatColor.RED).create());
			}
			sender.sendMessage(new ComponentBuilder("AirQueue v1.0.0").color(ChatColor.GOLD).create());
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("length"))
		{
			if (!sender.hasPermission("airqueue.length"))
			{
				sender.sendMessage(new ComponentBuilder("Error: You do not have the permission to run this command!").color(ChatColor.RED).create());
				return;
			}

			int length = this.plugin.playerQueue.size();

			sender.sendMessage(new ComponentBuilder(String.format("There are currently %d players in the queue.", length)).color(ChatColor.GOLD).create());
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("limit"))
		{
			if (!sender.hasPermission("airqueue.limit.get") && !sender.hasPermission("airqueue.limit.set"))
			{
				sender.sendMessage(new ComponentBuilder("Error: You do not have the permission to run this command!").color(ChatColor.RED).create());
				return;
			}

			sender.sendMessage(new ComponentBuilder(String.format("The player limit is set to %d players", this.plugin.playerLimit)).color(ChatColor.GOLD).create());
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("limit"))
		{
			if (!sender.hasPermission("airqueue.limit.set"))
			{
				sender.sendMessage(new ComponentBuilder("Error: You do not have the permission to run this command!").color(ChatColor.RED).create());
				return;
			}

			try {
				int limit = Integer.parseInt(args[1]);

				this.plugin.setPlayerLimit(limit);
				sender.sendMessage(new ComponentBuilder(String.format("Set the player limit to %d players.", limit)).color(ChatColor.GREEN).create());
			} catch(NumberFormatException ex)
			{
				sender.sendMessage(new ComponentBuilder("Error: Argument \"limit\" must be a number!").color(ChatColor.RED).create());
			}
		}
		else
		{
			sender.sendMessage(new ComponentBuilder("Command not found").color(ChatColor.RED).create());
		}
	}

	private void sendHelpMessage(CommandSender sender)
	{
		ComponentBuilder builder = new ComponentBuilder("\n============\n~ AirQueue ~\n------------\n").color(ChatColor.GOLD);

		builder = builder.append("/airqueue position|pos - Shows your position in the queue\n").color(ChatColor.GOLD);
		
		if (sender.hasPermission("airqueue.version"))
		{
			builder = builder.append("/airqueue version - Shows the current version of AirQueue\n").color(ChatColor.GOLD);
		}

		if (sender.hasPermission("airqueue.length"))
		{
			builder = builder.append("/airqueue length - Shows the length of the queue\n").color(ChatColor.GOLD);
		}

		if (sender.hasPermission("airqueue.limit.get"))
		{
			if (sender.hasPermission("airqueue.limit.set"))
			{
				builder = builder.append("/airqueue limit [limit] - Get/Set the player limit for the main server.\n").color(ChatColor.GOLD);
			}
			else
			{
				builder = builder.append("/airqueue limit - Get the player limit for the main server.\n").color(ChatColor.GOLD);
			}
		}

		builder.append("============").color(ChatColor.GOLD);

		sender.sendMessage(builder.create());
	}
}
