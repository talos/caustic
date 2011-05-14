package net.microscraper.client;

/**
 * Class to hold encoded name value pairs.
 * @author john
 *
 */
public class UnencodedNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	/**
	 * Instantiate an encoded name-value pair.
	 * @param name The name.  Will not be URLEncoded.
	 * @param value The value.  Will not be URLEncoded.
	 */
	public UnencodedNameValuePair(String name, String value) {
		this.name = name;
		this.value =value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
