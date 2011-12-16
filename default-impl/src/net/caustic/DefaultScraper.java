package net.caustic;

import net.caustic.Scraper;
import net.caustic.http.DefaultHttpBrowser;
import net.caustic.regexp.DefaultRegexpCompiler;
import net.caustic.uri.DefaultURILoader;
import net.caustic.uri.JavaNetUriResolver;

/**
 * A default implementation of {@link Scraper}, using
 * {@link JsonMEParser}, {@link DefaultRegexpCompiler}, {@link JavaNetUriResolver},
 * and {@link DefaultURILoader}.
 * @author realest
 *
 */
public class DefaultScraper extends Scraper {
	
	public DefaultScraper() {
		super(
				new DefaultRegexpCompiler(),
				new JavaNetUriResolver(),
				new DefaultURILoader(),
				new DefaultHttpBrowser());
	}
}
