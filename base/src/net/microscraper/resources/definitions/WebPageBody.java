package net.microscraper.resources.definitions;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

/**
 * Abstract class to obtain the body of a WebPage.
 * @author realest
 *
 */
public abstract class WebPageBody extends WebPage implements Stringable, Problematic, Variable {
	private final Regexp[] terminates;
	private final Reference ref;
	protected WebPageBody(Reference ref, URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPageHead[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages);
		this.ref = ref;
		this.terminates = terminates;
	}
	
	public Reference getRef() {
		return ref;
	}
	
	/**
	 * Load the WebPage.  Terminates at the point that one of the terminates regular expressions
	 * matches the body.
	 * @return The loaded body.
	 * @throws ScrapingFatality 
	 * @throws ScrapingDelay 
	 */
	public final String getString(Scraper context) throws ScrapingDelay, ScrapingFatality {
		headPriorWebPages(context);
		try {
			return getResponse(context);
		} catch (DelayRequest e) {
			throw new ScrapingDelay(e, this);
		} catch (BrowserException e) {
			throw new ScrapingFatality(e, this);
		}
	}
	
	protected abstract String getResponse(Scraper context)
			throws ScrapingDelay, DelayRequest, BrowserException, ScrapingFatality;
	
	protected Pattern[] generateTerminates(Scraper context) throws ScrapingDelay, ScrapingFatality {
		Pattern[] patterns = new Pattern[terminates.length];
		for(int i = 0 ; i < terminates.length; i ++) {
			patterns[i] = terminates[i].getPattern(context);
		}
		return patterns;
	}
}
