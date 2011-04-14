package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.RelationshipDefinition;

public class Model {
	private final Class klass;
	//private final ModelDefinition definition;
	private Model(Class klass /*, ModelDefinition definition*/) {
		this.klass = klass;
		//this.definition = definition;
	};

	/**
	 * Inflate a resource serialized in JSON.
	 * @param model
	 * @param reference
	 * @param json_obj
	 * @return
	 * @throws JSONInterfaceException
	 * @throws IllegalAccessException 
	 * @throws  
	 */
	public AbstractResource[] inflate(Database db, JSON.Object resources_json)
					throws JSONInterfaceException, InstantiationException, IllegalAccessException {
		AbstractResource[] resources = new AbstractResource[resources_json.length()];
		int k = 0;
		Iterator iter = resources_json.keys();
		while(iter.hasNext()) {
			// Create a blank instance of the resource.
			resources[k] = (AbstractResource) klass.newInstance();
			ModelDefinition definition = resources[k].definition();
			
			String key = (String) iter.next();
			JSON.Object resource_json = resources_json.getJSONObject(key);
			
			Hashtable attributes = new Hashtable();
			for(int i = 0; i < definition.attributes().length; i++) {
				String name = definition.attributes()[i];
				attributes.put(name, resource_json.getString(name));
			}
			
			Hashtable relationships = new Hashtable();
			RelationshipDefinition[] relationship_definitions = definition.relationships();
			for(int i = 0; i < relationship_definitions.length; i ++) {
				RelationshipDefinition relationship_def = relationship_definitions[i];
				String[] references = resource_json.getJSONArray(relationship_def.key).toArray();
				relationships.put(relationship_def.key, new Reference[references.length]);
				for(int j = 0; j < references.length; j ++ ) {
					((Reference[]) relationships.get(relationship_def.key))[j] =
						new Reference(relationship_def.target_model, references[j]);
				}
			}
			resources[k].initialize(db, key, attributes, relationships);
			k++;
		}
		return resources;
	}
	
	public static Model get(String model_name) {
		try {
			Class klass = Class.forName("net.microscraper.database.schema." + model_name);
			return get(klass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to instantiate model " + model_name, e);
		}
	}
	public static Model get(Class model_class) {
		try {
			return new Model(model_class /*, ((AbstractResource) model_class.newInstance()).definition()*/);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to instantiate model " + model_class.toString(), e);
		}
	}
}