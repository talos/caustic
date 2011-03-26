package net.microscraper.database.schema;


import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces;
import net.microscraper.client.ResultSet;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Collection;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Data {
	private Resource resource;
	
	public Data(Resource _resource) {
		resource = _resource;
	}
	
	public ResultSet scrape(Browser browser, Regexp regex_interface)
					throws PrematureRevivalException {
		ResultSet results = new ResultSet();
		Resource[] defaults = resource.relationship(Model.DEFAULTS);
		for(int i = 0; i < defaults.length; i ++) {
			new Default(defaults[i]).enterDefaults(results);
		}
		Resource[] scrapers = resource.relationship(Model.SCRAPERS);
		for(int i = 0; i < scrapers.length; i ++) {
			Scraper scraper = new Scraper(scrapers[i]);
			
		}
		return results;
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "data";
		
		public static final String[] ATTRIBUTES = { };
		
		public static final String DEFAULTS = "defaults";
		public static final String SCRAPERS = "scrapers";
		public final Relationship defaults = new Relationship( DEFAULTS, new Default.Model());
		public final Relationship scrapers = new Relationship( SCRAPERS, new Scraper.Model());
	
		public final Relationship[] relationships = { defaults, scrapers };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}
