package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
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

public class Scraper extends Resource {
	protected final HashtableWithNulls executions = new HashtableWithNulls();
	
	private static final RelationshipDefinition WEB_PAGES =
		new RelationshipDefinition( "web_pages", WebPage.class );
	private static final RelationshipDefinition SOURCE_SCRAPERS =
		new RelationshipDefinition( "source_scrapers", Scraper.class);
	private static final RelationshipDefinition SEARCHES_WITH =
		new RelationshipDefinition( "searches_with", Regexp.class);
	private static final RelationshipDefinition TESTED_BY =
		new RelationshipDefinition( "tested_by", Regexp.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() {
				return new AttributeDefinition[] { };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					WEB_PAGES, SOURCE_SCRAPERS, SEARCHES_WITH, TESTED_BY
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
				getNumberOfRelatedResources(SEARCHES_WITH) > 1) // one-to-many if pulling from multiple sources, or multiple regexps.
			return true;
		return false;
	}
	
	public boolean isPublishedToVariables() {
		return true;
	}
	
	private Status execute(Execution caller, Variables extraVariables) throws ExecutionFatality {
		Execution[] executions = executionsFromExecution(null);
		Status status = new Status();
		for(int i = 0 ; i < executions.length ; i ++) {
			if(extraVariables != null) {
				executions[i].addVariables(extraVariables);
			}
			status.merge(executions[i].safeExecute());
		}
		return status;
	}
	
	public Status execute(Execution caller) throws ExecutionFatality {
		return execute(caller, null);
	}
	
	public Status execute(Variables extraVariables) throws ExecutionFatality {
		return execute(null, extraVariables);
	}
	
	private ScraperExecution[] executionsFromExecution(Execution caller) throws ExecutionFatality {
		if(substituteValue != null) {
			return new ScraperExecution[] { new SolvedScraperExecution(this, caller, substituteValue) };
		}
		
		if(!executions.containsKey(caller)) {
			try {
				Resource[] searchesWith =  getRelatedResources(SEARCHES_WITH);
				Resource[] testedBy =  getRelatedResources(TESTED_BY);
				Resource[] scrapers = getRelatedResources(SOURCE_SCRAPERS); 
				Resource[] webPages = getRelatedResources(WEB_PAGES);

				if(searchesWith.length < 1) {
					throw new ExecutionFatality(caller, new RuntimeException("Scraper needs at least one regexp."));
				}
				if(scrapers.length + webPages.length < 1)
					throw new ExecutionFatality(caller, new RuntimeException("Scraper needs at least one web page or source scraper."));
				
				// creating new scraperExecutions adds them to the executions vector.
				for(int i = 0 ; i < searchesWith.length ; i ++ ) {
					for(int j = 0 ; j < scrapers.length ; j ++) {
						new ScraperExecutionFromScraper(this, caller, (Regexp) searchesWith[i], (Scraper) scrapers[j]);
					}
					for(int j = 0 ; j < webPages.length ; j ++) {
						new ScraperExecutionFromWebPage(this, caller, (Regexp) searchesWith[i], (WebPage) webPages[j]);
					}
				}
			} catch(ResourceNotFoundException e) {
				throw new ExecutionFatality(caller, e);
			}
		}
		
		Vector executionsForCaller = (Vector) executions.get(caller);
		ScraperExecution[] executionsAry = new ScraperExecution[executionsForCaller.size()];
		executionsForCaller.copyInto(executionsAry);
		return executionsAry;
	}
	
	public static abstract class ScraperExecution extends Execution {
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
	}

	private static class SolvedScraperExecution extends ScraperExecution {
		private final String match;
		public SolvedScraperExecution(ScraperExecution executionToDuplicate, String match) throws ExecutionFatality {
			super(executionToDuplicate.scraper, executionToDuplicate.getSourceExecution());
			this.match = match;
			this.safeExecute();
		}
		public SolvedScraperExecution(Scraper scraper, Execution caller, String match) throws ExecutionFatality {
			super(scraper, caller);
			this.match = match;
			this.safeExecute();
		}
		protected String privateExecute() {
			return match;
		}
	}

	private static abstract class UnsolvedScraperExecution extends ScraperExecution {
		private final RegexpExecution regexpExecution;
		protected UnsolvedScraperExecution(Scraper scraper, Execution caller, Regexp regexp) throws ExecutionFatality {
			super(scraper, caller);
			this.regexpExecution = (RegexpExecution) callResource(regexp);
		}
		protected String matchAgainst(Execution execution) throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
			regexpExecution.unsafeExecute();
			String[] matches;
			try {
				matches = regexpExecution.allMatches(execution.unsafeExecute());
			} catch (NoMatches e) {
				throw new ExecutionFailure(this, e);
			} catch (MissingGroup e) {
				throw new ExecutionFatality(this, e);
			}
			// create fakes from extra matches.
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
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
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
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
			ScraperExecution[] scraperExecutions;
			scraperExecutions = sourceScraper.executionsFromExecution(getSourceExecution());
			// Run scraper executions beforehand, as they may multiply themselves.
			for(int i = 0 ; i < scraperExecutions.length ; i ++ ) {
				scraperExecutions[i].unsafeExecute();
			}
			scraperExecutions = sourceScraper.executionsFromExecution(getSourceExecution());
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