package io.xydez.airqueue;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener {
	private AirQueue plugin;

	public EventListener(AirQueue instance)
	{
		this.plugin = instance;
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent event)
	{
		this.plugin.getLogger().info(String.format("ServerConnectEvent(player=%s,target=%s)", event.getPlayer().getName(), event.getTarget().getName()));

		if (event.getTarget().getName().equals(this.plugin.mainServer))
		{
			this.plugin.getLogger().info(String.format("Initial connect to main server. (size=%d,limit=%d)", event.getTarget().getPlayers().size(), this.plugin.playerLimit));

			if (event.getTarget().getPlayers().size() < this.plugin.playerLimit)
			{
				this.plugin.getLogger().info(String.format("Connecting player %s to main server (%d/%d players)", event.getPlayer().getName(), event.getTarget().getPlayers().size(), this.plugin.playerLimit));
			}
			else
			{
				this.plugin.getLogger().info(String.format("Connecting player %s to queue server (%d/%d players in main server)", event.getPlayer().getName(), event.getTarget().getPlayers().size(), this.plugin.playerLimit));
				event.setTarget(ProxyServer.getInstance().getServerInfo(this.plugin.queueServer));

				event.getPlayer().sendMessage(new ComponentBuilder(String.format("The main server is full! (%d/%d players)", ProxyServer.getInstance().getServerInfo(this.plugin.mainServer).getPlayers().size(), this.plugin.playerLimit)).color(ChatColor.GOLD).create());
				this.plugin.playerQueue.addLast(event.getPlayer().getUniqueId());

				sendQueuePosition(event.getPlayer());
			}
		}
		else if (event.getTarget().getName().equals(this.plugin.queueServer))
		{
			// Nothing yet
		}
		else
		{
			this.plugin.getLogger().warning(String.format("What server is the user connecting to? (MAIN_SERVER=%s, QUEUE_SERVER=%s,TARGET=%s)", this.plugin.mainServer, this.plugin.queueServer, event.getTarget().getName()));
		}
	}

	@EventHandler
	public void onServerDisconnect(ServerDisconnectEvent event)
	{
		// If the player disconnected from the queue server
		if (event.getTarget().getName().equals(this.plugin.queueServer))
		{
			int index = -1;

			for (int i = 0; i < this.plugin.playerQueue.size(); i++)
			{
				if (this.plugin.playerQueue.get(i).compareTo(event.getPlayer().getUniqueId()) == 0)
				{
					index = i;
					break;
				}
			}

			if (index != -1)
			{
				this.plugin.playerQueue.remove(index);

				for (int i = index; i < this.plugin.playerQueue.size(); i++)
				{
					sendQueuePosition(ProxyServer.getInstance().getPlayer(plugin.playerQueue.get(i)));
				}
			}
		}
		else if (event.getTarget().getName().equals(this.plugin.mainServer))
		{
			updateQueue();
		}
	}

	public void updateQueue()
	{
		int count = this.plugin.playerLimit - ProxyServer.getInstance().getServerInfo(this.plugin.mainServer).getPlayers().size();

		for (int i = 0; i < count; i++)
		{
			UUID uuid = plugin.playerQueue.pollFirst();

			if (uuid == null)
				break;

			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
			player.connect(ProxyServer.getInstance().getServerInfo(this.plugin.mainServer));
		}

		for (ProxiedPlayer player : ProxyServer.getInstance().getServerInfo(this.plugin.queueServer).getPlayers())
		{
			sendQueuePosition(player);
		}
	}

	public void sendQueuePosition(ProxiedPlayer player)
	{
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
			return;
		}

		player.sendMessage(new ComponentBuilder(String.format("You are in queue position %d.", index + 1)).color(ChatColor.GOLD).create());
	}
}
