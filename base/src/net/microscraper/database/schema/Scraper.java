package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.AbstractResult;
import net.microscraper.client.AbstractResult.Result;
import net.microscraper.client.Browser;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Log;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;


public class Scraper {
	private final Vector web_pages_to_load = new Vector();
	private final Vector prerequisite_scrapers = new Vector();
	//private final Vector source_strings_to_process = new Vector();
	private final String pattern_string;
	private final Integer match_number;
	public final boolean branch;
	public final Reference ref;
	
	public Scraper(Resource resource) throws PrematureRevivalException {
		ref = resource.ref;
		pattern_string = resource.attribute_get(Model.REGEXP);
		String match_number_string = resource.attribute_get(Model.MATCH_NUMBER);
		if(match_number_string == null) {
			match_number = null;
			branch = true;
		} else {
			match_number = Integer.parseInt(match_number_string);
			branch = false;
		}
		Utils.arrayIntoVector(resource.relationship(Model.WEB_PAGES), web_pages_to_load);
		Utils.arrayIntoVector(resource.relationship(Model.SOURCE_SCRAPERS), prerequisite_scrapers);
	}
	
	public void createResult(AbstractResult source, String value) {
		new Result(source, this, value);
	}
	
	public int execute(Variables variables, AbstractResult source_result)
					throws PrematureRevivalException, TemplateException, InterruptedException {
		try {
			Vector results = new Vector();
			Regexp regexp = new Regexp(pattern_string, variables);
			for(int i = 0; i < web_pages_to_load.size(); i++) {
				try {
					WebPage web_page = new WebPage((Resource) web_pages_to_load.elementAt(i), variables);
					try {
						// The Browser should handle caching, so we can re-load at our pleasure.
						results.addElement(processString(Client.browser.load(web_page), source_result));
					}  catch(BrowserException e) {
						Client.context().log.e(e);
					}
					// We still want to pull it out of the vector if there was a problem loading besides a MissingVariable.
					web_pages_to_load.removeElementAt(i);
					i--;
				} catch(MissingVariable e) {
					// Missing a variable, leave the web page resource in the vector.
					Client.context().log.i(e.getMessage());
				}
			}
			for(int i = 0; i < prerequisite_scrapers.size(); i++) {
				Resource scraper = (Resource) prerequisite_scrapers.elementAt(i);
				try {
					source_strings_to_process.addElement((String) variables.get(scraper.ref));
					prerequisite_scrapers.removeElementAt(i);
					i--;
				} catch(NullPointerException e) {
					// Missing a scraper, leave the prereq scraper in the vector.
					Client.context().log.i(new MissingVariable(scraper.ref.toString()).getMessage());
				}
			}
			Result[] results_ary = new Result[results.size()];
			results.copyInto(results_ary);
			return results_ary;
		} catch (MissingVariable e) { // Could not process the regular expression through Mustache.
			Client.context().log.i(e.getMessage());
			return new Result[] {};
		}
	}
	
	private Result processString(String source, Result source_result) {
		String input = (String) source_strings_to_process.elementAt(i);
		try {
			if(match_number == null) {
				String[] matches = regexp.pattern.allMatches(input);
			} else {
				String match = regexp.pattern.match(input, match_number);
			}
		} catch(NoMatches e) {
			Client.context().log.e(e);
		}
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "scraper";
	
		public static final String REGEXP = "regexp";
		public static final String MATCH_NUMBER = "match_number";
		public static final String PUBLISH = "publish";
		public static final String[] ATTRIBUTES = { REGEXP, MATCH_NUMBER, PUBLISH };
		
		public static final String WEB_PAGES = "web_pages";
		public static final String SOURCE_SCRAPERS = "source_scrapers";
		public final Relationship web_pages = new Relationship( WEB_PAGES, new WebPage.Model());
		public final Relationship source_scrapers = new Relationship( SOURCE_SCRAPERS, new Scraper.Model());
		public final Relationship[] relationships = { web_pages, source_scrapers };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}