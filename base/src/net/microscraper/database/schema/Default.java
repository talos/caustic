package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Default extends AbstractResource {	
	private String name;
	private String value;
	public Default() {};
	public Default(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	protected boolean branchesResults() throws FatalExecutionException {
		return false;
	}
	
	private final Hashtable results_hash = new Hashtable();
	private final Hashtable retry_scrapers_for_caller = new Hashtable();
	public Result[] getResults(AbstractResult caller) throws FatalExecutionException {
		Vector results; // = new Vector();
		Vector retry_scrapers;
		if(results_hash.containsKey(caller)) {
			results = (Vector) results_hash.get(caller);
			retry_scrapers = (Vector) retry_scrapers_for_caller.get(caller);
			// Retry certain failed portions.
			for(int i = 0 ; i < retry_scrapers.size() ; i ++ ) {
				Scraper scraper = (Scraper) retry_scrapers.elementAt(i);
				try {
					results.addElement(successForScraper(caller, scraper));
					retry_scrapers.remove(i);
					i--;
				} catch(MissingVariable e) {
					// failed second time in a row
				} catch(TemplateException e) {
					throw new FatalExecutionException(e);
				}
			}
		} else { // first time this is being run for this caller.
			results = new Vector();
			retry_scrapers = new Vector();
			// Fixed defaults are not compiled.
			if(name != null && value != null) {
				results.addElement(new Result.Success(caller, this, name, value)); 
			} else {
				try {
					AbstractResource[] scrapers = relationship(SUBSTITUTED_SCRAPERS);
					for(int i = 0 ; i < scrapers.length ; i ++) {
						Scraper scraper = (Scraper) scrapers[i];
						try {
							results.addElement(successForScraper(caller, scraper));
						} catch (MissingVariable e) {
							results.addElement(new Result.Premature(caller, this, e));
							retry_scrapers.addElement(scraper);
						}
					}
				} catch(ResourceNotFoundException e) {
					throw new FatalExecutionException(e);
				} catch(TemplateException e) {
					throw new FatalExecutionException(e);
				}
			}
			retry_scrapers_for_caller.put(caller, retry_scrapers);
			results_hash.put(caller, results);
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
	}
	
	public boolean isVariable() {
		return true;
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
	public static Default[] fromFormParams(String params_string, String encoding) {
		String[] params = Utils.split(params_string, "&");
		Default[] defaults = new Default[params.length];
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				String name = URLDecoder.decode(name_value[0], encoding);
				String value = URLDecoder.decode(name_value[1], encoding);
				defaults[i] = new Default(name, value);
			}
			return defaults;
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Encoding " + encoding + " not supported: " + e.getMessage());
		}
	}
	
	private Result.Success successForScraper(AbstractResult caller, Scraper scraper)
				throws FatalExecutionException, MissingVariable, TemplateException {
		return new Result.Success(caller, scraper, scraper.getName(), Mustache.compile(attribute_get(VALUE), caller.variables()));
		
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
