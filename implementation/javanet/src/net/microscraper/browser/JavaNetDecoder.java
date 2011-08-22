package net.microscraper.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.util.Decoder;

public class JavaNetDecoder implements Decoder {

	public String decode(String stringToDecode, String encoding) 
			throws UnsupportedEncodingException {
		return URLDecoder.decode(stringToDecode, encoding);
	}
	
}
