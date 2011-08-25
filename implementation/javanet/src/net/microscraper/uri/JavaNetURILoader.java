package net.microscraper.uri;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import net.microscraper.browser.JavaNetBrowser;
import net.microscraper.client.Browser;
import net.microscraper.file.FileLoader;
import net.microscraper.regexp.Pattern;

public class JavaNetURILoader implements URILoader {
	
	private final Browser browser;
	private final FileLoader fileLoader;

	/**
	 * Create a {@link JavaNetURILoader} using a specific browser.
	 * @param browser The {@link Browser} to load remote URIs with.
	 * @param fileLoader The {@link FileLoader} to load local URIs with.
	 */
	public JavaNetURILoader(Browser browser, FileLoader fileLoader) {
		this.browser = browser;
		this.fileLoader = fileLoader;
	}
	
	/**
	 * Create a {@link JavaNetURILoader} using a default {@link JavaNetBrowser}
	 * browser.
	 * @param fileLoader The {@link FileLoader} to load local URIs with.
	 */
	public JavaNetURILoader(FileLoader fileLoader) {
		this.browser = new JavaNetBrowser();
		this.fileLoader = fileLoader;
	}
	
	public String load(String uriStr) throws IOException {
		try {
			URI uri = new URI(uriStr);
			if(uri.getScheme() == null) {
				return fileLoader.load(uri.getSchemeSpecificPart());
			} else if(uri.getScheme().equalsIgnoreCase(UriResolver.FILE_SCHEME)) {
				return fileLoader.load(uri.getSchemeSpecificPart());
			} else {
				return browser.get(uriStr, new Hashtable(), new Pattern[] {});
			}
		} catch (URISyntaxException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
}
