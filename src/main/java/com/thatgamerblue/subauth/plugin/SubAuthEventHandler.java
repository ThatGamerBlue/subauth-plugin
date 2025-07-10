package com.thatgamerblue.subauth.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatgamerblue.subauth.plugin.gson.GsonTypeAdapters;
import com.thatgamerblue.subauth.plugin.ws.WSClient;
import com.thatgamerblue.subauth.plugin.ws.messages.AuthenticationMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.ErrorMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WSMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WhitelistUpdateMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.subscriptions.Subscription;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class SubAuthEventHandler implements Listener {
	private final Map<Subscription, List<String>> whitelistedPlayers = new ConcurrentHashMap<>();
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
		))).disableHtmlEscaping().create();

		wsReconnect();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		boolean isOp = event.getPlayer().isOp();
		boolean whitelist = event.getPlayer().isWhitelisted();
		if (!isOp && !whitelist && !fastCheckWhitelist.contains(event.getPlayer().getUniqueId())) {
			event.getPlayer().kick(Component.text(plugin.getConfig().getString("disallow_message")), PlayerKickEvent.Cause.WHITELIST);
		}
	}

	public void updateWhitelist(Subscription subscription, List<String> whitelist) {
		whitelistedPlayers.put(subscription, whitelist);
		rebuildFastWhitelist();
	}

	public void clearWhitelist() {
		whitelistedPlayers.clear();
		rebuildFastWhitelist();
	}

	public void wsReconnect() {
		if (this.wsClient.isShouldReconnect()) {
			this.wsClient = new WSClient(this, plugin.getConfig());
			wsClient.connect();
		}
	}

	private void rebuildFastWhitelist() {
		fastCheckWhitelist = whitelistedPlayers.values().stream().flatMap(List::stream).map(UUID::fromString).collect(Collectors.toSet());
	}
}
