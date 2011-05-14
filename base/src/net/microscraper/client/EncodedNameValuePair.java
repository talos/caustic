package net.microscraper.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Class to hold encoded name value pairs.
 * @author john
 *
 */
public class EncodedNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	/**
	 * Instantiate an encoded name-value pair.
	 * @param name The name.  Will be URLEncoded.
	 * @param value The value.  Will be URLEncoded.
	 * @param encoding The encoding to use.  Recommended UTF-8.
	 * @throws UnsupportedEncodingException if the encoding is not supported.
	 */
	public EncodedNameValuePair(String name, String value, String encoding) throws UnsupportedEncodingException {
		this.name = URLEncoder.encode(name, encoding);
		this.value = URLEncoder.encode(value, encoding);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
