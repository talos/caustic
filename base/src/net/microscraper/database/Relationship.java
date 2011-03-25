package net.microscraper.database;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.DatabaseException.PrematureRevivalException;

public class Relationship {
	public final String key;
	public final AbstractModel model;
	private final Hashtable resource_references = new Hashtable();
	
	public Relationship(String _key, AbstractModel _model) {
		key = _key;
		model = _model;
	}
	
	public void put (Resource resource, Reference reference) {
		if(!resource_references.containsKey(resource.ref))
			resource_references.put(resource.ref, new Vector());
		((Vector) resource_references.get(resource.ref)).addElement(reference);
	}
	
	public Resource[] all(Resource resource) throws PrematureRevivalException {
		Vector references = (Vector) resource_references.get(resource);
		Resource[] resources = new Resource[references.size()];
		for(int i = 0; i < resources.length; i++) {
			resources[i] = model.get((Reference) references.elementAt(i));
		}
		return resources;
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
}
