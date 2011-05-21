package net.microscraper.server.resource;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;

public abstract class MustacheEncodedNameValuePair implements MustacheNameValuePair {
	public static EncodedNameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
						Variables variables, String encoding)
				throws MissingVariableException, UnsupportedEncodingException, MustacheTemplateException {
		EncodedNameValuePair[] encodedNameValuePairs = 
			new EncodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new EncodedNameValuePair(
					nameValuePairs[i].getName().compile(variables),
					nameValuePairs[i].getValue().compile(variables),
					encoding);
		}
		return encodedNameValuePairs;
	}
}
