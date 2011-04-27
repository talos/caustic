package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.Regexp.RegexpExecution;
import net.microscraper.database.schema.WebPage.WebPageExecution;

public class Scraper extends Resource {
	private final Hashtable executions = new Hashtable();
	
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
	
	public void substitute(String value) {
		substituteValue = value;
	}
	
	private boolean isScraperOneToMany() {
		if((getNumberOfRelatedResources(WEB_PAGES) + getNumberOfRelatedResources(REGEXPS)) > 1 ||
				getNumberOfRelatedResources(SOURCE_SCRAPERS) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	public ScraperExecution[] getExecutions(Execution caller) throws ResourceNotFoundException {
		if(substituteValue != null) {
			return new ScraperExecution[] {new ScraperExecution(caller, substituteValue) };
		}
		
		if(!executions.containsKey(caller)) {
			Resource[] regexps =  getRelatedResources(REGEXPS);
			Resource[] scrapers = getRelatedResources(SOURCE_SCRAPERS); 
			Resource[] webPages = getRelatedResources(WEB_PAGES);
			for(int i = 0 ; i < regexps.length ; i ++ ) {
				for(int j = 0 ; j < scrapers.length ; j ++) {
					new ScraperExecutionFromScraper(caller, (Regexp) regexps[i], (Scraper) scrapers[j]);
				}
				for(int j = 0 ; j < webPages.length ; j ++) {
					new ScraperExecutionFromWebPage(caller, (Regexp) regexps[i], (WebPage) webPages[j]);
				}
			}
		}
		Vector executionsForCaller = (Vector) executions.get(caller);
		ScraperExecution[] executionsAry = new ScraperExecution[executionsForCaller.size()];
		executionsForCaller.copyInto(executionsAry);
		return executionsAry;
	}
	
	public Status execute(Execution caller) throws ResourceNotFoundException {
		ScraperExecution[] scrapers = getExecutions(caller);
		Status status = Status.SUCCESSFUL;
		for(int i = 0 ; i < scrapers.length ; i++) {
			try {
				scrapers[i].execute();
			} catch(MissingVariable e) {
				status = Status.IN_PROGRESS;
			} catch(BrowserException e) {
				return Status.FAILURE;
			} catch(FatalExecutionException e) {
				return Status.FAILURE;
			} catch(NoMatches e) {
				return Status.FAILURE;
			}
		}
		return status;
	}
	
	public class ScraperExecution extends ResourceExecution {
		private RegexpExecution regexpExecution;
		//private final Hashtable matches = new Hashtable();
		private String match;
		private ScraperExecution(Execution caller, Regexp regexp) throws ResourceNotFoundException {
			super(caller);
			regexpExecution = regexp.getExecution(getSourceExecution());
			if(!executions.containsKey(caller)) {
				executions.put(caller, new Vector());
			}
			Vector executionsForCaller = (Vector) executions.get(caller);
			executionsForCaller.addElement(this);
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
		protected void execute() throws NoMatches, BrowserException, FatalExecutionException, MissingVariable {
		}
	}
	
	private class ScraperExecutionFromWebPage extends ScraperExecution {
		private final WebPageExecution sourceWebPageExecution;
		private ScraperExecutionFromWebPage(Execution caller, Regexp regexp, WebPage webPage)
				throws ResourceNotFoundException {
			super(caller, regexp);
			sourceWebPageExecution = webPage.getExecution(getSourceExecution());
		}
		protected void execute() throws NoMatches, BrowserException, FatalExecutionException, MissingVariable {
			sourceWebPageExecution.execute();
			execute(sourceWebPageExecution.webPageString);
		}
	}
	
	private class ScraperExecutionFromScraper extends ScraperExecution {
		//private final ScraperExecution[] sourceScraperExecutions;
		private final Scraper sourceScraper;
		private ScraperExecutionFromScraper(Execution caller, Regexp regexp, Scraper scraper)
				throws ResourceNotFoundException {
			super(caller, regexp);
			sourceScraper = scraper;
		}
		protected void execute() throws FatalExecutionException, NoMatches {
			try {
				if(sourceScraper.execute(getSourceExecution()) == Status.SUCCESSFUL) {
					ScraperExecution[] sourceScraperExecutions = sourceScraper.getExecutions(getSourceExecution());
					for(int i = 0 ; i < sourceScraperExecutions.length ; i ++) {
						execute(sourceScraperExecutions[i].match());
					}
				}
			} catch(ResourceNotFoundException e) {
				throw new FatalExecutionException(e); 
			}
		}
	}
}