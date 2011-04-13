package net.microscraper.database.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.microscraper.client.Client;
import net.microscraper.client.Mustache;
import net.microscraper.client.Utils;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Reference;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Default extends AbstractResource {
	/*
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
	*/
	/**
	 * Simulate the Default for the specified source.
	 * @param source
	 * @return True if any action has been taken, false otherwise.
	 * @throws PrematureRevivalException
	 * @throws TemplateException
	 * @throws MissingVariable 
	 */
	public String[] execute(ResultSet source) throws TemplateException, MissingVariable {
		String raw_value = attribute_get(VALUE);
		return new String[] {
			Mustache.compile(raw_value, source.variables())
		};
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
