package net.microscraper.uri;

import net.microscraper.client.Browser;
import net.microscraper.file.FileLoader;
import net.microscraper.uri.UriFactory;
import net.microscraper.uri.Uri;
import net.microscraper.uri.Uri;

/**
 * An implementation of {@link UriFactory} that uses {@link JavaNetURI}.
 * @author realest
 *
 */
public class JavaNetURIFactory implements UriFactory {
	private final Browser browser;
	private final FileLoader fileLoader;
	
	/**
	 * 
	 * @param browser The {@link Browser} that this factory's {@link Uri}'s
	 * will use to {@link Uri#load()}.
	 * @param fileLoader The {@link FileLoader} that this factory's {@link Uri}'s
	 * will use to {@link Uri#load()}.
	 */
	public JavaNetURIFactory(Browser browser, FileLoader fileLoader) {
		this.browser = browser;
		this.fileLoader = fileLoader;
	}
	
	public Uri fromString(String uri) throws URIInterfaceException {
		return new JavaNetURI(uri, browser, fileLoader);
	}
}
