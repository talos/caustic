package net.microscraper.impl.uri;

import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.uri.URIFactory;
import net.microscraper.interfaces.uri.URIInterface;
import net.microscraper.interfaces.uri.URIInterfaceException;

/**
 * An implementation of {@link URIFactory} that uses {@link JavaNetURI}.
 * @author realest
 *
 */
public class JavaNetURIFactory implements URIFactory {
	private final Browser browser;
	private final FileLoader fileLoader;
	
	/**
	 * 
	 * @param browser The {@link Browser} that this factory's {@link URIInterface}'s
	 * will use to {@link URIInterface#load()}.
	 * @param fileLoader The {@link FileLoader} that this factory's {@link URIInterface}'s
	 * will use to {@link URIInterface#load()}.
	 */
	public JavaNetURIFactory(Browser browser, FileLoader fileLoader) {
		this.browser = browser;
		this.fileLoader = fileLoader;
	}
	
	public URIInterface fromString(String uri) throws URIInterfaceException {
		return new JavaNetURI(uri, browser, fileLoader);
	}
}
