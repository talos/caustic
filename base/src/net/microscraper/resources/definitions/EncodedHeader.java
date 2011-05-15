package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

public class EncodedHeader extends AbstractHeader {
	public EncodedHeader(MustacheTemplate name, MustacheTemplate value) {
		super(name, value);
	}

	public EncodedNameValuePair getNameValuePair(Scraper context)
			throws ScrapingDelay, ScrapingFatality, UnsupportedEncodingException {
		return new EncodedNameValuePair(name.getString(context), value.getString(context), context.getEncoding());
	}
}
