package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.ResultSet;
import net.microscraper.client.ResultSet.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class Default {
	private final Resource resource;
	public Default(Resource _resource) {
		resource = _resource;
	}
	
	public Result[] simulate(Variables variables) throws PrematureRevivalException {
		Resource[] substituted_scrapers = resource.relationship(Model.SUBSTITUTES_FOR);
		for(int i = 0; i < substituted_scrapers.length; i ++) {
			results.put(substituted_scrapers[i].ref, resource.attribute_get(Model.VALUE));
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
