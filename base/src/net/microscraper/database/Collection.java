package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public class Collection {
	public final AbstractModel model;
	private final Hashtable resources = new Hashtable();
	
	/**
	 * Fill a collection with resources pulled from a JSON object.
	 * @param _model
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Collection(AbstractModel _model, Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		model = _model;
		JSON.Iterator i = json_obj.keys();
		while(i.hasNext()) {
			String key = (String) i.next();
			Reference ref = new Reference(key);
			resources.put(ref, model.resource(ref, json_obj.getJSONObject(key)) );
		}
	}
	
	/**
	 * Fill a collection with resources pulled via reference from a Database.
	 * @param _model
	 * @param references
	 * @param db
	 * @throws ResourceNotFoundException 
	 * @throws ModelNotFoundException 
	 */
	public Collection(AbstractModel _model, Reference[] references, Database db)
				throws ModelNotFoundException, ResourceNotFoundException {
		model = _model;
		for(int i = 0; i < references.length; i++) {
			Reference ref = references[i];
			resources.put(ref, db.get(model, ref));
		}
	}
	
	public AbstractResource get(Reference ref) throws ResourceNotFoundException {
		try {
			return (AbstractResource) resources.get(ref);
		} catch(NullPointerException e) {
			throw new ResourceNotFoundException();
		}
	}
	
	public AbstractResource[] all() {
		AbstractResource[] resources_ary = new AbstractResource[resources.size()];
		Utils.hashtableValues(resources, resources_ary);
		return resources_ary;
	}
}
