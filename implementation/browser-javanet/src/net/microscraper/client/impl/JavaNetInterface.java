package net.microscraper.client.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URLInterface;

public class JavaNetInterface implements NetInterface {
	private final Browser browser;
	public JavaNetInterface(Browser browser) {
		this.browser = browser;
	}
	
	public URIInterface makeURI(String uriString) throws NetInterfaceException {
		try {
			return new JavaNetURI(new URI(uriString));
		} catch(URISyntaxException e) {
			throw new NetInterfaceException(e);
		}
	}

	public URLInterface makeURL(String urlString) throws NetInterfaceException {
		try {
			return new JavaNetURL(new URL(urlString));
		} catch (MalformedURLException e) {
			throw new NetInterfaceException(e);
		}
	}

	public URIInterface makeURI(String scheme, String schemeSpecificPart, String fragment)
				throws NetInterfaceException {
		try {
			return new JavaNetURI(new URI(scheme, schemeSpecificPart, fragment));	
		} catch(URISyntaxException e) {
			throw new NetInterfaceException(e);
		}
	}

	public Browser getBrowser() {
		return browser;
	}
}
