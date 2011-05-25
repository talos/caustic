package net.microscraper.client.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.server.MustacheNameValuePair;

public abstract class MustacheEncodedNameValuePair {
	public static EncodedNameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
						Variables variables, String encoding)
				throws MissingVariableException, UnsupportedEncodingException, MustacheTemplateException {
		EncodedNameValuePair[] encodedNameValuePairs = 
			new EncodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new EncodedNameValuePair(
					nameValuePairs[i].name.compile(variables),
					nameValuePairs[i].value.compile(variables),
					encoding);
		}
		return encodedNameValuePairs;
	}
}
