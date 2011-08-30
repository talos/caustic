package net.microscraper.util;

import net.microscraper.util.UUID;
import net.microscraper.util.UUIDFactory;

public class JavaUtilUUIDFactory implements UUIDFactory {

	@Override
	public UUID get() {
		return new UUID() {
			private final java.util.UUID uuid = java.util.UUID.randomUUID();
			
			@Override
			public int asInt() {
				return uuid.hashCode();
			}

			@Override
			public String asString() {
				return uuid.toString();
			}
			
		};
	}

}
