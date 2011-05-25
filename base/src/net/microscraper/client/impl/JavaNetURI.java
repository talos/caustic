package net.microscraper.client.impl;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URIInterface;

public class JavaNetURI implements URIInterface {
	private final URI uri;
	public JavaNetURI(URI uri) {
		this.uri = uri;
	}
	/*
	public URLInterface toURL() throws NetInterfaceException {
		try {
			return new JavaNetURL(this.uri.toURL());
		} catch (MalformedURLException e) {
			throw new NetInterfaceException(e);
		}
	}*/
	public boolean isAbsolute() {
		return uri.isAbsolute();
	}
	public URIInterface resolve(String link) {
		return new JavaNetURI(uri.resolve(link));
	}
	public URIInterface resolve(URIInterface otherURI) {
		return new JavaNetURI(uri.resolve(otherURI.toString()));
	}
	public String getScheme() {
		return uri.getScheme();
	}
	public String getSchemeSpecificPart() {
		return uri.getSchemeSpecificPart();
	}
	public String getFragment() {
		return uri.getFragment();
	}
	public URIInterface resolveJSONFragment(String key) throws NetInterfaceException {
		try {
			return new JavaNetURI(
				new URI(uri.getScheme(),
						uri.getSchemeSpecificPart(),
						uri.getFragment() + '.' + key));
		} catch(URISyntaxException e) {
			throw new NetInterfaceException(e);
		}
	}
	public URIInterface resolveJSONFragment(int index) throws NetInterfaceException {
		try {
			return new JavaNetURI(
				new URI(uri.getScheme(),
						uri.getSchemeSpecificPart(),
						uri.getFragment() + '.' + Integer.toString(index)));
		} catch(URISyntaxException e) {
			throw new NetInterfaceException(e);
		}
	}
	public final String toString() {
		return uri.toString();
	}
}
