package com.thatgamerblue.subauth.plugin.ws;

import com.thatgamerblue.subauth.plugin.SubAuthEventHandler;
import com.thatgamerblue.subauth.plugin.ws.messages.AuthenticationMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.ErrorMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WSMessage;
import com.thatgamerblue.subauth.plugin.ws.messages.WhitelistUpdateMessage;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WSClient extends WebSocketClient {
	private static final Logger logger = Logger.getLogger("SubAuth-WSClient");

	private final SubAuthEventHandler eventHandler;
	private final Configuration config;
	@Getter
	private boolean shouldReconnect = true;

	public WSClient(SubAuthEventHandler eventHandler, Configuration config) {
		super(URI.create("wss://" + config.getString("subauth_host") + ":" + config.getInt("subauth_port") + "/ws"));
		this.eventHandler = eventHandler;
		this.config = config;
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		List<String> tokens = config.getStringList("tokens");
		for (String token : tokens) {
			send(new AuthenticationMessage(token));
		}
	}

	@Override
	public void onMessage(String s) {
		WSMessage message = eventHandler.getGson().fromJson(s, WSMessage.class);
		if (message instanceof ErrorMessage error) {
			switch (error.getError()) {
				case NO_HANDLER:
					logger.severe("SubAuth backend encountered a critical error, wiping whitelist and disconnecting");
					eventHandler.clearWhitelist();
					shouldReconnect = false;
					close();
					break;
				case INVALID_TOKEN:
					logger.severe("There is an invalid SubAuth token in your config file, please check it, and recreate any tokens if necessary");
					break;
				case ALREADY_SUBSCRIBED:
					logger.warning("There is a duplicate SubAuth token in your config file, please check it, and remove any duplicates");
					break;
				case UNKNOWN_USER:
					logger.severe("One of your SubAuth tokens is owned by a user unknown to SubAuth. Did you unlink your account?");
					break;
			}
		} else if (message instanceof WhitelistUpdateMessage whitelistUpdate) {
			eventHandler.updateWhitelist(whitelistUpdate.getCause(), whitelistUpdate.getWhitelist().stream().map(UUID::fromString).toList());
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		if (shouldReconnect) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(eventHandler.getPlugin(), eventHandler::wsReconnect, 5000);
		}
	}

	@Override
	public void onError(Exception e) {
		logger.log(Level.SEVERE, "SubAuth's websocket connection encountered an issue", e);
	}

	private void send(WSMessage message) {
		send(eventHandler.getGson().toJson(message));
	}
}
