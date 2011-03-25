package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.PrematureRevivalException;

public class Resource {
	public final AbstractModel model;
	public final Reference ref;
	private final Hashtable attributes;
		
	private Resource(AbstractModel _model, Reference _ref, Hashtable _attributes) {
		model = _model;
		ref = _ref;
		attributes = _attributes;
	}
	
	public String attribute_get(String name) {
		return (String) attributes.get(name);
	}
	
	/**
	 * Retrieve all the resources related through a specific Relationship.
	 * @param relationship
	 * @return
	 * @throws PrematureRevivalException If the object has not been revived.
	 */
	public Resource[] relationship(Relationship relationship) throws PrematureRevivalException {
		return relationship.all(this);
	}
	
	/**
	 * Retrieve all the resources related through a specific Relationship.
	 * @param relationship_key
	 * @return
	 * @throws PrematureRevivalException If the object has not been revived.
	 */
	public Resource[] relationship(String relationship_key) throws PrematureRevivalException {
		return relationship((Relationship) model.relationships.get(relationship_key));
	}
	
	/**
	 * Inflate a resource serialized in JSON.
	 * @param model
	 * @param reference
	 * @param json_obj
	 * @return
	 * @throws JSONInterfaceException
	 */
	public static Resource inflate(AbstractModel model, Reference reference, JSON.Object json_obj)
					throws JSONInterfaceException {
		Hashtable attributes = new Hashtable();
		for(int i = 0; i < model.attributes.length; i++) {
			String name = model.attributes[i];
			attributes.put(name, json_obj.getString(name));
		}
		Resource resource = new Resource(model, reference, attributes);
		Relationship[] relationships = model.relationships.all();
		for(int i = 0; i < relationships.length; i ++) {
			Relationship relationship = relationships[i];
			String[] references = json_obj.getJSONArray(relationship.key).toArray();
			for(int j = 0; j < references.length; j ++ ) {
				relationship.put(resource, new Reference(references[j]));
			}
		}
		return resource;
	}
}
