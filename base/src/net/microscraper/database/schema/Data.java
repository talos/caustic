package net.microscraper.database.schema;

import net.microscraper.client.AbstractResult.ResultRoot;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Data {
	private final Resource[] defaults;
	private final Resource[] scrapers;
	
	public Data(Resource resource) throws PrematureRevivalException {
		defaults = resource.relationship(Model.DEFAULTS);
		scrapers = resource.relationship(Model.SCRAPERS);
	}
	
	public void scrape(ResultRoot root_result)
					throws PrematureRevivalException, InterruptedException {
		int prev_size = 0;
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
		}
		//return root_result;
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "data";
		
		public static final String DEFAULTS = "defaults";
		public static final String SCRAPERS = "scrapers";
		
		protected String _key() { return KEY; }
		protected String[] _attributes() {
			return new String[] { };
		}
		protected Relationship[] _relationships() {
			return new Relationship[] {
					new Relationship( DEFAULTS, new Default.Model()),
					new Relationship( SCRAPERS, new Scraper.Model())
			};
		}
	}
}
