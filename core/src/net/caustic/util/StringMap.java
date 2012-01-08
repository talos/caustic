package net.caustic.util;

public interface StringMap {

	public String get(String key);
	
	/**
	 * Return a wrapped {@link StringMap} that prefers the specified key/value mapping.
	 * @param key
	 * @param value
	 * @return A new {@link StringMap}.  Does not alter the original.
	 */
	public StringMap extend(String key, String value);
}
