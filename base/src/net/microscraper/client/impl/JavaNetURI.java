package net.microscraper.client.impl;

import java.net.MalformedURLException;
import java.net.URI;

import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URLInterface;

public class JavaNetURI implements URLInterface {
	private final URI uri;
	public JavaNetURI(URI uri) {
		this.uri = uri;
	}
	public URLInterface toURL() throws NetInterfaceException {
		try {
			return new JavaNetURL(this.uri.toURL());
		} catch (MalformedURLException e) {
			throw new NetInterfaceException(e);
		}
	}
}
