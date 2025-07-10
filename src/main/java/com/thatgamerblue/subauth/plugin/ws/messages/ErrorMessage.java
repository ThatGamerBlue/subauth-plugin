package com.thatgamerblue.subauth.plugin.ws.messages;

import lombok.Value;

@Value
public class ErrorMessage extends WSMessage {
	ErrorType error;

	public enum ErrorType {
		INVALID_TOKEN, NO_HANDLER, ALREADY_SUBSCRIBED, UNKNOWN_USER;
	}
}
