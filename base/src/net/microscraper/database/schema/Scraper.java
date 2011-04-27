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
	
	public ScraperExecution[] getExecutions(Execution caller) {
		Resource[] regexps =  getRelatedResources(REGEXPS);
		Resource[] scrapers = getRelatedResources(SOURCE_SCRAPERS); 
		Resource[] webPages = getRelatedResources(WEB_PAGES);
		ScraperExecution[] executions = new ScraperExecution[regexps.length * (scrapers.length + webPages.length)];
		for(int i = 0 ; i < regexps.length ; i ++ ) {
			for(int j = 0 ; j < scrapers.length ; j ++) {
				executions[(i * j) + j] =
					new ScraperExecutionFromScraper(caller, (Regexp) regexps[i], (Scraper) scrapers[j]);
			}
			for(int j = 0 ; j < webPages.length ; j ++) {
				executions[(i * (j + scrapers.length) ) + j] =
					new ScraperExecutionFromWebPage(caller, (Regexp) regexps[i], (WebPage) webPages[j]);
			}
		}
		return executions;
	}
	
	public Status execute(Execution caller) throws ResourceNotFoundException {
		ScraperExecution[] scrapers = getExecutions(caller);
		for(int i = 0 ; i < scrapers.length ; i++) {
			scrapers[i].execute();
		}
	}
	
	public void substitute(String value) {
		substituteValue = value;
	}
	
	public class ScraperExecution extends ResourceExecution {
		private RegexpExecution regexpExecution;
		//private final Hashtable matches = new Hashtable();
		private String match;
		private ScraperExecution(Execution caller, Regexp regexp) throws ResourceNotFoundException {
			super(caller);
			regexpExecution = regexp.getExecution(getSourceExecution());
		}
		private ScraperExecution(Execution caller, String match) {
			super(caller);
			this.match = match;
		}
		protected boolean isOneToMany() {
			return isScraperOneToMany();
		}
		protected Variables getLocalVariables() {
			if(match != null) {
				Variables variables = new Variables();
				variables.put(ref().title, match);
				return variables;
			} else {
				return null;
			}
		}
		// Replicate once we have a source.
		protected void execute(String source) throws NoMatches {
			String[] matches = regexpExecution.allMatches(source);
			match = matches[0];
			for(int i = 1 ; i < matches.length ; i ++) {
				new ScraperExecution(getSourceExecution(), matches[i]);
			}
		}
		public String match() {
			return match;
		}
		protected void execute() {
		}
	}
	
	private class ScraperExecutionFromWebPage extends ScraperExecution {
		private final WebPageExecution sourceWebPageExecution;
		private ScraperExecutionFromWebPage(Execution caller, Regexp regexp, WebPage webPage) {
			super(caller, regexp);
			sourceWebPageExecution = webPage.getExecution(getSourceExecution());
		}
		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			sourceWebPageExecution.execute();
		}
	}
	
	private class ScraperExecutionFromScraper extends ScraperExecution {
		private final Scraper sourceScraper;
		private ScraperExecutionFromScraper(Execution caller, Regexp regexp, Scraper scraper) {
			super(caller, regexp);
		}
	}
}