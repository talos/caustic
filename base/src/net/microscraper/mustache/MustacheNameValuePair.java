package net.microscraper.mustache;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Substitution;
import net.microscraper.util.Variables;


/**
 * A name-value pair with Mustache substitutions done for both name and value.
 * @author john
 *
 */
public class MustacheNameValuePair implements Substitutable {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
	
	public MustacheNameValuePair(MustacheTemplate name, MustacheTemplate value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Substitutes to {@link NameValuePair}.
	 */
	public Substitution sub(Variables variables) {
		Substitution sub = Substitution.combine(new Substitution[] { name.sub(variables), value.sub(variables) });
		if(sub.isSuccessful()) {
			String[] substituted = (String[]) sub.getSubstituted();
			return Substitution.success((NameValuePair) new BasicNameValuePair(substituted[0], substituted[1]));
		}
		return sub;
	}
}
