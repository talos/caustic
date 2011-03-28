package net.microscraper.database.schema;

import net.microscraper.client.AbstractResult;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Default {
	private final String value;
	private final Resource[] substituted_scrapers;
	public Default(Resource resource, Variables variables) throws TemplateException, MissingVariable, PrematureRevivalException {
		value = Mustache.compile(resource.attribute_get(Model.VALUE), variables);
		substituted_scrapers = resource.relationship(Model.SUBSTITUTES_FOR);
	}
	
	public void simulate(AbstractResult source) throws PrematureRevivalException {
		for(int i = 0; i < substituted_scrapers.length; i ++) {
			new Scraper(substituted_scrapers[i]).createResult(source, value);
		}
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "default";
	
		public static final String VALUE = "value";
		public static final String[] ATTRIBUTES = { VALUE };
		
		public static final String SCRAPERS = "scrapers";
		public final Relationship scrapers = new Relationship( SCRAPERS, new Scraper.Model());
		public final Relationship[] relationships = { scrapers };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}
