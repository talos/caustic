package net.microscraper.database;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.PrematureRevivalException;

public abstract class AbstractResource {
	public final AbstractModel model;
	public final Reference ref;
	private final Hashtable attributes = new Hashtable();
	private final Hashtable references = new Hashtable();
	private final Hashtable relationships = new Hashtable();
	//public final Relationships relationships;
	
	private boolean revived = false;
	
	public AbstractResource(AbstractModel _model, Reference reference, JSON.Object json_obj) throws JSONInterfaceException {
		model = _model;
		ref = reference;
		for(int i = 0; i < model.attributes.length; i++) {
			String name = model.attributes[i];
			attributes.put(name, json_obj.getString(name));
		}
		String[] relationship_names = model.relationships.keys();
		for(int i = 0; i < relationship_names.length; i++) {
			String name = relationship_names[i];
			references.put(
					model.relationships.get(name),
					Reference.fromArray(json_obj.getJSONArray(name).toArray()));
		}
	}
	
	public AbstractResource(AbstractModel _model, Reference reference, Hashtable _attributes) {
		model = _model;
		ref = reference;
		Utils.hashtableIntoHashtable(_attributes, attributes);
	}

	public void revive(Database db) throws PrematureRevivalException {
		try {
			Enumeration e = references.keys();
			while(e.hasMoreElements()) {
				Relationship relationship = (Relationship) e.nextElement();
				Reference[] related_refs = (Reference[]) references.get(relationship);
				relationships.put(relationship, new Collection(model, related_refs, db));
			}
			revived = true;
		} catch(DatabaseException e) {
			throw new PrematureRevivalException();
		}
	}
	
	public String attribute_get(String name) {
		return (String) attributes.get(name);
	}
	public Reference[] related_refs(Relationship relationship) {
		return (Reference[]) references.get(relationship);
	}
	
	/**
	 * Retrieve a collection of the resource's relationship.  Object must have been revived
	 * in order to do this.
	 * @param relationship
	 * @return
	 * @throws IllegalStateException If the object has not been revived.
	 */
	public Collection relationship(Relationship relationship) throws IllegalStateException {
		if(revived == true) {
			return (Collection) relationships.get(relationship);
		} else {
			throw new IllegalStateException();
		}
	}
}
