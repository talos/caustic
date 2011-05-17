package net.microscraper.client;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * @author john
 *
 */
public final class UnencodedNameValuePair {
	private final String name;
	private final String value;
	public UnencodedNameValuePair(String name, String value) {
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
