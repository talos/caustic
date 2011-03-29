package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Utils;
import net.microscraper.database.DatabaseException.PrematureRevivalException;

public abstract class Model {
	protected abstract String key();
	protected abstract String[] attributes();
	protected abstract Relationship[] relationships(/*Database db*/);
	/*
	public final String key;
	public final String[] attributes;
	public final Relationships relationships;
	*/
	protected static class Relationships {
		private final Relationship[] relationships_ary;
		private final Hashtable relationships = new Hashtable();
		private Relationships(Relationship[] _relationships) {
			relationships_ary = _relationships;
			for(int i = 0; i < _relationships.length; i ++) {
				relationships.put(_relationships[i].key, _relationships[i]);
			}
		}
		public Relationship get(String relationship_key) {
			return (Relationship) relationships.get(relationship_key);
		}
		public Relationship[] all() {
			return relationships_ary;
		}
	}
}
