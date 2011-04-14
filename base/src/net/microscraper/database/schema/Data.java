package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.ResultSet;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Data extends AbstractResource {
	public Result[] execute(Result calling_result)
			throws TemplateException, MissingVariable, ResourceNotFoundException {
		AbstractResource[] defaults = relationship(DEFAULTS);
		AbstractResource[] scrapers = relationship(SCRAPERS);
		
		/*
		int prev_size = 0;
		while(root_result.size() != prev_size) {
			prev_size = root_result.size();
			for(int i = 0 ; i < defaults.length ; i ++) {
				defaults[i].execute(root_result);
			}
			for(int i = 0; i < scrapers.length ; i ++) {
				scrapers[i].execute(root_result);
			}
			Client.context().log.i(Integer.toString(root_result.size()));
		}
		*/
	}
	
	private static final RelationshipDefinition DEFAULTS =
		new RelationshipDefinition( "defaults", Default.class);
	private static final RelationshipDefinition SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() {
				return new String[] { };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { DEFAULTS, SCRAPERS };
			}
		};
	}
}
