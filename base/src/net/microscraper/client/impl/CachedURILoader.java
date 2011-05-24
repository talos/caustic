package net.microscraper.client.impl;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URILoader;

/**
 * Abstract class that implements caching for the {@link URILoader} interface.
 * Subclasses must override {@link #loadNew(URIInterface)}.
 * @author john
 *
 */
public abstract class CachedURILoader implements URILoader {
	private final Hashtable cache = new Hashtable();
	
	private boolean isCached(URIInterface location) {
		if(cache.containsKey(location))
			return true;
		return false;
	}
	
	private String getFromCache(URIInterface location) {
		return (String) cache.get(location);
	}
	
	private void addToCache(URIInterface location, String content) {
		cache.put(location, content);
	}

	public final String load(URIInterface location) throws IOException {
		if(!isCached(location)) {
			String content = loadNew(location);
			addToCache(location, content);
			return content;
		} else {
			return getFromCache(location);
		}
	}
	
	/**
	 * Must be overriden by subclass of {@link CachedURILoader}.
	 * @param location A {@link URIInterface} location to load.
	 * @return A String loaded from that location.
	 * @throws IOException if there was an error loading a String from that location.
	 */
	protected abstract String loadNew(URIInterface location) throws IOException;
}
