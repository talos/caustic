package net.microscraper.model;

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
public class ExecutionVariable implements Executable, HasLeaves, HasVariables {
	private final Executable executable;
	private final HasLeaves hasLeaves;
	private final HasVariables hasVariables;
	
	/**
	 * A variable can be pulled from only a single scraper match.
	 * 0-indexed, and negative numbers count backwards (-1 is last match.)
	 * @see ExecutionLeaf#minMatch
	 * @see ExecutionLeaf#maxMatch
	 */
	public final int match;
	
	public ExecutionVariable(Executable execution, HasLeaves hasLeaves,
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

	public ExecutionVariable[] getVariables() {
		return hasVariables.getVariables();
	}

	public ExecutionLeaf[] getLeaves() {
		return hasLeaves.getLeaves();
	}
	
	private static final String MATCH = "match";
	
	/**
	 * Deserialize a {@link ExecutionVariable} from a {@link Interfaces.JSON.Object}.
	 * @param location A {@link URI} that identifies the root of this variable's leaves.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An {@link ExecutionVariable} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a ExecutionVariable.
	 */
	protected static ExecutionVariable deserialize(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			Executable executable = Executable.Deserializer.deserialize(jsonInterface, location, jsonObject); 
			HasLeaves hasLeaves = HasLeaves.Deserializer.deserialize(jsonInterface, location, jsonObject);
			HasVariables hasVariables = HasVariables.Deserializer.deserialize(jsonInterface, location, jsonObject);
			int match = jsonObject.getInt(MATCH);
			
			return new ExecutionVariable(executable, hasLeaves, hasVariables, match);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link ExecutionVariable}s from a {@link Interfaces.JSON.Array}.
	 * @param location A {@link URI} that identifies the root of this variables' leaves.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonArray Input {@link Interfaces.JSON.Array} array.
	 * @return An array of {@link ExecutionVariable} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a ExecutionVariable, or if the array is invalid.
	 */
	protected static ExecutionVariable[] deserializeArray(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Array jsonArray)
				throws DeserializationException {
		ExecutionVariable[] variables = new ExecutionVariable[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length() ; i++ ) {
			try {
				variables[i] = ExecutionVariable.deserialize(jsonInterface, location, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return variables;
	}
}
