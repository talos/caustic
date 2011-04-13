package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.client.ResultSet;
import net.microscraper.client.Utils;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet.Result;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.Model;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.RelationshipDefinition;

public class Scraper extends AbstractResource {
	/*private final Reference ref;
	private final Pattern pattern;
	private final Integer match_number;
	private final Resource[] web_pages;
	private final Resource[] source_scrapers;
	
	public Scraper(Resource resource) throws ResourceNotFoundException, ModelNotFoundException {
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
	*/
	public String[] execute(ResultSet calling_result)
					throws TemplateException, ResourceNotFoundException {
		AbstractResource[] web_pages = relationship(WEB_PAGES);
		AbstractResource[] source_scrapers = relationship(SOURCE_SCRAPERS);
		AbstractResource[] defaults = relationship(DEFAULTS);
		if(calling_result.contains(this.ref()))
			return calling_result.get(this.ref());
		for(int j = 0; j < web_pages.length; j++) {
			try {
				/*WebPage web_page = new WebPage((Resource) web_pages[j], calling_result.variables());
				try {
					// The Browser should handle caching, so we can re-load at our pleasure.
					String source_string = Client.context().browser.load(web_page);
					processInput(source_string, calling_result);
				}  catch(BrowserException e) {
					Client.context().log.e(e);
				}*/
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
	}
	
	private void processInput(String input, ResultSet source_result) {
		Integer match_number;
		try {
			match_number = new Integer(attribute_get(MATCH_NUMBER));
		} catch(NumberFormatException e) {
			match_number = null;
		}
		Pattern pattern = Client.context().regexp.compile(attribute_get(REGEXP));
		try {
			if(match_number == null) {
				source_result.addOneToMany(ref(), pattern.allMatches(input));
			} else {
				source_result.addOneToOne(ref(), pattern.match(input, match_number.intValue()));
			}
		} catch(NoMatches e) {
			Client.context().log.w(e);
		}
	}

	/**
	 * Simulate defaults from a form-style parameter string, like
	 * key1=val1&key2=val2 ...
	 * @param params_string
	 * @param encoding
	 * @return
	 */
	public static void simulateFromFormParams(String params_string, String encoding, ResultSet source) {
		String[] params = Utils.split(params_string, "&");
		Default[] defaults = new Default[params.length];
		
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				String name = URLDecoder.decode(name_value[0], encoding);
				String value = URLDecoder.decode(name_value[1], encoding);
				source.addOneToOne(new Reference(name), value);
			}
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding " + encoding + " not supported: " + e.getMessage());
		}
	}
	private static final String REGEXP = "regexp";
	private static final String MATCH_NUMBER = "match_number";
	private static final RelationshipDefinition WEB_PAGES =
		new RelationshipDefinition( "web_pages", WebPage.class );
	private static final RelationshipDefinition SOURCE_SCRAPERS =
		new RelationshipDefinition( "source_scrapers", Scraper.class);
	private static final RelationshipDefinition DEFAULTS =
		new RelationshipDefinition( "defaults", Default.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() {
				return new String[] { REGEXP, MATCH_NUMBER };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					WEB_PAGES, SOURCE_SCRAPERS
				};
			}
		};
	}
}