package com.thatgamerblue.subauth.plugin.gson;

import com.google.gson.TypeAdapterFactory;
import java.util.Collection;

public class GsonTypeAdapters {
	public static <T> TypeAdapterFactory createFactory(Class<T> baseClass, Collection<Class<? extends T>> messages) {
		RuntimeTypeAdapterFactory<T> factory = RuntimeTypeAdapterFactory.of(baseClass);
		messages.forEach(factory::registerSubtype);
		return factory;
	}
}
