package net.microscraper.resources.definitions;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class Links {
	public final URI[] uris;
	public Links (URI[] uris) {
		this.uris = uris;
	}
	
	private static final String REF = "$REF";
	
	/**
	 * Deserialize a JSON Array into a {@link} instance.
	 * @param jsonInterface The {@link Interfaces.JSON} to use in processing JSON.
	 * @param jsonArray A {@link Interfaces.JSON.Array} of links.
	 * @return A {@link Links} instance.
	 * @throws DeserializationException If there is an error in the JSON, or one of the links is
	 * not a valid URI.
	 */
	public static Links deserializeArray(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Array jsonArray)
		throws DeserializationException {
		try {
			URI[] uris = new URI[jsonArray.length()];
			for(int i = 0 ; i < jsonArray.length(); i ++) {
				Interfaces.JSON.Object link = jsonArray.getJSONObject(i);
				try {
					uris[i] = new URI(link.getString(REF));
				} catch(URISyntaxException e) {
					throw new DeserializationException("Link '" + link.getString(REF) + "' is not a URI.");
				}
			}
			return new Links(uris);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e);
		}
	}
	
	public static Links blank() {
		return new Links(new URI[0]);
	}
}
