package com.thatgamerblue.subauth.plugin.ws.messages;

import com.thatgamerblue.subauth.plugin.ws.messages.subscriptions.Subscription;
import java.util.List;
import lombok.Value;

@Value
public class WhitelistUpdateMessage extends WSMessage {
	Subscription cause;
	List<String> whitelist;
}
