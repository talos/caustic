package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.AbstractResult.ResultRoot;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Data {
	private final Vector defaults = new Vector();
	private final Vector scrapers = new Vector();
	
	public Data(Resource resource) throws PrematureRevivalException {
		Utils.arrayIntoVector(resource.relationship(Model.DEFAULTS), defaults);
		Utils.arrayIntoVector(resource.relationship(Model.SCRAPERS), scrapers);
	}
	
	public ResultRoot scrape() throws PrematureRevivalException, InterruptedException {
		ResultRoot root_result = new ResultRoot();
		
		// TODO: the way this should be done now is to iterate through the -web pages-
		/*
		for(int i = 0; i < scrapers.size(); i ++) {
			Variables variables = root_result.variables();
			
			// Before we try a new scraper, see if we can execute more defaults.
			for(int j = 0; i < defaults.size(); j ++) {
				try {
					try {
						new Default((Resource) defaults.elementAt(j), variables).simulate(root_result);
					} catch (TemplateException e) { // Bad template, we still pull it off the list.
						Client.context().log.e(e);
					}
					defaults.removeElementAt(j);
					j--;
				} catch(MissingVariable e) { // Missing variable, skip the default.
					Client.context().log.w(e);
				}
			}
			Scraper scraper = new Scraper((Resource) scrapers.elementAt(i));
			try {
				scraper.execute(variables, root_result);
			} catch(TemplateException e) {
				Client.context().log.e(e);
			}
		}
		return root_result;
		*/
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
