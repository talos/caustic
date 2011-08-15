package net.microscraper.client;

import net.microscraper.browser.JavaNetBrowser;
import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.file.JavaIOFileLoader;
import net.microscraper.json.JSONMEParser;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.uri.JavaNetURIFactory;

/**
 * Basic implementation of {@link Microscraper} using
 * {@link JavaUtilRegexpCompiler}, {@link JavaNetBrowser},
 * {@link JavaNetURIFactory}, {@link JavaIOFileLoader},
 * and {@link JSONMEParser}.
 * @author realest
 *
 */
public class BasicMicroscraper extends Microscraper {
	public BasicMicroscraper(
			Database database) {
		super(new JavaUtilRegexpCompiler(),
			  new JavaNetBrowser(),
			  new JavaNetURIFactory(
					  new JavaNetBrowser(),
					  new JavaIOFileLoader()
					 ),
			  new JSONMEParser(), database);
	}
}
