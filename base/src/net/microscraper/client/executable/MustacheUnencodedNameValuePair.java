package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.server.MustacheNameValuePair;

public abstract class MustacheUnencodedNameValuePair {
	
	public static UnencodedNameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
					Variables variables)
			throws MissingVariableException, MustacheTemplateException {
		UnencodedNameValuePair[] encodedNameValuePairs = 
			new UnencodedNameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = new UnencodedNameValuePair(
					nameValuePairs[i].name.compile(variables),
					nameValuePairs[i].value.compile(variables));
		}
		return encodedNameValuePairs;
	}
	
}
