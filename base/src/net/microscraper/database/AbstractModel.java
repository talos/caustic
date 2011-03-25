package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public abstract class AbstractModel {
	protected abstract String _key();
	protected abstract String[] _attributes();
	protected abstract Relationship[] _relationships();
	public final String key;
	public final String[] attributes;
	public final Relationships relationships;

	protected final AbstractModel _model = this;
	protected AbstractModel() {
		key = _key();
		attributes = _attributes();
		relationships = new Relationships(_relationships());
	}

	public abstract AbstractResource resource(Reference ref, Interfaces.JSON.Object json_obj)
				throws JSONInterfaceException;
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof AbstractModel))
			return false;
		return key.equals(((AbstractModel) obj).key);
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	public static class Relationships {
		Hashtable relationships = new Hashtable();
		public Relationships(Relationship[] _relationships) {
			for(int i = 0; i < _relationships.length; i++ ) {
				Relationship r = _relationships[i];
				relationships.put(r.key, r.model);
			}
		}
		public AbstractModel get(String relationship_name) {
			return (AbstractModel) relationships.get(relationship_name);
		}
		public String[] keys() {
			String[] keys = new String[relationships.size()];
			Utils.hashtableKeys(relationships, keys);
			return keys;
		}
	}
}
