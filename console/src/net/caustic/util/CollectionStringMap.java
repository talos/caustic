package net.caustic.util;

import java.util.Map;

/**
 * Implementation of {@link StringMap} using java.util.Collection library.
 * @author talos
 *
 */
public class CollectionStringMap implements StringMap {

	public final String id;
	
	private final CollectionStringMap parent;
	private final Map<String, String> map;
	
	public CollectionStringMap(String id, Map<String, String> map) {
		this.id = id;
		this.map = map;
		this.parent = null;
	}
	
	private CollectionStringMap(String id, CollectionStringMap parent, Map<String, String> map) {
		this.id = id;
		this.map = map;
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
	
	public CollectionStringMap branch(String id, Map<String, String> map) {
		return new CollectionStringMap(id, this, map);
	}
	
	public String getParentId() {
		if(parent != null) {
			return parent.id;
		} else {
			return null;
		}
	}
}
