package net.microscraper.instruction;

import net.microscraper.MicroscraperException;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceObject;

/**
 * {@link DeserializationException} is thrown when there is a problem generating an
 * {@link Executable} from a {@link JSONInterfaceObject}.
 * @author talos
 *
 */
public class DeserializationException extends MicroscraperException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131401908426100287L;
	
	public DeserializationException(Throwable e, JSONInterfaceObject jsonObject) {
		super(e);
	}
	public DeserializationException(Throwable e, JSONInterfaceArray jsonArray, int index) {
		super(e);
	}
	public DeserializationException(String msg, JSONInterfaceObject jsonObject) {
		super(msg);
	}
}
