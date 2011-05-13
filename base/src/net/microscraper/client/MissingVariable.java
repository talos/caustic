package net.microscraper.client;

public class MissingVariable extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8720790457856091375L;
	public final String missingVariable;
	public MissingVariable(String missingVariable) {
		super("Missing variable " + missingVariable);
		this.missingVariable = missingVariable;
	}
}
