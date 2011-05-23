package net.microscraper.server.resource;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.Ref;

/**
 * An {@link Find} can be used to connect a {@link Parser} to a String source.
 * @author john
 *
 */
public interface Find {
	
	/**
	 * 
	 * @return A {@link Ref} to the Parser this {@link Find} uses.
	 */
	public Ref getParserLink();
	
	/**
	 * 
	 * @return The String key for an optional mustache substitution to
	 * make with the results of this executable.  Is null if there
	 * is none, check with {@link #hasName}.
	 * @see #hasName
	 */
	public String getName();

	/**
	 * 
	 * @return Whether this Executable should be used in Mustache substitutions.
	 * @see #getName
	 */
	public boolean hasName();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link Find} using an inner constructor.
	 * Should only be instantiated inside {@link FindOne} or {@link FindMany}.
	 * @see FindOne
	 * @see FindMany
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String PARSER_LINK = "parser";
		private static final String NAME = "name";
		private static final String NAME_DEFAULT = null;
		
		/**
		 * Protected, should be called only by {@link FindOne} or {@link FindMany}.
		 * Deserialize an {@link Find} from a {@link JSONInterfaceObject}.
		 * @param location A {@link URI} that identifies the root of this execution's links.
		 * @param jsonInterface {@link JSONInterface} used to process JSON.
		 * @param jsonObject Input {@link JSONInterfaceObject} object.
		 * @return An {@link Find} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * an Execution.
		 * @see FindOne#deserialize
		 * @see FindMany#deserialize
		 */
		protected static Find deserialize(JSONInterface jsonInterface,
						URI location, JSONInterfaceObject jsonObject)
					throws DeserializationException {
			try {
				final Ref parserLink = Ref.deserialize(jsonInterface, location, jsonObject.getJSONObject(PARSER_LINK));
				final boolean hasName;
				final String name;
				if(jsonObject.has(NAME)) {
					hasName = true;
					name = jsonObject.getString(NAME);
				} else {
					hasName = false;
					name = NAME_DEFAULT;
				}
				
				return new Find() {
					public Ref getParserLink() {
						return parserLink;
					}
					public String getName() {
						return name;
					}
					public boolean hasName() {
						return hasName;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
