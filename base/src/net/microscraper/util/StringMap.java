package net.microscraper.util;

import java.util.Hashtable;

public class StringMap {
	private final Hashtable hashtable;
	private final StringMap parent;
	
	public StringMap(Hashtable hashtable) {
		this.hashtable = hashtable;
		this.parent = null;
	}
	
	private StringMap(StringMap parent) {
		this.parent = parent;
		this.hashtable = new Hashtable();
	}
	
	public StringMap spawnChild() {
		return new StringMap(this);
	}
	
	public String get(String key) {
		if(hashtable.containsKey(key)) {
			return (String) hashtable.get(key);
		} else if(parent != null) {
			return parent.get(key);
		} else {
			return null;
		}
	}
	
	public void put(String key, String value) {
		hashtable.put(key, value);
	}
}
