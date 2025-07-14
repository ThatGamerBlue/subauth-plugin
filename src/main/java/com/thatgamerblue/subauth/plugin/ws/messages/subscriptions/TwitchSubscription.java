package com.thatgamerblue.subauth.plugin.ws.messages.subscriptions;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class TwitchSubscription extends Subscription {
	String userId;
	@EqualsAndHashCode.Exclude
	Instant createdAt;

	@Override
	String getId() {
		return userId;
	}
}
