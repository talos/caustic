package net.microscraper.client.impl;

import java.io.IOException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.execution.ResourceLoader;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.Page;
import net.microscraper.model.Parser;
import net.microscraper.model.Scraper;

public abstract class JSONResourceLoader implements ResourceLoader {
	private final Interfaces.JSON jsonInterface;
	public JSONResourceLoader(Interfaces.JSON jsonInterface) {
		this.jsonInterface = jsonInterface;
	}
	
	public abstract Interfaces.JSON.Object obtainJSON(Link link) throws IOException, JSONInterfaceException;
	
	public Parser loadParser(Link link) throws IOException,
			DeserializationException {
		try {
			return Parser.deserialize(jsonInterface, link.location, obtainJSON(link));
		} catch (JSONInterfaceException e) {
			throw new IOException(e);
		}
	}

	public Scraper loadScraper(Link link) throws IOException,
			DeserializationException {
		try {
			return Scraper.deserialize(jsonInterface, link.location, obtainJSON(link));
		} catch (JSONInterfaceException e) {
			throw new IOException(e);
		}
	}

	public Page loadPage(Link link) throws IOException,
			DeserializationException {
		try {
			return Page.deserialize(jsonInterface, link.location, obtainJSON(link));
		} catch (JSONInterfaceException e) {
			throw new IOException(e);
		}
	}
}
