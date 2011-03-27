package net.microscraper.database.schema;


import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.AbstractResult;
import net.microscraper.client.AbstractResult.Result;
import net.microscraper.client.AbstractResult.ResultRoot;
import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.schema.Scraper.Model;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Data {
	private final Vector defaults = new Vector();
	private final Vector scrapers = new Vector();
	
	public Data(Resource resource) throws PrematureRevivalException {
		Utils.arrayIntoVector(resource.relationship(Model.DEFAULTS), defaults);
		Utils.arrayIntoVector(resource.relationship(Model.SCRAPERS), scrapers);
	}
	
	public ResultRoot scrape()
					throws PrematureRevivalException {
		ResultRoot root_result = new ResultRoot();
		//AbstractResult curRoot = root_result;
		for(int i = 0; i < scrapers.size(); i ++) {
			Variables variables = root_result.variables();
			for(int j = 0; i < defaults.size(); j ++) {
				try {
					new Default((Resource) defaults.elementAt(j), variables).simulate(root_result);
				} catch() {
					
				}
			}
			Scraper scraper = new Scraper((Resource) scrapers.elementAt(i));
		}
		return root_result;
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
