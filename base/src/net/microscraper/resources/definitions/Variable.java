package net.microscraper.resources.definitions;

/**
 * Variable resources can be used as the start point of a Link, and are stored under
 * their reference in an ExecutionContext.
 * @author realest
 *
 */
public interface Variable {
	
	/**
	 * 
	 * @return {@link Reference} The unique reference to refer to this resource.
	 */
	public Reference getRef();
}
