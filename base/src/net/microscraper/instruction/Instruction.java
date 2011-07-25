package net.microscraper.instruction;

import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public class Instruction {
	
	private final JSONLocation location;
	
	/**
	 * 
	 * @return The {@link JSONLocation} where this {@link Instruction} is located.
	 */
	public final JSONLocation getLocation() {
		return location;
	}
	
	/**
	 * {@link Instruction} can be initialized with a {@link JSONLocation} of its location.
	 */
	public Instruction(JSONLocation location) {
		this.location = location;
	}

	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param obj The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 */
	public Instruction(JSONInterfaceObject obj) throws DeserializationException {
		this.location = obj.getLocation();
	}
}
