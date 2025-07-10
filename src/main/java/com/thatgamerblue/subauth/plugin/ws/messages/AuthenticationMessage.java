package com.thatgamerblue.subauth.plugin.ws.messages;

import lombok.Value;

@Value
public class AuthenticationMessage extends WSMessage {
	String token;
}
