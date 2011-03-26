package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet;
import net.microscraper.client.ResultSet.Result;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;


public class Scraper {
	private final Resource resource;
	private final Vector web_pages_to_load = new Vector();
	private final Vector prerequisite_scrapers = new Vector();
	private final Vector source_strings_to_process = new Vector();
	public Scraper(Resource _resource) throws PrematureRevivalException {
		resource = _resource;
		Utils.arrayIntoVector(resource.relationship(Model.WEB_PAGES), web_pages_to_load);
		Utils.arrayIntoVector(resource.relationship(Model.SOURCE_SCRAPERS), prerequisite_scrapers);
	}
	public Result[] execute(Browser browser, Hashtable variables, Interfaces.Regexp regexp_interface)
					throws PrematureRevivalException, TemplateException, InterruptedException {
		for(int i = 0; i < web_pages_to_load.size(); i++) {
			try {
				WebPage web_page = new WebPage((Resource) web_pages_to_load.elementAt(i), variables, regexp_interface);
				String source_string = browser.load(web_page);
				source_strings_to_process.addElement(source_string);
			} catch(MissingVariable e) {
				// Missing a variable, leave the web page resource in the vector.
			} catch(BrowserException e) {
				// Something went wrong loading the web page, pull it out of the vector.
				// TODO: logging?
				web_pages_to_load.removeElementAt(i);
			}
		}
		return null;
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