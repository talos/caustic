package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Result;

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
	
	protected ResourceExecution[] generateExecutions(Execution caller) throws ResourceNotFoundException {
		
		Resource[] webPageSources = getRelatedResources(WEB_PAGES);
		Resource[] scraperSources = getRelatedResources(SOURCE_SCRAPERS);
		Resource[] sources = new Resource[webPageSources.length + scraperSources.length];
		for(int i = 0 ; i < webPageSources.length ; i ++) {
			sources[i] = webPageSources[i];
		}
		for(int i = 0 ; i < scraperSources.length ; i ++) {
			sources[i + webPageSources.length] = scraperSources[i];
		}
		
		//Execution[] regexps = callRelatedResources(caller, REGEXPS);
		Resource[] regexps = getRelatedResources(REGEXPS);
		ScraperExecution[] executions = new ScraperExecution[regexps.length * sources.length];
		for(int i = 0 ; i < sources.length ; i ++ ) {
			for(int j = 0 ; j < regexps.length ; j++) {
				ScraperExecution exc = new ScraperExecution(caller, regexps[j]);
				executions[(i*j) + j] = exc;
				exc.call(sources[i]);
			}
		}
		return executions;
	}
	
	private boolean isScraperOneToMany() {
		if((getNumberOfRelatedResources(WEB_PAGES) + getNumberOfRelatedResources(REGEXPS)) > 1 ||
				getNumberOfRelatedResources(SOURCE_SCRAPERS) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	private final class ScraperExecution extends ResourceExecution {
		private final Resource source;
		private final Resource regexp;
		protected ScraperExecution(Execution caller, Resource source, Resource regexp) throws ResourceNotFoundException {
			super(caller);
			this.source = source;
			this.regexp = regexp;
			
		}
		protected String generateName() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}
		protected String generateValue() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}
		protected boolean isOneToMany() {
			return isScraperOneToMany();
		}
		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	/*
	private final class ScraperOneToOneExecution extends ScraperExecution {
		private ScraperOneToOneExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
		}
		
		protected String generateValue() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}

		protected boolean isOneToMany() {
			return false;
		}

		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	private final class ScraperOneToManyExecution extends ScraperExecution {
		private ScraperOneToManyExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
		}
		
		protected String generateValue() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			
		}

		protected boolean isOneToMany() {
			return true;
		}

		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
	}*/
}