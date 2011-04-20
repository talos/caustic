package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class Database {

	private Hashtable resources = new Hashtable();
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void inflate(Interfaces.JSON.Object json_obj)
			throws JSONInterfaceException, InstantiationException, IllegalAccessException {
		Iterator iter = json_obj.keys();
		while(iter.hasNext()) {
			String model_name = (String) iter.next();
			Model model = Model.get(model_name);
			Resource[] resources_ary = model.inflate(this, json_obj.getJSONObject(model_name));
			for(int i = 0 ; i < resources_ary.length ; i ++ ) {
				resources.put(resources_ary[i].ref(), resources_ary[i]);
			}
		}
	}
	
	public Resource get(Reference reference) throws ResourceNotFoundException {
		Resource resource = (Resource) resources.get(reference);
		if(resource != null)
			return resource;
		else
			throw new ResourceNotFoundException(reference);
	}
	
	public static class DatabaseException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4737299738008794427L;
		public DatabaseException(String message) { super(message); }
		
	}

	public static class ResourceNotFoundException extends DatabaseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2178488029152395826L;
		public ResourceNotFoundException(Reference ref) {
			super("Could not find resource '" + ref.toString() + "'");
		}
	}
}
