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
		if(getResourcesToCall(WEB_PAGES).length + getResourcesToCall(REGEXPS).length +
				getResourcesToCall(SOURCE_SCRAPERS).length > 1) // one-to-many if pulling from multiple sources.
			return new ScraperOneToManyExecution(caller);
		return new ScraperOneToOneExecution(caller);
	}
	
	private abstract class ScraperExecution extends ResourceExecution {
		private ScraperExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
		}
		protected String generateName() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}
	}
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
			// TODO Auto-generated method stub
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
			Pattern pattern;
			try {
				pattern = Client.regexp.compile(getAttributeValue(REGEXP));
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
			Execution[] executions = getCalledExecutions();
			
		}

		protected boolean isOneToMany() {
			return true;
		}

		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}