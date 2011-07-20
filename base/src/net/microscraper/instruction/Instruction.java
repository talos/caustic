package net.microscraper.instruction;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public class Instruction {
	
	private final String location;
	
	/**
	 * 
	 * @return The resource's absolute URI.
	 */
	public final String getLocation() {
		return location;
	}
	
	/**
	 * {@link Instruction} must be initialized with a {@link URIInterface} location.
	 * @param location The {@link URIInterface} where this {@link Instruction} can be
	 * found.  Should be absolute.
	 */
	public Instruction(String location) {
		this.location = location;
	}
}
