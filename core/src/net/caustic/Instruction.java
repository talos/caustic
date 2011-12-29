package net.caustic;

/**
 * An {@link Instruction} that has yet to be deserialized.  This allows for lazy evaluation
 * of children, in particular where a child cannot be deserialized until a variable is available
 * in the database.
 * @author talos
 *
 */
abstract class Instruction {

	/**
	 * Key for {@link Instruction#children} when deserializing from JSON.
	 */
	public static final String THEN = "then";
	
	/**
	 * Key for an object that will extend the current object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Key for metadata.
	 */
	public static final String DESCRIPTION = "description";
	
	public final String description;
	public final String uri;
	
	public Instruction(String description, String uri) {
		this.description = description;
		this.uri = uri;
	}
	
}