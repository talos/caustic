package net.microscraper.resources.definitions;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

/**
 * A one-to-one executable that can link to other executables,
 * both variables and leaves.  It cannot be piped, although
 * its value would be exported were its parent scraper to be piped."
 * @author john
 *
 */
public class Variable implements Executable, HasLeaves, HasVariables {
	private final Executable executable;
	private final HasLeaves hasLeaves;
	private final HasVariables hasVariables;
	
	/**
	 * A variable can be pulled from only a single scraper match.
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * @see Leaf#minMatch
	 * @see Leaf#maxMatch
	 */
	public final int match;
	
	public Variable(Executable execution, HasLeaves hasLeaves,
			HasVariables hasVariables,  int match) {
		this.executable = execution;
		this.match = match;
		this.hasLeaves = hasLeaves;
		this.hasVariables = hasVariables;
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

	public Variable[] getVariables() {
		return hasVariables.getVariables();
	}

	public Leaf[] getLeaves() {
		return hasLeaves.getLeaves();
	}
	
	private static final String MATCH = "match";
	
	/**
	 * Deserialize a {@link Variable} from a {@link Interfaces.JSON.Object}.
	 * @param location A {@link URI} that identifies the root of this variable's leaves.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An {@link Variable} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a Variable.
	 */
	protected static Variable deserialize(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			Executable executable = Executable.Deserializer.deserialize(jsonInterface, location, jsonObject); 
			HasLeaves hasLeaves = HasLeaves.Deserializer.deserialize(jsonInterface, location, jsonObject);
			HasVariables hasVariables = HasVariables.Deserializer.deserialize(jsonInterface, location, jsonObject);
			int match = jsonObject.getInt(MATCH);
			
			return new Variable(executable, hasLeaves, hasVariables, match);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link Variable}s from a {@link Interfaces.JSON.Array}.
	 * @param location A {@link URI} that identifies the root of this variables' leaves.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonArray Input {@link Interfaces.JSON.Array} array.
	 * @return An array of {@link Variable} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a Variable, or if the array is invalid.
	 */
	protected static Variable[] deserializeArray(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Array jsonArray)
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
