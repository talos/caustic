package net.microscraper.model;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

/**
 * Permits connections to a {@link ExecutableLeaf} executables.
 * @author john
 *
 */
public interface HasLeaves {
	
	/**
	 * 
	 * @return An array of associated {@link ExecutableLeaf} executables.
	 */
	public abstract ExecutableLeaf[] getLeaves();
	
	/**
	 * A helper class to deserialize 
	 * interfaces of {@link HasLeaves} using an inner constructor.
	 * Should only be instantiated inside {@link ExecutableVariable} or {@link ContextRoot}.
	 * @see ExecutableVariable
	 * @see ContextRoot
	 * @author john
	 *
	 */
	public static class Deserializer {
		private static final String LEAVES = "leaves";
		
		/**
		 * Protected, should be called only by {@link ExecutableVariable} or {@link ContextRoot}.
		 * Deserialize an {@link HasLeaves} from a {@link Interfaces.JSON.Object}.
		 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
		 * @param location A {@link URI} that identifies the root of these leaves' links.
		 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
		 * @return An {@link HasLeaves} instance.
		 * @throws DeserializationException If this is not a valid JSON serialization of
		 * a {@link HasLeaves}.
		 */
		protected static HasLeaves deserialize(Interfaces.JSON jsonInterface,
						URI location, Interfaces.JSON.Object jsonObject)
					throws DeserializationException {
			try {
				final ExecutableLeaf[] leaves = ExecutableLeaf.deserializeArray(jsonInterface, location, jsonObject.getJSONArray(LEAVES));				
				return new HasLeaves() {
					public ExecutableLeaf[] getLeaves() {
						return leaves;
					}
				};
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonObject);
			}
		}
	}
}
