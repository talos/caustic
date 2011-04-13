package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.client.Client;
import net.microscraper.client.Mustache;
import net.microscraper.client.Utils;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Default {
	private final String raw_value;
	private final Reference[] substituted_scraper_refs;
	protected Default(Resource resource) throws ResourceNotFoundException, ModelNotFoundException  {
		raw_value = resource.attribute_get(Model.VALUE);
		//substituted_scrapers = resource.relationship(Model.SCRAPERS);
		Resource[] substituted_scrapers = resource.relationship(Model.SCRAPERS);
		substituted_scraper_refs = new Reference[substituted_scrapers.length];
		for(int i = 0 ; i < substituted_scrapers.length ; i ++) {
			substituted_scraper_refs[i] = substituted_scrapers[i].ref;
		}
	}
	public Default(String name, String value) {
		substituted_scraper_refs = new Reference[] {
			new Reference(name)
		};
		raw_value = value;
	}
	/**
	 * Simulate the Default for the specified source.  This is done by inserting the value
	 * for each of the substituted scrapers.
	 * @param source
	 * @return True if any action has been taken, false otherwise.
	 * @throws PrematureRevivalException
	 * @throws TemplateException
	 */
	public void simulate(ResultSet source) throws TemplateException {
		try {
			String value = Mustache.compile(raw_value, source.variables());
			for(int i = 0; i < substituted_scraper_refs.length; i ++) {
				Client.context().log.i("Replacing '" + substituted_scraper_refs[i].toString() + "' with default value '" + value + "'");
				source.addOneToOne(substituted_scraper_refs[i], value);
				Client.context().log.i(source.variables().toString());
			}
		} catch (MissingVariable e) {
			Client.context().log.w(e);
		}
	}
	
	/**
	 * Generate an array of defaults from a form-style parameter string, like
	 * key1=val1&key2=val2 ...
	 * @param params_string
	 * @param encoding
	 * @return
	 */
	public static Default[] fromFormParams(String params_string, String encoding) {
		String[] params = Utils.split(params_string, "&");
		Default[] defaults = new Default[params.length];
				
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				defaults[i] =
					new Default(
								URLDecoder.decode(name_value[0], encoding),
								URLDecoder.decode(name_value[1], encoding)
								);
			}
			return defaults;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding " + encoding + " not supported: " + e.getMessage());
		}
	}
	
	public static class Model implements ModelDefinition {
		public static final String KEY = "default";
	
		public static final String VALUE = "value";
		
		public static final RelationshipDefinition SCRAPERS = new RelationshipDefinition( "scrapers", Scraper.Model.KEY );
		
		public String key() { return KEY; }
		public String[] attributes() { return new String[] { VALUE }; }
		public RelationshipDefinition[] relationships() {
			return new RelationshipDefinition[] { SCRAPERS };
		}
	}
}
