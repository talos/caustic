package net.microscraper.uri;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.file.FileLoader;
import net.microscraper.uri.URIInterface;
import net.microscraper.uri.URIInterfaceException;

public class JavaNetURI implements URIInterface {

	private static final String HTTP_SCHEME = "http";
	private static final String FILE_SCHEME = "file";
	
	private final URI uri;
	private final Browser browser;
	private final FileLoader fileLoader;
	
	public JavaNetURI(String uriString, Browser browser, FileLoader fileLoader) throws URIInterfaceException {
		try {
			this.uri = new URI(uriString);
			this.browser = browser;
			this.fileLoader = fileLoader;
		} catch(URISyntaxException e) {
			throw new URIInterfaceException(e);
		}
	}
	
	public JavaNetURI(URI uri, Browser browser, FileLoader fileLoader) {
		this.uri = uri;
		this.browser = browser;
		this.fileLoader = fileLoader;
	}
	
	public URIInterface resolve(URIInterface otherLocation) {
			return new JavaNetURI( uri.resolve(otherLocation.toString()), browser, fileLoader );
	}
	
	public URIInterface resolve(String path) throws URIInterfaceException {
		return resolve(new JavaNetURI(path, browser, fileLoader));
	}
	
	public String getSchemeSpecificPart() {
		return uri.getSchemeSpecificPart();
	}
	
	public String toString() {
		return uri.toString();
	}
	
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof URIInterface) {
			return this.toString().equals(obj.toString());
		}
		return false;
	}

	public String load() throws IOException, URIInterfaceException {
		if(uri.getScheme() == null) {
			return fileLoader.load(this);
		} else if(uri.getScheme().equals(FILE_SCHEME)) {
			return fileLoader.load(this);
		} else if(uri.getScheme().equals(HTTP_SCHEME)) {
			int prevRateLimit = browser.getRateLimit();
			try {
				browser.setRateLimit(0);
				String response = browser.get(this.toString(), null, null, null);
				return response;
			} catch(BrowserException e) {
				throw new IOException(e);
			} finally {
				browser.setRateLimit(prevRateLimit);
			}
		} else {
			throw new URIInterfaceException("Cannot load scheme " + uri.getScheme());
		}
	}
}
