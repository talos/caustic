package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.Regexp.RegexpExecution;
import net.microscraper.database.schema.WebPage.WebPageExecution;

public class Scraper extends Resource {
	private static final RelationshipDefinition WEB_PAGES =
		new RelationshipDefinition( "web_pages", WebPage.class );
	private static final RelationshipDefinition SOURCE_SCRAPERS =
		new RelationshipDefinition( "source_scrapers", Scraper.class);
	private static final RelationshipDefinition REGEXPS =
		new RelationshipDefinition( "regexps", Regexp.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() {
				return new AttributeDefinition[] { };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					WEB_PAGES, SOURCE_SCRAPERS, REGEXPS
				};
			}
		};
	}
	
	private String substituteValue = null;
	
	private boolean isScraperOneToMany() {
		if((getNumberOfRelatedResources(WEB_PAGES) + getNumberOfRelatedResources(REGEXPS)) > 1 ||
				getNumberOfRelatedResources(SOURCE_SCRAPERS) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	public ScraperExecution[] getExecutions(Execution caller) throws ResourceNotFoundException {
		Resource[] regexps = getRelatedResources(REGEXPS);
		Resource[] scrapers = getRelatedResources(SOURCE_SCRAPERS);
		Resource[] webPages = getRelatedResources(WEB_PAGES);
		
		Vector executions = new Vector();
		for(int i = 0 ; i < regexps.length ; i ++ ) {
			Vector sourceStrings = new Vector();
			RegexpExecution regexp = ((Regexp) regexps[i]).getExecution(caller);
			for(int j = 0 ; j < scrapers.length ; j ++) {
				ScraperExecution[] scraperExecutions = ((Scraper) scrapers[j]).getExecutions(caller);
				for(int k = 0 ; k < scraperExecutions.length ; k ++ ) {
					ScraperExecution scraperExecution = scraperExecutions[k];
					scraperExecution.execute();
					Utils.arrayIntoVector(scraperExecution.matches(), sourceStrings);
				}
			}
			for(int j = 0 ; j < webPages.length ; j ++) {
				WebPageExecution webPageExecution = ((WebPage) webPages[j]).getExecution(caller);
				webPageExecution.execute();
				sourceStrings.addElement(webPageExecution.load());
			}
			for(int j = 0 ; j < sourceStrings.size() ; j ++) {
				executions.addElement(new ScraperExecution(caller, regexp, (String) sourceStrings.elementAt(j)));
			}
		}
		ScraperExecution[] executionsAry = new ScraperExecution[executions.size()];
		executions.copyInto(executionsAry);
		return executionsAry;
	}
	
	public Status execute(Execution caller) throws ResourceNotFoundException {
		ScraperExecution[] excs = getExecutions(caller);
		for(int i = 0 ; i < excs.length ; i ++) {
			excs[i].execute();
		}
	}
	
	public void substitute(String value) {
		substituteValue = value;
	}
	
	public final class ScraperExecution extends ResourceExecution {
		private final String sourceString;
		private final RegexpExecution regexpExecution;
		//private final Hashtable matches = new Hashtable();
		private String[] matches;
		private ScraperExecution(Execution caller, RegexpExecution regexpExecution,
				String sourceString) {
			super(caller);
			this.sourceString = sourceString;
			this.regexpExecution = regexpExecution;
		}
		protected boolean isOneToMany() {
			return isScraperOneToMany();
		}
		protected Variables getLocalVariables() {
			return null;
		}
		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			matches = regexpExecution.allMatches(sourceString);
		}
		public String[] matches() {
			return matches;
		}
	}
}