package net.microscraper.resources.definitions;

import net.microscraper.client.MissingReference;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

/**
 * A string where Mustache-style demarcation denotes where Variables are taken out.
 * @author john
 *
 */
public final class MustacheTemplate implements Stringable, Problematic {
	private final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 */
	public MustacheTemplate(String string) {
		this.string = string;
	}
	public String getString(Scraper context) throws ScrapingDelay, ScrapingFatality {
		try {
			return Mustache.compile(string, context);
		} catch(MissingReference e) {
			throw new ScrapingDelay(e, this);
		} catch(MustacheTemplateException e) { // Kill all executions if the template is no good.
			throw new ScrapingFatality(e, this);
		}
	}
	public String getName() {
		return string;
	}
}
