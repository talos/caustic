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
	
	protected ResourceExecution getExecution(Execution caller) throws ResourceNotFoundException {
		Resource[] webPageSources = getRelatedResources(WEB_PAGES);
		Resource[] scraperSources = getRelatedResources(SOURCE_SCRAPERS);
		Resource[] regexpResources = getRelatedResources(REGEXPS);
		return new ScraperExecution(caller, webPageSources, scraperSources, regexpResources);
	}
	
	private boolean isScraperOneToMany() {
		if((getNumberOfRelatedResources(WEB_PAGES) + getNumberOfRelatedResources(REGEXPS)) > 1 ||
				getNumberOfRelatedResources(SOURCE_SCRAPERS) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	private final class ScraperExecution extends ResourceExecution {
		protected ScraperExecution(Execution caller, Resource[] webPageSources, Resource[] scraperResources, Resource[] regexpResources)
					throws ResourceNotFoundException {
			super(caller);
		}
		protected boolean isOneToMany() {
			return isScraperOneToMany();
		}
		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			
		}
	}
}