package net.microscraper.resources;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Resources;
import net.microscraper.client.Resources.ResourceException;
import net.microscraper.resources.definitions.Reference;

public class Relationships {
	private final Hashtable relationships = new Hashtable();
	
	public Relationships(AttributeDefinition[] definitions, Interfaces.JSON.Object jsonObj) throws JSONInterfaceException {
		for(int i = 0 ; i < definitions.length ; i++) {
			String relationshipName = definitions[i].name;
			Class targetClass = definitions[i].targetClass;
			Interfaces.JSON.Array jsonAry = jsonObj.getJSONArray(relationshipName);
			Reference[] referencesAry = new Reference[jsonAry.length()];
			for(int j = 0 ; j < referencesAry.length ; j ++) {
				referencesAry[j] = new Reference(targetClass, jsonAry.getString(j));
			}
			this.relationships.put(definitions[i].name, referencesAry);
		}
	}
	
	private Reference[] getReferences(AttributeDefinition def) {
		return (Reference[]) relationships.get(def.name);
	}
	
	public Resource[] get(Resources resources, AttributeDefinition def) throws ResourceException {
		Reference[] references = getReferences(def);
		Resource[] relatedResources = new Resource[references.length];
		
		for(int i = 0; i < references.length ; i ++) {
			relatedResources[i] = resources.get(references[i]);
		}
		return relatedResources;
	}
	
	public int getSize(AttributeDefinition def) {
		return getReferences(def).length;
	}
}