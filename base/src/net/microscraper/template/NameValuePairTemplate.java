package net.microscraper.template;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;


/**
 * A name-value pair with {@link Template} substitutions done for both name and value.
 * @author john
 *
 */
public class NameValuePairTemplate implements Substitutable {
	public final Template name;
	public final Template value;
	
	public NameValuePairTemplate(Template name, Template value) {
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
