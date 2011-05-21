package net.microscraper.server.resource;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

/**
 * A one-to-one {@link Parsable} that can link to other {@link Parsable}s,
 * both {@link Variable} and {@link Leaf}.  It cannot be piped, although
 * its value is exported when its parent scraper is piped.
 * @author john
 *
 */
public class Variable implements Parsable, HasLeaves, HasVariables {
	private final Parsable parsable;
	private final HasLeaves hasLeaves;
	private final HasVariables hasVariables;
	
	/**
	 * A variable can be pulled from only a single scraper match.
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * @see Leaf#minMatch
	 * @see Leaf#maxMatch
	 */
	public final int match;
	
	public Variable(Parsable parsable, HasLeaves hasLeaves,
			HasVariables hasVariables,  int match) {
		this.parsable = parsable;
		this.match = match;
		this.hasLeaves = hasLeaves;
		this.hasVariables = hasVariables;
	}
	
	public Link getParserLink() {
		return parsable.getParserLink();
	}

	public String getName() {
		return parsable.getName();
	}

	public boolean hasName() {
		return parsable.hasName();
	}

	public Variable[] getVariables() {
		return hasVariables.getVariables();
	}

	public Leaf[] getLeaves() {
		return hasLeaves.getLeaves();
	}
	
	private static final String MATCH = "match";
	
	/**
	 * Deserialize a {@link Variable} from a {@link JSONInterfaceObject}.
	 * @param location A {@link URI} that identifies the root of this variable's leaves.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link Variable} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a ExecutableVariable.
	 */
	protected static Variable deserialize(JSONInterface jsonInterface,
					URI location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Parsable executable = Parsable.Deserializer.deserialize(jsonInterface, location, jsonObject); 
			HasLeaves hasLeaves = HasLeaves.Deserializer.deserialize(jsonInterface, location, jsonObject);
			HasVariables hasVariables = HasVariables.Deserializer.deserialize(jsonInterface, location, jsonObject);
			int match = jsonObject.getInt(MATCH);
			
			return new Variable(executable, hasLeaves, hasVariables, match);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link Variable}s from a {@link JSONInterfaceArray}.
	 * @param location A {@link URI} that identifies the root of this variables' leaves.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonArray Input {@link JSONInterfaceArray} array.
	 * @return An array of {@link Variable} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a ExecutableVariable, or if the array is invalid.
	 */
	protected static Variable[] deserializeArray(JSONInterface jsonInterface,
					URI location, JSONInterfaceArray jsonArray)
				throws DeserializationException {
		Variable[] variables = new Variable[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length() ; i++ ) {
			try {
				variables[i] = Variable.deserialize(jsonInterface, location, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return variables;
	}
}
