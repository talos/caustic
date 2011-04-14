package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Data extends AbstractResource {
	public Result[] execute(AbstractResult caller)
			throws TemplateException, MissingVariable, ResourceNotFoundException, InterruptedException, BrowserException {
		AbstractResource[] defaults = relationship(DEFAULTS);
		AbstractResource[] scrapers = relationship(SCRAPERS);
		
		Vector results = new Vector();
		int lastSize;
		do {
			lastSize = results.size();
			for(int i = 0 ; i < defaults.length ; i ++) {
				try {
					results.addElement(defaults[i].getValue(caller));
				} catch(MissingVariable e) {
					Client.context().log.w(e);
				}
			}
			for(int i = 0 ; i < scrapers.length ; i ++) {
				try {
					results.addElement(scrapers[i].getValue(caller));
				} catch(MissingVariable e) {
					Client.context().log.w(e);
				}
			}
		} while(lastSize != results.size());
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
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
