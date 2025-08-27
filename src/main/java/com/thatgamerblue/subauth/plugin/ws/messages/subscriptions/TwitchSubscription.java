package com.thatgamerblue.subauth.plugin.ws.messages.subscriptions;

import lombok.Value;
import org.json.simple.JSONObject;

@Value
public class TwitchSubscription extends Subscription {
	String userId;
	String createdAt;

	public static TwitchSubscription deserialize(JSONObject object) {
		return new TwitchSubscription((String) object.get("userId"), (String) object.get("createdAt"));
	}

	@Override
	String getId() {
		return userId;
	}
}
