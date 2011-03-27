package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Mustache;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Default {
	private final String raw_value;
	public Default(Resource resource) {
		raw_value = resource.attribute_get(Model.VALUE);
	}
	
	public int simulate(Result source, Variables variables) throws PrematureRevivalException {
		String compiled_value = Mustache.compile(raw_value, variables);
		Resource[] substituted_scrapers = resource.relationship(Model.SUBSTITUTES_FOR);
		for(int i = 0; i < substituted_scrapers.length; i ++) {
			//put(substituted_scrapers[i].ref, resource.attribute_get(Model.VALUE));
			new Scraper(substituted_scrapers[i]).createResult(source, value);
		}
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "default";
	
		public static final String VALUE = "value";
		public static final String[] ATTRIBUTES = { VALUE };
		
		public static final String SUBSTITUTES_FOR = "substitutes_for";
		public final Relationship substitutes_for = new Relationship( SUBSTITUTES_FOR, new Scraper.Model());
		public final Relationship[] relationships = { substitutes_for };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
	}
}
