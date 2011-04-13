package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public abstract class AbstractResource {
	private Reference ref;
	private Hashtable attributes;
	private Hashtable relationships;
	private Database db;
	
	public Reference ref() {
		return ref;
	}
	
	public AbstractResource initialize(Database db, String key, Hashtable attributes, Hashtable relationships) {
		this.db = db;
		this.ref = new Reference(Model.get(getClass()), key);
		this.attributes = attributes;
		this.relationships = relationships;
		return this;
	}
	
	protected String attribute_get(String name) {
		return (String) attributes.get(name);
	}
	
	/**
	 * Retrieve all the resources related through a specific Relationship.
	 * @param relationship
	 * @return
	 * @throws ModelNotFoundException 
	 * @throws ResourceNotFoundException
	 */
	protected AbstractResource[] relationship(RelationshipDefinition relationship)
				throws ResourceNotFoundException {
		Reference[] references = (Reference[]) relationships.get(relationship.key);
		AbstractResource[] resources = new AbstractResource[references.length];
		for(int i = 0; i < references.length ; i ++) {
			resources[i] = db.get(references[i]);
		}
		return resources;
	}
	
	public abstract ModelDefinition definition();
	public abstract void execute(ResultSet source_result)
		throws TemplateException, MissingVariable, ResourceNotFoundException;
}
