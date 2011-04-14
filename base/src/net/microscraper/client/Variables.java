package net.microscraper.client;

import java.util.Hashtable;

public class Variables {
	private final Hashtable variables = new Hashtable();
	//private final Hashtable variables_by_ref = new Hashtable();
	//private final Hashtable variables_by_title = new Hashtable();
	public Variables put(String key, String value) throws NullPointerException {
		/*variables_by_ref.put(key, value);
		if(variables_by_title.containsKey(key.title)) {
			Client.context().log.i("Overwrote a redundant reference in Variables, with title " + key.title);
		}
		variables_by_title.put(key.title, value);
		*/
		variables.put(key, value);
		return this;
	}
	public boolean containsKey(String key) {
		return variables.containsKey(key);
	}
	public String get(String key) {
		return (String) variables.get(key);
	}
	public String toString() {
		return variables.toString();
	}
	/*
	public String[] keys() {
		String[] keys = new String[variables.size()];
		Utils.hashtableKeys(variables, keys);
		return keys;
	}
	public String[] values() {
		String[] titles = new String[variables_by_title.size()];
		Utils.hashtableKeys(variables_by_title, titles);
		return titles;
	}*/
	/**
	 * This merges another Variables object into this one, destructively.
	 * @param other
	 * @return
	 */
	/*
	public Variables merge(Variables other) {
		Reference[] other_refs = other.refs();
		for(int i = 0; i < other_refs.length; i ++) {
			put(other_refs[i], other.getByRef(other_refs[i]));
		}
		return this;
	}
	*/
	/*
	public boolean equals(Variables other) {
		if(this.titles().length != other.titles().length) {
			return false; // Must be the same size.
		}
		String[] titles = this.titles();
		for(int i = 0; i < titles.length; i ++) {
			if(other.containsTitle(titles[i])) { // Must contain the same effective titles.
				if(!this.getByTitle(titles[i]).equals(other.getByTitle(titles[i]))) { // The titles must be mapped to the same value.
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	*/
}
