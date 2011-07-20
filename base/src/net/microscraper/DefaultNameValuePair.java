package net.microscraper;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * @author john
 *
 */
public final class DefaultNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	public DefaultNameValuePair(String name, String value) {
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
