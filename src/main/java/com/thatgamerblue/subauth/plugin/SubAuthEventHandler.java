package com.thatgamerblue.subauth.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatgamerblue.subauth.plugin.gson.GsonTypeAdapters;
import com.thatgamerblue.subauth.plugin.util.Strings;
import com.thatgamerblue.subauth.plugin.ws.WSClient;
import com.thatgamerblue.subauth.plugin.ws.messages.AuthenticationMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.ErrorMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WSMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WhitelistUpdateMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.subscriptions.Subscription;
import com.thatgamerblue.subauth.plugin.ws.messages.subscriptions.TwitchSubscription;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class SubAuthEventHandler implements Listener {
	private static final Logger logger = Logger.getLogger("SubAuth");

	private final Map<Subscription, List<UUID>> whitelistedPlayers = new ConcurrentHashMap<>();
	private Set<UUID> fastCheckWhitelist = new HashSet<>();

	@Getter
	private final SubAuthPlugin plugin;
	@Getter
	private final Gson gson;
	private WSClient wsClient;

	public SubAuthEventHandler(SubAuthPlugin plugin) {
		this.plugin = plugin;
		this.gson = new GsonBuilder().registerTypeAdapterFactory(GsonTypeAdapters.createFactory(WSMessage.class, List.of(
			AuthenticationMessage.class,
			WhitelistUpdateMessage.class,
			ErrorMessage.class
		))).registerTypeAdapterFactory(GsonTypeAdapters.createFactory(Subscription.class, List.of(
			TwitchSubscription.class
		))).disableHtmlEscaping().create();

		wsReconnect();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final UUID playerUuid = event.getPlayer().getUniqueId();
		boolean isOp = event.getPlayer().isOp();
		boolean whitelist = event.getPlayer().isWhitelisted();
		boolean subWhitelist = fastCheckWhitelist.contains(playerUuid);
		if (!isOp && !whitelist && !subWhitelist) {
			event.getPlayer().kick(Component.text(plugin.getConfig().getString("disallow_message")), PlayerKickEvent.Cause.WHITELIST);
			return;
		}
		List<Subscription> matchingSubscriptions = new ArrayList<>();
		for (Map.Entry<Subscription, List<UUID>> entry : whitelistedPlayers.entrySet()) {
			if (entry.getValue().contains(playerUuid)) {
				matchingSubscriptions.add(entry.getKey());
			}
		}
		String s = "[" + Strings.join(matchingSubscriptions, ", ") + "]";
		logger.info("Allowing player " + event.getPlayer().getName() + " to join: op? " + isOp + " mc whitelist? " + whitelist);
		logger.info(event.getPlayer().getName() + ": subWhitelist is " + s);
	}

	public void updateWhitelist(Subscription subscription, List<UUID> whitelist) {
		whitelistedPlayers.put(subscription, whitelist);
		rebuildFastWhitelist();
	}

	public void clearWhitelist() {
		whitelistedPlayers.clear();
		rebuildFastWhitelist();
	}

	public void wsReconnect() {
		if (this.wsClient == null || this.wsClient.isShouldReconnect()) {
			this.wsClient = new WSClient(this, plugin.getConfig());
			wsClient.connect();
		}
	}

	private void rebuildFastWhitelist() {
		fastCheckWhitelist = whitelistedPlayers.values().stream().flatMap(List::stream).collect(Collectors.toSet());
	}
}
