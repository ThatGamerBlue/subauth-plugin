package com.thatgamerblue.subauth.plugin;

import com.thatgamerblue.subauth.plugin.util.Strings;
import com.thatgamerblue.subauth.plugin.ws.WSClient;
import com.thatgamerblue.subauth.plugin.ws.messages.subscriptions.Subscription;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SubAuthEventHandler implements Listener {
	private static final String DEFAULT_NOT_AUTHED_MESSAGE = "You must be a subscriber to join this server!";
	private static final Logger logger = Logger.getLogger("SubAuth");

	private final Map<Subscription, List<UUID>> whitelistedPlayers = new ConcurrentHashMap<>();
	private Set<UUID> fastCheckWhitelist = new HashSet<>();

	@Getter
	private final SubAuthPlugin plugin;
	@Setter
	private boolean notConfigured;
	private WSClient wsClient;

	public SubAuthEventHandler(SubAuthPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID playerUuid = player.getUniqueId();
		boolean isOp = player.isOp();
		boolean whitelist = player.isWhitelisted();
		boolean hasPermission = player.hasPermission("subauth.bypass");
		boolean subWhitelist = fastCheckWhitelist.contains(playerUuid);
		if (!isOp && !whitelist && !subWhitelist && !hasPermission) {
			event.getPlayer().kickPlayer(plugin.getConfig().getString("disallow_message", DEFAULT_NOT_AUTHED_MESSAGE));
			return;
		}
		List<Subscription> matchingSubscriptions = new ArrayList<>();
		for (Map.Entry<Subscription, List<UUID>> entry : whitelistedPlayers.entrySet()) {
			if (entry.getValue().contains(playerUuid)) {
				matchingSubscriptions.add(entry.getKey());
			}
		}
		String s = "[" + Strings.join(matchingSubscriptions, ", ") + "]";
		logger.info("Allowing player " + event.getPlayer().getName() + " to join: op? " + isOp + " mc whitelist? " + whitelist + " permission? " + hasPermission);
		logger.info(event.getPlayer().getName() + ": subWhitelist is " + s);
		if (notConfigured && isOp) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "[SubAuth] SubAuth has not been properly configured, and will not allow users to join! Please check the config file.");
		}
	}

	public void updateWhitelist(Subscription subscription, List<UUID> whitelist) {
		logger.info("Received whitelist update for subscription: " + subscription.toString());
		whitelistedPlayers.put(subscription, whitelist);
		rebuildFastWhitelist();
	}

	public void clearWhitelist() {
		whitelistedPlayers.clear();
		rebuildFastWhitelist();
	}

	public void wsConnect() {
		if (this.wsClient == null || this.wsClient.isShouldReconnect()) {
			this.wsClient = new WSClient(this, plugin.getConfig());
			wsClient.connect();
		}
	}

	private void rebuildFastWhitelist() {
		Set<UUID> newSet = new HashSet<>();
		for (List<UUID> list : whitelistedPlayers.values()) {
			newSet.addAll(list);
		}
		fastCheckWhitelist = newSet;
	}
}
