package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Default extends Resource {	
	private String name;
	private String value;
	public Default() {};
	public Default(String name, String value) {
		this.name = name;
		this.value = value;
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
	
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { VALUE }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
			}
		};
	}
	protected ResourceExecution getExecution(Execution caller)
			throws ResourceNotFoundException {
		return new DefaultExecution(caller);
	}
	
	public class DefaultExecution extends ResourceExecution {
		private Reference[] substitutedReferences;
		private String value;
		protected DefaultExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
			Resource[] substitutedScrapers = getRelatedResources(SUBSTITUTED_SCRAPERS);
			substitutedReferences = new Reference[substitutedScrapers.length];
			for(int i = 0 ; i < substitutedScrapers.length ; i ++ ) {
				substitutedReferences[i] = substitutedScrapers[i].ref();
			}
		}

		protected boolean isOneToMany() {
			return false;
		}

		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			try {
				value = getAttributeValue(VALUE);
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
		
		
	}
}
