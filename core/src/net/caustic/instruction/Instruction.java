package net.caustic.instruction;

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
	 * Key for a self-reference.
	 */
	public static final String SELF = "$this";
	
	/**
	 * Key for description.
	 */
	public static final String DESCRIPTION = "description";
	
	public final String serialized;
	public final String uri;
	
	Instruction(String serialized, String uri) {
		this.serialized = serialized;
		this.uri = uri;
	}
}