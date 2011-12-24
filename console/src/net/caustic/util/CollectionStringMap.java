package net.caustic.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link StringMap} using java.util.Collection library.
 * @author talos
 *
 */
public class CollectionStringMap implements StringMap {

	private final Map<String, String> map;
	private final CollectionStringMap parent;
	
	public CollectionStringMap(Map<String, String> map) {
		this.map = map;
		this.parent = null;
	}
	
	private CollectionStringMap(CollectionStringMap parent) {
		this.map = new HashMap<String, String>();
		this.parent = parent;
	}

	@Override
	public String get(String key) {
		if(map.containsKey(key)) {
			return map.get(key);
		} else {
			if(parent != null) {
				return parent.get(key);
			} else {
				return null;
			}
		}
	}
	
	public void put(String key, String value) {
		map.put(key,  value);
	}
	
	public CollectionStringMap branch() {
		return new CollectionStringMap(this);
	}
}
