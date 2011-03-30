package net.microscraper.database;

import java.util.Hashtable;

public class Relationships {
	private final Relationship[] relationships_ary;
	private final Hashtable relationships = new Hashtable();
	private Relationships(Relationship[] _relationships, Database db) {
		relationships_ary = _relationships;
		for(int i = 0; i < _relationships.length; i ++) {
			relationships.put(_relationships[i].key, _relationships[i]);
		}
	}
	public Resource[] get(Relationship relationship) {
		
	}
	
	public static class Relationship {
		public final String key;
		public final String model_key;
		public Relationship(String _key, String _model_key) {
			key = _key;
			model_key = _model_key;
		}
	}
	/*public Relationship get(String relationship_key) {
		return (Relationship) relationships.get(relationship_key);
	}
	public Relationship[] all() {
		return relationships_ary;
	}*/
}

	/*
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
		if(!(obj instanceof Relationship))
			return false;
		return key.equals(((Relationship) obj).key);
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	public static class AbstractRelationship {
		public final String key;
		public final String model_key;
		public AbstractRelationship(String _key, String _model_key) {
			key = _key;
			model_key = _model_key;
		}
	}*/
