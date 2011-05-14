package net.microscraper.resources.definitions;

import net.microscraper.client.MissingReference;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

/**
 * A string where Mustache-style demarcation denotes where Variables are taken out.
 * @author john
 *
 */
public final class MustacheTemplate implements Stringable, Executable {
	private final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 */
	public MustacheTemplate(String string) {
		this.string = string;
	}
	public String getString(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		try {
			return Mustache.compile(string, context);
		} catch(MissingReference e) {
			throw new ExecutionDelay(e, this);
		} catch(MustacheTemplateException e) { // Kill all executions if the template is no good.
			throw new ExecutionFatality(e, this);
		}
	}
}
