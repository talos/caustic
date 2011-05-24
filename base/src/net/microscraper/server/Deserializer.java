package net.microscraper.server;

import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;

/**
 * Abstract class whose implementations deserialize {@link Resource}s from JSON.
 * @author john
 *
 */
public abstract class JSONDeserializer {
	public Resource deserialize(JSONInterfaceObject jsonObject, URIInterface rootLocation, String[] path, String key) {
		
	}
	
	protected abstract Resource process(JSONInterfaceObject jsonObject, Resource resource);
}
