package net.microscraper.util;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * Both {@link #getName()} and {@link #getValue()} can be <code>null</code>.
 * @author john
 *
 */
public class BasicNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	
	/**
	 * Create a new {@link BasicNameValuePair}.
	 * @param name The {@link String} value for {@link #getName()}.
	 * Can be <code>null</code>.
	 * @param valueThe {@link String} value for {@link #getValue()}.
	 * Can be <code>null</code>.
	 */
	public BasicNameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
}
