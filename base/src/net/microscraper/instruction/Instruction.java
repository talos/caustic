package net.microscraper.instruction;

import net.microscraper.MustacheTemplate;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public class Instruction {
	
	/**
	 * Key for {@link #getName()} value when deserializing from JSON.
	 */
	public static final String NAME = "name";
	
	private final MustacheTemplate name;
	private final JSONLocation location;
	
	/**
	 * 
	 * @return The {@link JSONLocation} where this {@link Instruction} is located.
	 */
	public final JSONLocation getLocation() {
		return location;
	}
	
	/**
	 * @return A {@link MustacheTemplate} attached to this particular {@link Find} {@link Instruction}.
	 * Is <code>null</code> if it has none.
	 * @see {@link #hasName}
	 */
	public final MustacheTemplate getName() {
		return name;
	}

	/**
	 * Whether this {@link Find} {@link Instruction} has a {@link #name}.
	 * @see {@link #name}
	 */
	public final boolean hasName() {
		if(getName() == null)
			return false;
		return true;
	}

	/**
	 *
	 * @param location A {@link JSONLocation} where this {@link Instruction} is located.
	 * @param name The {@link MustacheTemplate} to use as a name for this {@link Instruction}.
	 * Can be <code>null</code>.
	 */
	public Instruction(JSONLocation location, MustacheTemplate name) {
		this.location = location;
		this.name = name;
	}

	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param obj The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 */
	public Instruction(JSONInterfaceObject obj) throws DeserializationException {
		this.location = obj.getLocation();
		try {
			if(obj.has(NAME)) {
				name = new MustacheTemplate(obj.getString(NAME));
			} else {
				name = null;
			}
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, obj);
		}
	}
}
