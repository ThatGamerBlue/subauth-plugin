package com.thatgamerblue.subauth.plugin.ws.messages.subscriptions;

public abstract class Subscription {
	abstract String getId();

	@Override
	public String toString() {
		return "Type: " + getClass().getSimpleName() + " ID: " + getId();
	}
}
