package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

/**
 * An {@link Parsable} can be used to connect a {@link Parser} to a String source.
 * @author john
 *
 */
public interface Parsable {
	
	/**
	 * 
	 * @return A {@link Link} to the Parser this {@link Parsable} uses.
	 */
	public Link getParserLink();
	
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
	 * interfaces of {@link Parsable} using an inner constructor.
	 * Should only be instantiated inside {@link Variable} or {@link Leaf}.
	 * @see Variable
	 * @see Leaf
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String PARSER_LINK = "parser";
		private static final String NAME = "name";
		private static final String NAME_DEFAULT = null;
		
		/**
		 * Protected, should be called only by {@link Variable} or {@link Leaf}.
		 * Deserialize an {@link Parsable} from a {@link Interfaces.JSON.Object}.
		 * @param location A {@link URI} that identifies the root of this execution's links.
		 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
		 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
		 * @return An {@link Parsable} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * an Execution.
		 * @see Variable#deserialize
		 * @see Leaf#deserialize
		 */
		protected static Parsable deserialize(Interfaces.JSON jsonInterface,
						URI location, Interfaces.JSON.Object jsonObject)
					throws DeserializationException {
			try {
				final Link parserLink = Link.deserialize(jsonInterface, location, jsonObject.getJSONObject(PARSER_LINK));
				final boolean hasName;
				final String name;
				if(jsonObject.has(NAME)) {
					hasName = true;
					name = jsonObject.getString(NAME);
				} else {
					hasName = false;
					name = NAME_DEFAULT;
				}
				
				return new Parsable() {
					public Link getParserLink() {
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
