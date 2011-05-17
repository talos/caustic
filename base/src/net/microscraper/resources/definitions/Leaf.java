package net.microscraper.resources.definitions;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

/**
 * An executable that can connect to another scraper.
 * It cannot link to any variable executions, because it can be one-to-many.
 * @author john
 *
 */
public class Leaf implements Executable, HasPipes {
	private final Executable executable;
	private final HasPipes hasPipes;
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * @see #maxMatch
	 * @see Variable#match
	 */
	public final int minMatch;
	
	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * @see #minMatch
	 * @see Variable#match
	 */
	public final int maxMatch;
	
	public Leaf(Executable executable, HasPipes hasPipes, int minMatch, int maxMatch) {
		this.executable = executable;
		this.hasPipes = hasPipes;
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
	}
	
	public Link[] getPipes() {
		return hasPipes.getPipes();
	}

	public Link getParserLink() {
		return executable.getParserLink();
	}

	public String getName() {
		return executable.getName();
	}

	public boolean hasName() {
		return executable.hasName();
	}
	
	private static final String MIN_MATCH = "min";
	private static final String MAX_MATCH = "max";
	
	/**
	 * Deserialize an {@link Leaf} from a {@link Interfaces.JSON.Object}.
	 * @param location A {@link URI} that identifies the root of this leaf's links.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An {@link Leaf} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * an Leaf.
	 */
	protected static Leaf deserialize(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			Executable executable = Executable.Deserializer.deserialize(jsonInterface, location, jsonObject); 
			HasPipes hasPipes = HasPipes.Deserializer.deserialize(jsonInterface, location, jsonObject);
			int minMatch = jsonObject.getInt(MIN_MATCH);
			int maxMatch = jsonObject.getInt(MAX_MATCH);
			
			return new Leaf(executable, hasPipes, minMatch, maxMatch);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	

	/**
	 * Deserialize an array of {@link Leaf}s from a {@link Interfaces.JSON.Array}.
	 * @param location A {@link URI} that identifies the root of this leaf's links.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonArray Input {@link Interfaces.JSON.Array} array.
	 * @return An array of {@link Leaf} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a Leaf, or if the array is invalid.
	 */
	protected static Leaf[] deserializeArray(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Array jsonArray)
				throws DeserializationException {
		Leaf[] leaves = new Leaf[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length() ; i++ ) {
			try {
				leaves[i] = Leaf.deserialize(jsonInterface, location, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return leaves;
	}
}
