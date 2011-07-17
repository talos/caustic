package net.microscraper.server;

import net.microscraper.client.interfaces.URIInterface;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public class Instruction {
	
	private final URIInterface location;
	
	/**
	 * 
	 * @return The resource's absolute {@link URIInterface} location.
	 */
	public final URIInterface getLocation() {
		return location;
	}
	
	/**
	 * {@link Instruction} must be initialized with a {@link URIInterface} location.
	 * @param location The {@link URIInterface} where this {@link Instruction} can be
	 * found.  Should be absolute.
	 * @throws InstantiationError If {@link #location} is not absolute.
	 */
	public Instruction(URIInterface location) {
		if(location.isAbsolute()) {
			this.location = location;
		} else {
			throw new InstantiationError();
		}
	}
}
