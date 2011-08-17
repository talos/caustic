package net.microscraper.instruction;

/**
 * {@link DeserializationException} is thrown when there is a problem generating an
 * {@link Instruction} from serialized form.
 * @author talos
 *
 */
public class DeserializationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131401908426100287L;
	
	public DeserializationException(Throwable e) {
		super(e);
	}
	public DeserializationException(String msg) {
		super(msg);
	}
}
