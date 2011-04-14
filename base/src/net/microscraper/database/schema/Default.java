package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Default extends AbstractResource {	
	/**
	 * Simulate the Default for the specified source.
	 * @param source
	 * @return True if any action has been taken, false otherwise.
	 * @throws PrematureRevivalException
	 * @throws TemplateException
	 * @throws MissingVariable 
	 * @throws ResourceNotFoundException 
	 */
	public Result[] execute(Result caller) throws TemplateException, MissingVariable, ResourceNotFoundException {
		String raw_value = attribute_get(VALUE);
		AbstractResource[] scrapers = relationship(SUBSTITUTED_SCRAPERS);
		Result[] results = new Result[scrapers.length];
		for(int i = 0 ; i < scrapers.length ; i ++) {
			results[i] = new Result(caller, scrapers[i], scrapers[i].ref().title, Mustache.compile(raw_value, caller.variables()));
		}
		return results;
	}

	/**
	 * Simulate defaults from a form-style parameter string, like
	 * key1=val1&key2=val2 ...
	 * @param params_string
	 * @param encoding
	 * @return
	 * @throws MissingVariable 
	 * @throws TemplateException 
	 */
	public static Result[] simulateFromFormParams(String params_string, String encoding, Result caller)
				throws TemplateException, MissingVariable {
		String[] params = Utils.split(params_string, "&");
		Result[] results = new Result[params.length];
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				String name = URLDecoder.decode(name_value[0], encoding);
				String value = URLDecoder.decode(name_value[1], encoding);
				results[i] = new Result(caller, new Scraper(), name, Mustache.compile(value, caller.variables()));
			}
			return results;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding " + encoding + " not supported: " + e.getMessage());
		}
	}
	
	private static final String VALUE = "value";
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() { return new String[] { VALUE }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
			}
		};
	}
}
