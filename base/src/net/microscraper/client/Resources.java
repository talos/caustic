package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.resources.Resource;
import net.microscraper.resources.ResourceDefinition;

public class Resources {
	private final Client client;
	private final Hashtable resources = new Hashtable();
	
	public Resources(Client client, Interfaces.JSON.Object jsonKlassObj) throws ResourceException {
		this.client = client;
		try {
			Iterator klassIter = jsonKlassObj.keys();
			while(klassIter.hasNext()) {
				String klass = (String) klassIter.next();
				ResourceDefinition definition = (ResourceDefinition) Class.forName("net.microscraper.resources.definitions." + klass).newInstance();
				Interfaces.JSON.Object jsonResourceObj = jsonKlassObj.getJSONObject(klass);
				Iterator resourceIter = jsonResourceObj.keys();
				
				while(resourceIter.hasNext()) {
					String fullName = (String) resourceIter.next();
					Resource resource = new Resource(this, definition, fullName, jsonResourceObj.getJSONObject(fullName));
					resources.put(resource.ref(), resource);
				}
			}
		} catch(JSONInterfaceException e) {
			throw new ResourceException(e);
		} catch(IllegalAccessException e) {
			throw new ResourceException(e);
		} catch(InstantiationException e) {
			throw new ResourceException(e);
		} catch (ClassNotFoundException e) {
			throw new ResourceException(e);
		}
	}
	
	public Resource get(Reference reference) throws ResourceException {
		Resource resource = (Resource) resources.get(reference);
		if(resource != null)
			return resource;
		else
			throw new ResourceException("Could not find " + reference.toString());
	}
	
	public static class ResourceException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4737299738008794427L;
		public ResourceException(String message) { super(message); }
		public ResourceException(Throwable e) { super(e); }
	}
}
