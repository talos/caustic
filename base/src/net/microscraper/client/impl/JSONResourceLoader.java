package net.microscraper.client.impl;

import java.io.IOException;
import java.net.URI;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.execution.ResourceLoader;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.Page;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;
import net.microscraper.model.Scraper;

/**
 * Abstract class that implements caching and {@link ResourceLoader} interfaces.  All that must be overriden
 * is {@link JSONResourceLoader#obtainJSON}.
 * @author john
 *
 */
public abstract class JSONResourceLoader implements ResourceLoader {
	private final Interfaces.JSON jsonInterface;
	private final Hashtable cache = new Hashtable();
	
	public JSONResourceLoader(Interfaces.JSON jsonInterface) {
		this.jsonInterface = jsonInterface;
	}
	
	/**
	 * Obtain JSON from a URI.
	 * @param location the URI where the JSON text should be located.
	 * @return An {@link Interfaces.JSON.Object} parsed from the JSON text loaded from the location.
	 * @throws IOException If the location could not be loaded.
	 * @throws JSONInterfaceException If the JSON from the location could not be parsed.
	 */
	public abstract Interfaces.JSON.Object obtainJSON(URI location) throws IOException, JSONInterfaceException;
	
	public final Parser loadParser(Link link) throws IOException,
			DeserializationException {
		if(isCached(link.location)) {
			return (Parser) getFromCache(link.location);
		} else {
			try {
				Parser parser = Parser.deserialize(jsonInterface, link.location, obtainJSON(link.location));
				addToCache(link.location, parser);
				return parser;
			} catch (JSONInterfaceException e) {
				throw new IOException(e);
			}
		}
	}

	public final Scraper loadScraper(Link link) throws IOException,
			DeserializationException {
		if(isCached(link.location)) {
			return (Scraper) getFromCache(link.location);
		} else {
			try {
				return Scraper.deserialize(jsonInterface, link.location, obtainJSON(link.location));
			} catch (JSONInterfaceException e) {
				throw new IOException(e);
			}
		}
	}

	public final Page loadPage(Link link) throws IOException,
			DeserializationException {
		if(isCached(link.location)) {
			return (Page) getFromCache(link.location);
		} else {
			try {
				return Page.deserialize(jsonInterface, link.location, obtainJSON(link.location));
			} catch (JSONInterfaceException e) {
				throw new IOException(e);
			}
		}
	}
	
	private boolean isCached(URI location) {
		if(cache.containsKey(location))
			return true;
		return false;
	}
	
	private Resource getFromCache(URI location) {
		return (Resource) cache.get(location);
	}
	
	private void addToCache(URI location, Resource resource) {
		cache.put(location, resource);
	}
}
