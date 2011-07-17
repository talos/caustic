package net.microscraper.server.instruction;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Instruction;

/**
 * The {@link URL} {@link Instruction} holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL extends Scraper {	
	
	private final MustacheTemplate template;
	/**
	 * @return A string that can be mustached and used as a URL.
	 */
	public final MustacheTemplate getTemplate() {
		return template;
	}
	
	private static final String URL = "url";

	/**
	 * Deserialize a {@link URL} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link URL} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link URL}.
	 * @throws IOException If there was an error loading a referenced {@link Instruction}.
	 */
	public URL(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		//super(jsonObject.getLocation());
		super(jsonObject);
		try {
			this.template = new MustacheTemplate(jsonObject.getString(URL));
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	public URL(URIInterface location, Page[] spawnPages, Scraper[] spawnScrapers,
			FindMany[] findManys, FindOne[] findOnes, MustacheTemplate urlTemplate) {
		super(location, spawnPages, spawnScrapers, findManys, findOnes);
		this.template = urlTemplate;
	}
	
	/**	
	 * Quick test to see whether a {@link JSONInterfaceObject} could deserialize into a {@link URL}.
	 * @param jsonObject The {@link JSONInterfaceObject} to test.
	 * @return <code>True</code> if the {@link JSONInterfaceObject} could contain
	 *  a URL, <code>false</code> otherwise.
	 */
	public static final boolean isURL(JSONInterfaceObject jsonObject) {
		if(jsonObject.has(URL)) {
			return true;
		}
		return false;
	}
}
