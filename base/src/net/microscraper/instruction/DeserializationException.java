package net.microscraper.instruction;

import net.microscraper.client.MicroscraperException;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONObjectInterface;

/**
 * {@link DeserializationException} is thrown when there is a problem generating an
 * {@link Executable} from a {@link JSONObjectInterface}.
 * @author talos
 *
 */
public class DeserializationException extends MicroscraperException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131401908426100287L;
	
	public DeserializationException(Throwable e, JSONObjectInterface jsonObject) {
		super(e);
	}
	public DeserializationException(Throwable e, JSONArrayInterface jsonArray, int index) {
		super(e);
	}
	public DeserializationException(String msg, JSONObjectInterface jsonObject) {
		super(msg);
	}
}
