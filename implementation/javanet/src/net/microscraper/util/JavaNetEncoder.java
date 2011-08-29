package net.microscraper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.microscraper.util.Encoder;

public class JavaNetEncoder implements Encoder {
	private final String encoding;
	
	/**
	 * Instantiate a {@link JavaNetEncoder} with a specific encoding.
	 * @param encoding The encoding to use.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 */
	public JavaNetEncoder(String encoding) throws UnsupportedEncodingException {
		this.encoding = encoding;
		URLDecoder.decode("", encoding);
	}
	
	public String encode(String stringToEncode)  {
		try {
			return URLEncoder.encode(stringToEncode, encoding);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Encoding should have been supported.");
		}
	}
}
