package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Utils;
import net.microscraper.database.DatabaseException.PrematureRevivalException;

public abstract class AbstractModel {
	protected abstract String _key();
	protected abstract String[] _attributes();
	protected abstract Relationship[] _relationships();
	
	public final String key;
	public final String[] attributes;
	public final Relationships relationships;
	private final Hashtable resources = new Hashtable();
	
	protected AbstractModel() {
		key = _key();
		attributes = _attributes();
		relationships = new Relationships(_relationships());
	}
	
	/**
	 * Fill a model with resources pulled from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public void inflate (JSON.Object json_obj) throws JSONInterfaceException {
		JSON.Iterator i = json_obj.keys();
		while(i.hasNext()) {
			String key = (String) i.next();
			Reference ref = new Reference(key);
			resources.put(ref, Resource.inflate(this, ref, json_obj.getJSONObject(key)) );
		}
	}
	
	public Resource get(Reference ref) throws PrematureRevivalException {
		return ((Resource) resources.get(ref));
	}
	
	public Resource[] all() throws PrematureRevivalException {
		Reference[] references = new Reference[resources.size()];
		Resource[] resources_ary = new Resource[resources.size()];
		Utils.hashtableKeys(resources, references);
		for(int i = 0; i < references.length; i++) {
			resources_ary[i] = get(references[i]);
		}
		return resources_ary;
	}
	
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
