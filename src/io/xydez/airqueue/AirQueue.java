package io.xydez.airqueue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class AirQueue extends Plugin {
	protected EventListener eventListener;
	protected String queueServer;
	protected String mainServer;
	protected int playerLimit = 0;

	private File configFile;
	protected Configuration config;
	protected LinkedList<UUID> playerQueue = new LinkedList<UUID>();

	@Override
	public void onEnable()
	{
		this.eventListener = new EventListener(this);

		getProxy().getPluginManager().registerListener(this, this.eventListener);
		getProxy().getPluginManager().registerCommand(this, new AirQueueCommand(this));

		try {
			this.configFile = new File(getProxy().getPluginsFolder() + "/airqueue.yml");

			// If the configuration doesn't exist, create a default configuration
			if (!this.configFile.exists())
			{
				this.configFile.createNewFile();
				this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);

				this.config.set("player_limit", 20);
				this.config.set("server.main", "main");
				this.config.set("server.queue", "queue");

				saveConfig();
			}
			else
			{
				this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.playerLimit = this.config.getInt("player_limit", 20);
		this.mainServer = this.config.getString("server.main", "main");
		this.queueServer = this.config.getString("server.queue", "queue");
	}

	@Override
	public void onDisable()
	{
		saveConfig();
	}

	protected void saveConfig()
	{
		try
		{
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.config, this.configFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void setPlayerLimit(int limit)
	{
		this.playerLimit = limit;
		this.config.set("player_limit", limit);
		this.eventListener.updateQueue();
	}
}
