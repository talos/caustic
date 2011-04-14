package net.microscraper.database.schema;

import java.util.Enumeration;
import java.util.Hashtable;
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
		
		Hashtable results_hsh = new Hashtable();
		int lastSize;
		do {
			lastSize = results_hsh.size();
			for(int i = 0 ; i < defaults.length ; i ++) {
				try {
					results_hsh.put(defaults[i].ref(), defaults[i].getValue(caller));
				} catch(MissingVariable e) {
					Client.context().log.w(e);
				}
			}
			for(int i = 0 ; i < scrapers.length ; i ++) {
				try {
					results_hsh.put(scrapers[i].ref(), scrapers[i].getValue(caller));
				} catch(MissingVariable e) {
					Client.context().log.w(e);
				}
			}
			//Client.context().log.i(Integer.toString(lastSize));
		} while(lastSize != results_hsh.size());
		//Result[] results_ary = new Result[results.size()];
		//results.copyInto(results_ary);
		//int i = 0;
		// Flatten multidimensional results hash into vector of results.
		Vector results_vec = new Vector();
		Enumeration elements = results_hsh.elements();
		while(elements.hasMoreElements()) {
			Result[] r = (Result[]) elements.nextElement();
			for(int i = 0 ; i < r.length ; i ++) {
				results_vec.addElement(r[i]);
			}
		}
		Result[] results_ary = new Result[results_vec.size()];
		results_vec.copyInto(results_ary);
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
