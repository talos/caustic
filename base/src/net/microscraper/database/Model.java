package net.microscraper.database;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Attribute.Attributes;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Relationship.Relationships;

public class Model {
	private final Class klass;
	private Model(Class klass) {
		this.klass = klass;
	};
	
	/**
	 * Inflate a resource serialized in JSON.
	 * @param model
	 * @param reference
	 * @param json_obj
	 * @return
	 * @throws JSONInterfaceException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException
	 */
	public Resource[] inflate(Database db, JSON.Object resources_json)
					throws JSONInterfaceException, InstantiationException, IllegalAccessException {
		Resource[] resources = new Resource[resources_json.length()];
		int k = 0;
		Iterator iter = resources_json.keys();
		while(iter.hasNext()) {
			// Create a blank instance of the resource.
			resources[k] = (Resource) klass.newInstance();
			ModelDefinition definition = resources[k].definition();
			
			String key = (String) iter.next();
			
			JSON.Object resource_json = resources_json.getJSONObject(key);
			
			Attributes attributes = new Attributes();
			for(int i = 0; i < definition.attributes().length; i++) {
				AttributeDefinition def = definition.attributes()[i];
				Object rawValue = resource_json.get(def.name);
				if(rawValue.equals(null)) {
					attributes.put(def, null);
				} else {
					attributes.put(def, (String) rawValue);
				}
			}
			
			Relationships relationships = new Relationships();
			RelationshipDefinition[] relationship_definitions = definition.relationships();
			for(int i = 0; i < relationship_definitions.length; i ++) {
				RelationshipDefinition def = relationship_definitions[i];
				String[] references = resource_json.getJSONArray(def.key).toArray();
				for(int j = 0; j < references.length; j ++ ) {
					relationships.put(def, new Reference(def.target_model, references[j]));
				}
			}
			resources[k].initialize(key, attributes, relationships);
			k++;
		}
		return resources;
	}
	
	public static Model get(String model_name) {
		try {
			Class klass = Class.forName("net.microscraper.database.schema." + model_name);
			return get(klass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to instantiate model '" + model_name + "'", e);
		}
	}
	public static Model get(Class model_class) {
		try {
			return new Model(model_class /*, ((AbstractResource) model_class.newInstance()).definition()*/);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to instantiate model '" + model_class.toString() + "'", e);
		}
	}
	public String toString() {
		return klass.getName();
	}
	
	public interface ModelDefinition {
		public AttributeDefinition[] attributes();
		public RelationshipDefinition[] relationships();
	}
}