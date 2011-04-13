package net.microscraper.database.schema;

import net.microscraper.client.ResultSet;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Data {
	private final Resource[] defaults;
	private final Resource[] scrapers;
	private final Reference ref;
	
	public Data(Resource resource) throws ResourceNotFoundException, ModelNotFoundException {
		ref = resource.ref;
		defaults = resource.relationship(Model.DEFAULTS);
		scrapers = resource.relationship(Model.SCRAPERS);
	}
	
	public void scrape(ResultSet root_result) throws InterruptedException, ResourceNotFoundException, ModelNotFoundException {
		int prev_size = 0;
		Client.context().log.i("Scraping data " + ref.toString());
		while(root_result.size() != prev_size) {
			prev_size = root_result.size();
			for(int i = 0 ; i < defaults.length ; i ++) {
				Default _default = new Default(defaults[i]);
				try {
					_default.simulate(root_result);
				} catch (TemplateException e) {
					Client.context().log.w(e);
				}
			}
			for(int i = 0; i < scrapers.length ; i ++) {
				Scraper scraper = new Scraper(scrapers[i]);
				try {
					scraper.execute(root_result);
				} catch (TemplateException e) {
					Client.context().log.w(e);
				}
			}
			Client.context().log.i(Integer.toString(root_result.size()));
		}
	}
	
	public static class Model implements ModelDefinition {
		public static final String KEY = "data";
		
		public static final RelationshipDefinition DEFAULTS = new RelationshipDefinition( "defaults", Default.Model.KEY);
		public static final RelationshipDefinition SCRAPERS = new RelationshipDefinition( "scrapers", Scraper.Model.KEY);
		
		public String key() { return KEY; }
		public String[] attributes() {
			return new String[] { };
		}
		public RelationshipDefinition[] relationships() {
			return new RelationshipDefinition[] { DEFAULTS, SCRAPERS };
		}
	}
}
