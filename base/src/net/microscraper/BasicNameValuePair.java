package net.microscraper;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * @author john
 *
 */
public final class BasicNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
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
