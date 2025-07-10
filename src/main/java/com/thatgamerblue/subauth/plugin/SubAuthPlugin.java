package com.thatgamerblue.subauth.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SubAuthPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new SubAuthEventHandler(this), this);
	}
}
