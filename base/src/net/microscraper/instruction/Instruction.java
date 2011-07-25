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
	
	private final String location;
	
	/**
	 * 
	 * @return The resource's absolute URI.
	 */
	public final String getLocation() {
		return location;
	}
	
	/**
	 * {@link Instruction} can be initialized with a {@link String} of its location.
	 */
	public Instruction(String location) {
		this.location = location;
	}

	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param obj The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 */
	public Instruction(JSONInterfaceObject obj) throws DeserializationException {
		this.location = obj.getLocation().toString();
	}
}
