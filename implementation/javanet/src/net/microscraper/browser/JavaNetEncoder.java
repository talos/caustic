package net.microscraper.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.microscraper.util.Encoder;

public class JavaNetEncoder implements Encoder {

	public String encode(String stringToEncode, String encoding)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(stringToEncode, encoding);
	}
}
