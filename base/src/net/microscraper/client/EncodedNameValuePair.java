package net.microscraper.client;

import java.io.UnsupportedEncodingException;

/**
 * Interface to hold and retrieve encoded name value pairs.
 * @author john
 *
 */
public interface EncodedNameValuePair {
	public String getName(String encoding) throws UnsupportedEncodingException;
	public String getValue(String encoding) throws UnsupportedEncodingException;
}
