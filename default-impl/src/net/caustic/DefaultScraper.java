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
	
	/**
	 * 
	 * @param loadLocal Whether to allow the loading of local files as instructions at all.
	 * <code>True</code> to load locals, <code>false</code> otherwise.  It is <i>highly</i>
	 * recommended to use <code>false</code> if the compiled code is being run by
	 * other users/accessed on a network.
	 */
	public DefaultScraper(boolean loadLocal) {
		super(
				new DefaultRegexpCompiler(),
				new JavaNetUriResolver(),
				new DefaultURILoader(loadLocal),
				new DefaultHttpBrowser());
	}
}
