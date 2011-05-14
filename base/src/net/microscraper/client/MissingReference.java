package net.microscraper.client;

import net.microscraper.resources.definitions.Reference;

/**
 * Exception to indicate that a Ref was not available.
 * @author realest
 *
 */
public class MissingReference extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8720790457856091375L;
	public final Reference ref;
	public MissingReference(Reference ref) {
		super("Missing reference " + ref);
		this.ref = ref;
	}
}
