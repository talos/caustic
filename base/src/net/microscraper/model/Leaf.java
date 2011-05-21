package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * A {@link Parsable} that can connect to other {@link Scraper} through {@link #getPipes},
 * and is one-to-many (even if it only has one result.)
 * Its executions do not implement {@link Variables}, because {@link Leaf} can be one-to-many.
 * @see Parsable
 * @see HasPipes
 * @author john
 *
 */
public class Leaf implements Parsable, HasPipes {
	private final Parsable executable;
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
	
	public Leaf(Parsable executable, HasPipes hasPipes, int minMatch, int maxMatch) {
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
	 * Deserialize an {@link Leaf} from a {@link JSONInterfaceObject}.
	 * @param location A {@link URI} that identifies the root of this leaf's links.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link Leaf} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * an ExecutableLeaf.
	 */
	protected static Leaf deserialize(JSONInterface jsonInterface,
					URI location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Parsable executable = Parsable.Deserializer.deserialize(jsonInterface, location, jsonObject); 
			HasPipes hasPipes = HasPipes.Deserializer.deserialize(jsonInterface, location, jsonObject);
			int minMatch = jsonObject.getInt(MIN_MATCH);
			int maxMatch = jsonObject.getInt(MAX_MATCH);
			
			return new Leaf(executable, hasPipes, minMatch, maxMatch);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	

	/**
	 * Deserialize an array of {@link Leaf}s from a {@link JSONInterfaceArray}.
	 * @param location A {@link URI} that identifies the root of this leaf's links.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonArray Input {@link JSONInterfaceArray} array.
	 * @return An array of {@link Leaf} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a ExecutableLeaf, or if the array is invalid.
	 */
	protected static Leaf[] deserializeArray(JSONInterface jsonInterface,
					URI location, JSONInterfaceArray jsonArray)
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
