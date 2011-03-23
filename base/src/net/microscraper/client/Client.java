package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.Reference;

public class Client {
	private final Browser browser;
	public Client (Browser _browser) {
		browser = _browser;
	}
	
	public Hashtable evaluate(Collection collection) {
		Hashtable variables = new Hashtable();
		
		Enumeration e = collection.defaults.elements();
		while(e.hasMoreElements()) {
			Default _default = (Default) e.nextElement();
			for(int i = 0; i < _default.substitutes_for.length; i ++) {
				variables.put(_default.substitutes_for[i].title, _default.value);
			}
		}
		/*
		for(int i = 0; i < data.scrapers.length; i ++) {
			data.scrapers[i].
		}
		*/
		return variables;
	}
}
