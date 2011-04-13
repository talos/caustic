package net.microscraper.database;
/*
import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public class Resource {
	public final ModelDefinition model;
	public final Reference ref;
	private final Hashtable attributes;
	private final Hashtable relationships;
	private final Database db;
	
	protected Resource(Database _db, ModelDefinition _model, Reference _ref, Hashtable _attributes, Hashtable _relationships) {
		db = _db;
		model = _model;
		ref = _ref;
		attributes = _attributes;
		relationships = _relationships;
	}
	
	public String attribute_get(String name) {
		return (String) attributes.get(name);
	}
	
	public Resource[] relationship(RelationshipDefinition relationship) throws ResourceNotFoundException, ModelNotFoundException {
		//return relationship.all(this);
		Reference[] references = (Reference[]) relationships.get(relationship.key);
		Resource[] resources = new Resource[references.length];
		for(int i = 0; i < references.length ; i ++) {
			resources[i] = db.get(relationship.model_key, references[i]);
		}
		return resources;
	}
	
	public static Resource inflate(Database db, ModelDefinition model, Reference reference, JSON.Object json_obj)
					throws JSONInterfaceException {
		Hashtable attributes = new Hashtable();
		for(int i = 0; i < model.attributes().length; i++) {
			String name = model.attributes()[i];
			attributes.put(name, json_obj.getString(name));
		}
		Hashtable relationships = new Hashtable();
		RelationshipDefinition[] relationship_definitions = model.relationships();
		for(int i = 0; i < relationship_definitions.length; i ++) {
			RelationshipDefinition relationship_def = relationship_definitions[i];
			String[] references = json_obj.getJSONArray(relationship_def.key).toArray();
			relationships.put(relationship_def.key, new Reference[references.length]);
			for(int j = 0; j < references.length; j ++ ) {
				((Reference[]) relationships.get(relationship_def.key))[j] = new Reference(references[j]);
			}
		}
		
		return new Resource(db, model, reference, attributes, relationships);
	}
}
*/
