package net.caustic.scope;

/**
 * An interface for a unique identifier.
 * @author realest
 *
 */
public interface Scope {
	
	/**
	 * 
	 * @return The {@link Scope} expressed as a {@link String}.
	 */
	public String asString();
	
	/**
	 * 
	 * @return The {@link String} name of the scope.
	 */
	public String getName();
}
