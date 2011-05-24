package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.Ref;
import net.microscraper.server.resource.mixin.FindsMany;
import net.microscraper.server.resource.mixin.FindsOne;

/**
 * A one-to-one {@link Find} that can link to other {@link Find}s,
 * both {@link FindOne} and {@link FindMany}.  It cannot be piped, although
 * its value is exported when its parent scraper is piped.
 * @author john
 *
 */
public class FindOne extends Find implements FindsMany, FindsOne {
	
	/**
	 * The resource's identifier when deserializing.
	 */
	public static final String KEY = "find_one";
	
	private final FindsMany hasLeaves;
	private final FindsOne hasVariables;
	
	/**
	 * A {@link FindOne} finds a single scraper match.
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * @see FindMany#minMatch
	 * @see FindMany#maxMatch
	 */
	public final int match;
	
	public FindOne(Find find, FindsMany hasLeaves,
			FindsOne hasVariables,  int match) throws URIMustBeAbsoluteException {
		super(find);
		this.match = match;
		this.hasLeaves = hasLeaves;
		this.hasVariables = hasVariables;
	}
	
	public FindOne[] getFindOnes() {
		return hasVariables.getFindOnes();
	}

	public FindMany[] getFindMany() {
		return hasLeaves.getFindMany();
	}
	
	private static final String MATCH = "match";
	
	/**
	 * Deserialize a {@link FindOne} from a {@link JSONInterfaceObject}.
	 * @param location A {@link URIInterface}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link FindOne} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a {@link FindOne}.
	 */
	public static FindOne deserialize(URIInterface location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Find executable = Find.deserialize(location, jsonObject); 
			FindsMany hasLeaves = FindsMany.Deserializer.deserialize(location, jsonObject);
			FindsOne hasVariables = FindsOne.Deserializer.deserialize(location, jsonObject);
			int match = jsonObject.getInt(MATCH);
			
			return new FindOne(executable, hasLeaves, hasVariables, match);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link FindOne}s from a {@link JSONInterfaceArray}.
	 * @param location A {@link URIInterface} that identifies the root of this variables' leaves.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonArray Input {@link JSONInterfaceArray} array.
	 * @return An array of {@link FindOne} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a ExecutableVariable, or if the array is invalid.
	 */
	public static FindOne[] deserializeArray(URIInterface location, JSONInterfaceArray jsonArray)
				throws DeserializationException {
		FindOne[] variables = new FindOne[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length() ; i++ ) {
			try {
				variables[i] = FindOne.deserialize(location, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return variables;
	}
}
