package net.microscraper;

import net.microscraper.impl.browser.JavaNetBrowser;
import net.microscraper.impl.file.JavaIOFileLoader;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.impl.uri.JavaNetURIFactory;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.uri.URIFactory;

/**
 * Factory constructors for {@link Microscraper}.
 * @author realest
 *
 */
public class BasicMicroscraper {
	public static Microscraper to(Database database) {
		RegexpCompiler compiler = new JavaUtilRegexpCompiler();
		Browser browser = new JavaNetBrowser();
		JSONInterface jsonInterface = new JSONME();
		FileLoader fileLoader = new JavaIOFileLoader();
		URIFactory uriFactory = new JavaNetURIFactory(browser, fileLoader);
		return new Microscraper(compiler, browser, uriFactory, jsonInterface, database);
	}
}
