package net.microscraper.database;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Scraper;

/**
 * This is a default in-memory implementation of {@link DatabaseView}
 * using {@link Hashtable}.  Created by the {@link Hashtable} constructors
 * of {@link Scraper}.
 * @author talos
 * @see Scraper#Scraper(net.microscraper.instruction.Load, Hashtable)
 * @see Scraper#Scraper(net.microscraper.instruction.Instruction, Hashtable, String)
 *
 */
public class InMemoryDatabaseView implements DatabaseView {
	private final Hashtable hashtable;
	private final DatabaseView parent;
	private final Vector hooks = new Vector();

	/**
	 * Construct a new {@link DatabaseView} without any values.
	 */
	public InMemoryDatabaseView() {
		this.hashtable = new Hashtable();
		this.parent = null;
	}
	
	/**
	 * Construct a new {@link DatabaseView} backed by
	 * <code>hashtable</code>, which will be cloned,
	 * and not modified.
	 * @param hashtable A backing {@link Hashtable}.
	 */
	public InMemoryDatabaseView(Hashtable hashtable) {
		Enumeration keys = hashtable.keys();
		Hashtable clone = new Hashtable();
		try {
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) hashtable.get(key);
				clone.put(key, value);
			}
		} catch(ClassCastException e) {
			throw new IllegalArgumentException("Must use string-string hashtable " +
						"to create a HashtableStringMap");
		}
		this.hashtable = (Hashtable) hashtable.clone();
		this.parent = null;
	}
	
	/**
	 * Private constructor, used by {@link #spawnChild()}.
	 * @param parent The parent {@link DatabaseView} to check
	 * if the backing {@link Hashtable} has no entry.
	 */
	private InMemoryDatabaseView(DatabaseView parent) {
		this.parent = parent;
		this.hashtable = new Hashtable();
	}
	
	public DatabaseView spawnChild(String name) throws DatabasePersistException {
		DatabaseView child = new InMemoryDatabaseView(this);
		for(int i = 0 ; i < hooks.size() ; i ++) {
			((DatabaseViewHook) hooks.elementAt(i)).spawnChild(name, child);
		}
		return child;
	}
	
	public DatabaseView spawnChild(String name, String value) throws DatabasePersistException {
		DatabaseView child = new InMemoryDatabaseView(this);
		child.put(name, value);
		for(int i = 0 ; i < hooks.size() ; i ++) {
			((DatabaseViewHook) hooks.elementAt(i)).spawnChild(name, child);
		}
		return child;
	}
	
	public String get(String key) throws DatabaseReadException {
		if(hashtable.containsKey(key)) {
			return (String) hashtable.get(key);
		} else if(parent != null) {
			return parent.get(key);
		} else {
			return null;
		}
	}
	
	public void put(String key, String value) throws DatabasePersistException {
		for(int i = 0 ; i < hooks.size() ; i ++) {
			((DatabaseViewHook) hooks.elementAt(i)).put(key, value);
		}
		hashtable.put(key, value);
	}
	
	/**
	 * Show a {@link String} representation of this {@link InMemoryDatabaseView}
	 * in addition to all its parents.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(parent != null) {
			buf.append(parent.toString());
		}
		buf.append("<< " + hashtable.toString());
		return buf.toString();
	}

	public void addHook(DatabaseViewHook viewHook) {
		hooks.add(viewHook);
	}
}
