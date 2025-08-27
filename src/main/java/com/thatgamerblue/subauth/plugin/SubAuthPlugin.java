package com.thatgamerblue.subauth.plugin;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SubAuthPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		saveDefaultConfig();

		SubAuthEventHandler eventHandler = new SubAuthEventHandler(this);

		if (getConfig().getStringList("tokens").contains("token1")) {
			Logger logger = Logger.getLogger("SubAuth");
			logger.severe("=====================================================");
			logger.severe("SubAuth needs to be configured before you can use it!");
			logger.severe("");
			logger.severe("By default it will disallow all users not opped or manually whitelisted");
			logger.severe("via the Minecraft whitelist system.");
			logger.severe("");
			logger.severe("Please get a token from the SubAuth server and put it in your config file,");
			logger.severe("removing the lines that say token1, token2, etc, then restart your server.");
			logger.severe("=====================================================");
		} else {
			eventHandler.wsConnect();
		}

		Bukkit.getPluginManager().registerEvents(eventHandler, this);
	}
}
