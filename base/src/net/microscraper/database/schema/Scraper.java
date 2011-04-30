package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Status;
import net.microscraper.database.schema.Regexp.RegexpExecution;
import net.microscraper.database.schema.WebPage.WebPageExecution;

public class Scraper extends Resource {
	protected final HashtableWithNulls executions = new HashtableWithNulls();
	
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
	
	public boolean isOneToMany() {		
		if((getNumberOfRelatedResources(WEB_PAGES) + getNumberOfRelatedResources(SOURCE_SCRAPERS)) > 1 ||
				getNumberOfRelatedResources(REGEXPS) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	private ScraperExecution[] executionsFromExecution(Execution caller) throws ExecutionFatality {
		if(substituteValue != null) {
			return new ScraperExecution[] { new SolvedScraperExecution(this, caller, substituteValue) };
		}
		
		if(!executions.containsKey(caller)) {
			Resource[] regexps =  getRelatedResources(REGEXPS);
			Resource[] scrapers = getRelatedResources(SOURCE_SCRAPERS); 
			Resource[] webPages = getRelatedResources(WEB_PAGES);
			for(int i = 0 ; i < regexps.length ; i ++ ) {
				for(int j = 0 ; j < scrapers.length ; j ++) {
					new ScraperExecutionFromScraper(this, caller, (Regexp) regexps[i], (Scraper) scrapers[j]);
				}
				for(int j = 0 ; j < webPages.length ; j ++) {
					new ScraperExecutionFromWebPage(this, caller, (Regexp) regexps[i], (WebPage) webPages[j]);
				}
			}
		}
		
		Vector executionsForCaller = (Vector) executions.get(caller);
		ScraperExecution[] executionsAry = new ScraperExecution[executionsForCaller.size()];
		executionsForCaller.copyInto(executionsAry);
		return executionsAry;
	}
	
	private ScraperExecution getExecution(Execution caller, Variables extraVariables) throws ExecutionFatality {
		ScraperExecution[] scrapers = getExecutions(caller);
		//Status compoundStatus = new Status.InProgress();
		for(int i = 0 ; i < scrapers.length ; i++) {
			if(extraVariables != null) {
				scrapers[i].addVariables(extraVariables);
			}
			//compoundStatus.merge(scrapers[i].execute());
		}
		//return compoundStatus;
	}
	
	public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
		return getExecution(null, extraVariables);
	}
	
	/*
	public Execution executionFromExecution(Execution caller) throws ExecutionFatality {
		return getExecution(caller, null);
	}
	*/
	public static abstract class ScraperExecution extends Execution {
		//private final Hashtable matches = new Hashtable();
		//private String match;
		private final Scraper scraper;
		private ScraperExecution(Scraper scraper, Execution caller) {
			super(scraper, caller);
			this.scraper = scraper;
			if(!scraper.executions.containsKey(caller)) {
				scraper.executions.put(caller, new Vector());
			}
			Vector executionsForCaller = (Vector) scraper.executions.get(caller);
			executionsForCaller.addElement(this);
		}
		protected boolean isOneToMany() {
			return scraper.isOneToMany();
		}
	}

	private static class SolvedScraperExecution extends ScraperExecution {
		private final String match;
		public SolvedScraperExecution(ScraperExecution executionToDuplicate, String match) {
			super(executionToDuplicate.scraper, executionToDuplicate.getSourceExecution());
			this.match = match;
		}
		public SolvedScraperExecution(Scraper scraper, Execution caller, String match) {
			super(scraper, caller);
			this.match = match;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			return match;
		}
	}

	private static abstract class UnsolvedScraperExecution extends ScraperExecution {
		private final RegexpExecution regexpExecution;
		protected UnsolvedScraperExecution(Scraper scraper, Execution caller, Regexp regexp) throws ExecutionFatality {
			super(scraper, caller);
			this.regexpExecution = (RegexpExecution) callResource(regexp);
		}
		protected String matchAgainst(Execution execution) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			regexpExecution.unsafeExecute();
			String[] matches;
			try {
				matches = regexpExecution.allMatches(execution.unsafeExecute());
			} catch (NoMatches e) {
				throw new ExecutionFailure(getSourceExecution(), e);
			}
			for(int i = 1 ; i < matches.length ; i ++) {
				new SolvedScraperExecution(this, matches[i]);
			}
			return matches[0];
		}
	}
	
	private static class ScraperExecutionFromWebPage extends UnsolvedScraperExecution {
		private final WebPage webPage;
		private ScraperExecutionFromWebPage(Scraper scraper, Execution caller, Regexp regexp, WebPage webPage) throws ExecutionFatality {
			super(scraper, caller, regexp);
			this.webPage = webPage;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			return matchAgainst(callResource(webPage));
		}
	}
	
	private static class ScraperExecutionFromScraper extends UnsolvedScraperExecution {
		private final Scraper sourceScraper;
		private ScraperExecutionFromScraper(Scraper scraper, Execution caller, Regexp regexp, Scraper sourceScraper)
				throws ExecutionFatality {
			super(scraper, caller, regexp);
			this.sourceScraper = sourceScraper;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			ScraperExecution[] scraperExecutions = sourceScraper.executionsFromExecution(getSourceExecution());
			String[] matches = new String[scraperExecutions.length];
			for(int i = 0 ; i < scraperExecutions.length ; i ++ ) {
				matches[i] = matchAgainst(scraperExecutions[i]);
			}
			// create fakes for the extra matches
			for(int i = 1 ; i < matches.length ; i++) {
				new SolvedScraperExecution(this, matches[i]);
			}
			return matches[0];
		}
	}
	
}