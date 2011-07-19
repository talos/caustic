package net.microscraper.server;

import net.microscraper.client.DefaultNameValuePair;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.NameValuePair;
import net.microscraper.client.Variables;

/**
 * A name-value pair with Mustache substitutions done for both name and value.
 * @author john
 *
 */
public class MustacheNameValuePair {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
	
	public MustacheNameValuePair(MustacheTemplate name, MustacheTemplate value) {
		this.name = name;
		this.value = value;
	}
	
	public NameValuePair compile(Variables variables) throws MissingVariableException, MustacheTemplateException {
		return new DefaultNameValuePair(name.compile(variables), value.compile(variables));
	}
	
	public static NameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
				Variables variables)
		throws MissingVariableException, MustacheTemplateException {
		NameValuePair[] encodedNameValuePairs = 
			new NameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = nameValuePairs[i].compile(variables);
		}
		return encodedNameValuePairs;
	}
}
