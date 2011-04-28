package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Utils.HashtableWithNulls;
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
	
	public ScraperExecution[] getExecutions(Execution caller) throws ResourceNotFoundException {
		if(substituteValue != null) {
			return new ScraperExecution[] {new ScraperExecution(this, caller, substituteValue) };
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
	
	private Status execute(Execution caller, Variables extraVariables) throws ResourceNotFoundException, InterruptedException {
		ScraperExecution[] scrapers = getExecutions(caller);
		Status compoundStatus = Status.IN_PROGRESS;
		for(int i = 0 ; i < scrapers.length ; i++) {
			if(extraVariables != null) {
				scrapers[i].addVariables(extraVariables);
			}
			compoundStatus.join(scrapers[i].execute());
		}
		return compoundStatus;
	}
	
	public Status execute(Variables extraVariables) throws ResourceNotFoundException, InterruptedException {
		return execute(null, extraVariables);
	}
	
	public Status execute(Execution caller) throws ResourceNotFoundException, InterruptedException {
		return execute(caller, null);
	}
	
	public static class ScraperExecution extends ResourceExecution {
		private RegexpExecution regexpExecution;
		//private final Hashtable matches = new Hashtable();
		private String match;
		private final Scraper scraper;
		private ScraperExecution(Scraper scraper, Execution caller, Regexp regexp) throws ResourceNotFoundException {
			super(scraper, caller);
			this.scraper = scraper;
			regexpExecution = regexp.getExecution(getSourceExecution());
			if(!scraper.executions.containsKey(caller)) {
				scraper.executions.put(caller, new Vector());
			}
			Vector executionsForCaller = (Vector) scraper.executions.get(caller);
			executionsForCaller.addElement(this);
		}
		private ScraperExecution(Scraper scraper, Execution caller, String match) {
			super(scraper, caller);
			this.match = match;
			this.scraper = scraper;
		}
		protected boolean isOneToMany() {
			return scraper.isOneToMany();
		}
		protected Variables getLocalVariables() {
			if(match != null) {
				Variables variables = new Variables();
				variables.put(scraper.ref().title, match);
				return variables;
			} else {
				return null;
			}
		}
		// Replicate once we have a source.
		protected void execute(String source) throws NoMatches, MissingVariable {
			String[] matches = regexpExecution.allMatches(source);
			match = matches[0];
			for(int i = 1 ; i < matches.length ; i ++) {
				new ScraperExecution(scraper, getSourceExecution(), matches[i]);
			}
		}
		public String match() {
			return match;
		}
		protected Status privateExecute() throws ResourceNotFoundException, InterruptedException {
			return Status.SUCCESSFUL;
		}
		public String getPublishValue() {
			return match;
		}
	}
	
	private static class ScraperExecutionFromWebPage extends ScraperExecution {
		private final WebPageExecution sourceWebPageExecution;
		private ScraperExecutionFromWebPage(Scraper scraper, Execution caller, Regexp regexp, WebPage webPage)
				throws ResourceNotFoundException {
			super(scraper, caller, regexp);

			sourceWebPageExecution = webPage.getExecution(getSourceExecution());
		}
		protected Status privateExecute() throws ResourceNotFoundException, InterruptedException {
			Status status = Status.SUCCESSFUL;
			try {
				if(sourceWebPageExecution.execute() == Status.SUCCESSFUL) {
					try {
						this.execute(sourceWebPageExecution.load());
					} catch(NoMatches e) {
						status.join(Status.FAILURE);
					}
				} else {
					status.join(Status.IN_PROGRESS);
				}
			} catch (MissingVariable e) {
				status.join(Status.IN_PROGRESS);
			}
			return status;
		}
	}
	
	private static class ScraperExecutionFromScraper extends ScraperExecution {
		private final Scraper sourceScraper;
		private ScraperExecutionFromScraper(Scraper scraper, Execution caller, Regexp regexp, Scraper sourceScraper)
				throws ResourceNotFoundException {
			super(scraper, caller, regexp);
			this.sourceScraper = sourceScraper;
		}
		protected Status privateExecute() throws ResourceNotFoundException, InterruptedException {
			Status status = Status.SUCCESSFUL;
			if(sourceScraper.execute(getSourceExecution()) == Status.SUCCESSFUL) {
				ScraperExecution[] sourceScraperExecutions = sourceScraper.getExecutions(getSourceExecution());
				for(int i = 0 ; i < sourceScraperExecutions.length ; i ++) {
					try {
						this.execute(sourceScraperExecutions[i].match());
					} catch(MissingVariable e) {
						status.join(Status.IN_PROGRESS);
					} catch(NoMatches e) {
						status.join(Status.FAILURE);
					}
				}
			}

			return status;
		}
	}
}