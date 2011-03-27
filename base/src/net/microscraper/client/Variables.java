package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.database.Reference;

public class Variables {
	private final Hashtable variables_by_ref = new Hashtable();
	private final Hashtable variables_by_title = new Hashtable();
	/**
	 * Map a key to a value.
	 * @param key A Reference key.
	 * @param value A string value.
	 * @return The Variables object, for chaining.
	 * @throws NullPointerException if the key or value is null.
	 */
	public Variables put(Reference key, String value) throws NullPointerException {
		variables_by_ref.put(key, value);
		if(variables_by_title.containsKey(key.title)) {
			Log.i("Overwrote a redundant reference in Variables, with title " + key.title);
		}
		variables_by_title.put(key.title, value);
		
		return this;
	}
	public String getByRef(Reference key) throws NullPointerException {
		return (String) variables_by_ref.get(key);
	}
	public String getByTitle(String title) throws NullPointerException {
		return (String) variables_by_title.get(title);
	}
	public boolean containsRef(Reference key) {
		return variables_by_ref.containsKey(key);
	}
	public boolean containsTitle(String title) {
		return variables_by_title.containsKey(title);
	}
	public String toString() {
		return variables_by_ref.toString();
	}
	public Reference[] refs() {
		Reference[] refs = new Reference[variables_by_ref.size()];
		Utils.hashtableKeys(variables_by_ref, refs);
		return refs;
	}
	public String[] titles() {
		String[] titles = new String[variables_by_title.size()];
		Utils.hashtableKeys(variables_by_title, titles);
		return titles;
	}
	/**
	 * This merges another Variables object into this one, destructively.
	 * @param other
	 * @return
	 */
	public Variables merge(Variables other) {
		Reference[] other_refs = other.refs();
		for(int i = 0; i < other_refs.length; i ++) {
			put(other_refs[i], other.getByRef(other_refs[i]));
		}
		return this;
	}
	
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
}
