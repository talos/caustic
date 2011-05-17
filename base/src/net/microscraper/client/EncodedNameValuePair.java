package net.microscraper.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Interface to hold and retrieve encoded name value pairs.
 * @author john
 *
 */
public final class EncodedNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	public EncodedNameValuePair(String name, String value, String encoding) throws UnsupportedEncodingException {
		this.name = URLDecoder.decode(name, encoding);
		this.value = URLDecoder.decode(value, encoding);
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
}
