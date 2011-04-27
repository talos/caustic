package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Default extends Resource {		
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
	
	private final Hashtable executions = new Hashtable();
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { VALUE }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
			}
		};
	}
	
	protected DefaultExecution getExecution(Execution caller)
			throws ResourceNotFoundException {
		if(!executions.containsKey(caller)) {
			executions.put(caller, new DefaultExecution(this, caller));
		}
		return (DefaultExecution) executions.get(caller);
	}

	public Status execute(Execution caller) throws ResourceNotFoundException {
		try {
			getExecution(caller).execute();
			return Status.SUCCESSFUL;
		} catch(MissingVariable e) {
			return Status.IN_PROGRESS;
		} catch(FatalExecutionException e) {
			return Status.FAILURE;
		}
	}
	
	public class DefaultExecution extends ResourceExecution {
		private Resource[] substitutedScrapers;
		private String value;
		protected DefaultExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
			substitutedScrapers = getRelatedResources(SUBSTITUTED_SCRAPERS);
		}
		
		protected boolean isOneToMany() {
			return false;
		}

		protected Variables getLocalVariables() {
			return null;
		}
		
		protected void execute() throws MissingVariable, FatalExecutionException {
			try {
				value = getAttributeValue(VALUE);
				for(int i = 0 ; i < substitutedScrapers.length ; i++) {
					((Scraper) substitutedScrapers[i]).substitute(value);
				}
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
	}
}
