package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.PrematureRevivalException;

public class Collection {
	private final Hashtable resources = new Hashtable();
	
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
		if(!(obj instanceof Collection))
			return false;
		return key.equals(((Collection) obj).key);
	}
	
	public int hashCode() {
		return key.hashCode();
	}
}
