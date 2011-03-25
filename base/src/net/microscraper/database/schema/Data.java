package net.microscraper.database.schema;


import net.microscraper.client.Browser;
import net.microscraper.client.Interfaces;
import net.microscraper.client.ResultSet;
import net.microscraper.client.Interfaces.Regexp;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship;

public class Data extends AbstractModel {
	public static String KEY = "data";

	public static String[] ATTRIBUTES = { };
	
	public static Relationship DEFAULTS = new Relationship( "defaults", new Default());
	public static Relationship SCRAPERS = new Relationship( "scrapers", new Scraper());

	public static Relationship[] RELATIONSHIPS = { DEFAULTS, SCRAPERS };
	
	protected String _key() { return KEY; }
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }

	public Resource resource(Reference ref, Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		return new Resource(ref, json_obj);
	}
	
	public class Resource extends AbstractResource {
		public Resource(Reference ref, Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
			super(_model, ref, json_obj);
		}
		
		public boolean scrape(ResultSet result_set, Browser browser, Regexp regex_interface) {
			// TODO
			
		}
	}
}
