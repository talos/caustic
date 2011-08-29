package net.microscraper.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.util.Decoder;

public class JavaNetDecoder implements Decoder {
	private final String encoding;
	
	/**
	 * Instantiate a {@link JavaNetDecoder} with a specific encoding.
	 * @param encoding The encoding to use.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 */
	public JavaNetDecoder(String encoding) throws UnsupportedEncodingException {
		this.encoding = encoding;
		URLDecoder.decode("", encoding);
	}
	
	public String decode(String stringToDecode)  {
		try {
			return URLDecoder.decode(stringToDecode, encoding);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Encoding should have been supported.");
		}
	}
	
}
