package net.microscraper.resources.definitions;

import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFailure;
import net.microscraper.resources.ScrapingFatality;

/**
 * A generic header to add to a WebPage request.
 * @author john
 *
 */
public final class GenericHeader extends AbstractHeader {
	public GenericHeader(MustacheTemplate name, MustacheTemplate value) {
		super(name, value);
	}
	
	/**
	 * Generate an {@link UnencodedNameValuePair}.
	 * @param context
	 * @return an {@link UnencodedNameValuePair}.
	 * @throws ScrapingDelay
	 * @throws ScrapingFailure
	 * @throws ScrapingFatality
	 */
	public UnencodedNameValuePair getNameValuePair(Scraper context)
			throws ScrapingDelay, ScrapingFatality {
		return new UnencodedNameValuePair(name.getString(context), value.getString(context));
	}
}