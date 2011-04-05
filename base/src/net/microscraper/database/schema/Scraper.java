package net.microscraper.database.schema;

import net.microscraper.client.AbstractResult;
import net.microscraper.client.AbstractResult.Result;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Scraper {
	private final Reference ref;
	private final Pattern pattern;
	private final Integer match_number;
	private final Resource[] web_pages;
	private final Resource[] source_scrapers;
	
	public Scraper(Resource resource) throws PrematureRevivalException {
		ref = resource.ref;
		pattern = Client.context().regexp.compile(resource.attribute_get(Model.REGEXP));
		Integer _match_number;
		try {
			_match_number = new Integer(resource.attribute_get(Model.MATCH_NUMBER));
		} catch(NumberFormatException e) {
			_match_number = null;
		}
		match_number = _match_number;
		web_pages = resource.relationship(Model.WEB_PAGES);
		source_scrapers = resource.relationship(Model.SOURCE_SCRAPERS);
	}
	
	public void execute(AbstractResult calling_result)
					throws PrematureRevivalException, TemplateException, InterruptedException {
		//AbstractResult[] calling_results = calling_result.livingResults();
		Client.context().log.i("Executing scraper " + ref.toString());
		
		//for(int i = 0; i < calling_results.length; i ++) {
		//	AbstractResult calling_result = calling_results[i];
		//	if(calling_result.contains(ref))
		//		continue;
		if(calling_result.contains(this.ref))
			return;
		for(int j = 0; j < web_pages.length; j++) {
			try {
				WebPage web_page = new WebPage((Resource) web_pages[j], calling_result.variables());
				try {
					// The Browser should handle caching, so we can re-load at our pleasure.
					String source_string = Client.context().browser.load(web_page);
					processInput(source_string, calling_result);
				}  catch(BrowserException e) {
					Client.context().log.e(e);
				}
			} catch(MissingVariable e) {
				// Missing a variable.
				Client.context().log.i(e.getMessage());
			}
		}
		for(int j = 0; j < source_scrapers.length; j++) {
			Resource source_scraper = source_scrapers[j];
			if(!calling_result.contains(source_scraper.ref)) {
				new Scraper(source_scraper).execute(calling_result);
			}
			if(calling_result.contains(source_scraper.ref)) {
				Result[] source_results = calling_result.get(source_scraper.ref);
				for(int k = 0 ; k < source_results.length; k++) {
					//Client.context().log.i(source_results[k].value);
					processInput(source_results[k].value, calling_result );
				}
			}
		}
		//}
	}
	
	private void processInput(String input, AbstractResult source_result) {
		try {
			if(match_number == null) {
				source_result.addOneToMany(ref, pattern.allMatches(input));
			} else {
				source_result.addOneToOne(ref, pattern.match(input, match_number.intValue()));
			}
		} catch(NoMatches e) {
			Client.context().log.w(e);
		}
	}
	
	public static class Model implements ModelDefinition {
		public static final String KEY = "scraper";
	
		public static final String REGEXP = "regexp";
		public static final String MATCH_NUMBER = "match_number";
		public static final String PUBLISH = "publish";
		
		public static final RelationshipDefinition WEB_PAGES = new RelationshipDefinition( "web_pages", WebPage.Model.KEY);
		public static final RelationshipDefinition SOURCE_SCRAPERS = new RelationshipDefinition( "source_scrapers", Scraper.Model.KEY);
		
		public String key() { return KEY; }
		public String[] attributes() {
			return new String[] { REGEXP, MATCH_NUMBER, PUBLISH };
		}
		public RelationshipDefinition[] relationships() {
			return new RelationshipDefinition[] {
				WEB_PAGES, SOURCE_SCRAPERS
			};
		}
	}
}