package net.microscraper.resources.definitions;

import net.microscraper.client.MissingVariable;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * A string where Mustache-style demarcation denotes where Variables are taken out.
 * @author john
 *
 */
public final class MustacheableString implements Parsable {
	private final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 */
	public MustacheableString(String string) {
		this.string = string;
	}
	public String parse(ExecutionContext context) throws ExecutionDelay,
			ExecutionFailure, ExecutionFatality {
		Variables variables = context.getVariables();
		try {
			return Mustache.compile(string, variables);
		} catch(MissingVariable e) {
			throw new ExecutionDelay(e, this, variables);
		} catch(MustacheTemplateException e) { // Kill all executions if the template is no good.
			throw new ExecutionFatality(e, this, variables);
		}
	}
}
