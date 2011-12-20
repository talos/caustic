package net.caustic.util;

import java.util.Map;

/**
 * Implementation of {@link StringMap} using java.util.Collection library.
 * @author talos
 *
 */
public class CollectionStringMap implements StringMap {

	private final Map<String, String> map;
	
	public CollectionStringMap(Map<String, String> map) {
		this.map = map;
	}
	
	@Override
	public String get(String key) {
		return map.get(key);
	}

}
