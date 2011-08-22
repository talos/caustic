package net.microscraper.mustache;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Execution;
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
	public Execution sub(Variables variables) {
		Execution sub = Execution.combine(new Execution[] { name.sub(variables), value.sub(variables) });
		if(sub.isSuccessful()) {
			Object[] substituted = (Object[]) sub.getExecuted();
			return Execution.success(
					(NameValuePair) new BasicNameValuePair((String) substituted[0], (String) substituted[1]));
		}
		return sub;
	}
}
