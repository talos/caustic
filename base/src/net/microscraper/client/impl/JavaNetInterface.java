package net.microscraper.client.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URLInterface;

public class JavaNetInterface implements NetInterface {

	public URIInterface getURI(String uriString) throws NetInterfaceException {
		try {
			return new JavaNetURI(new URI(uriString));
		} catch(URISyntaxException e) {
			throw new NetInterfaceException(e);
		}
	}

	public URLInterface getURL(String urlString) throws NetInterfaceException {
		try {
			return new JavaNetURL(new URL(urlString));
		} catch (MalformedURLException e) {
			throw new NetInterfaceException(e);
		}
	}

}
