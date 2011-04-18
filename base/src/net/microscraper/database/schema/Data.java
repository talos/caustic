package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Data extends AbstractResource.Simple {
	protected String getName(AbstractResult caller) throws TemplateException,
			ResourceNotFoundException, InterruptedException, MissingVariable,
			NoMatches, FatalExecutionException {
		return ref().title;
	}
	
	protected String getValue(AbstractResult caller) throws TemplateException,
			ResourceNotFoundException, InterruptedException, MissingVariable,
			NoMatches, FatalExecutionException {
		AbstractResource[] defaults = relationship(DEFAULTS);
		AbstractResource[] scrapers = relationship(SCRAPERS);
		
		Vector results = new Vector();
		for(int i = 0 ; i < defaults.length ; i ++) {
			Default _default = (Default) defaults[i];
			Utils.arrayIntoVector(_default.getResults(caller), results);
		}
		for(int i = 0 ; i < scrapers.length ; i ++) {
			Scraper scraper = (Scraper) scrapers[i];
			Utils.arrayIntoVector(scraper.getResults(caller), results);
		}
		
		// Count success/premature/failure.
		int success = 0;
		int premature = 0;
		int failure = 0;
		for(int i = 0 ; i < results.size() ; i ++) {
			if(((Result) results.elementAt(i)).successful) {
				success++;
			} else if(((Result) results.elementAt(i)).premature) {
				premature++;
			} else if(((Result) results.elementAt(i)).failure) {
				failure++;
			}
		}
		return Integer.toString(success) + " successes, " + Integer.toString(premature) + " not yet ready, " + Integer.toString(failure);
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
