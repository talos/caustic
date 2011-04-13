package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public class Database {
	private Hashtable resources = new Hashtable();
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Database(Interfaces.JSON.Object json_obj)
			throws JSONInterfaceException, InstantiationException, IllegalAccessException {
		Iterator iter = json_obj.keys();
		while(iter.hasNext()) {
			String model_name = (String) iter.next();
			Model model = Model.get(model_name);
			AbstractResource[] resources_ary = model.inflate(this, json_obj.getJSONObject(model_name));
			for(int i = 0 ; i < resources_ary.length ; i ++ ) {
				resources.put(resources_ary[i].ref(), resources_ary[i]);
			}
		}
	}
	public AbstractResource get(Reference reference) throws ResourceNotFoundException {
		AbstractResource resource = (AbstractResource) resources.get(reference);
		if(resource != null)
			return resource;
		else
			throw new ResourceNotFoundException(reference);
	}
}
