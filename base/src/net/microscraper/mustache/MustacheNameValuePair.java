package net.microscraper.mustache;

import net.microscraper.instruction.MissingVariableException;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Variables;


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
	
	public NameValuePair compile(Variables variables) throws MissingVariableException {
		return new BasicNameValuePair(name.compile(variables), value.compile(variables));
	}
	
	public static NameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
				Variables variables)
		throws MissingVariableException {
		NameValuePair[] encodedNameValuePairs = 
			new NameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = nameValuePairs[i].compile(variables);
		}
		return encodedNameValuePairs;
	}
}
