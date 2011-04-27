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

	public Status execute(Variables extraVariables) throws ResourceNotFoundException, FatalExecutionException {
		DefaultExecution exc = getExecution(null);
		exc.addVariables(extraVariables);
		return exc.execute();
	}
	
	public class DefaultExecution extends ResourceExecution {
		private Resource[] substitutedScrapers;
		private String value;
		private Status status = Status.IN_PROGRESS;
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
		
		protected Status execute() throws FatalExecutionException {
			status = Status.SUCCESSFUL;
			try {
				try {
					value = getAttributeValue(VALUE);
				} catch(MissingVariable e) {
					status = Status.IN_PROGRESS;
				}
				for(int i = 0 ; i < substitutedScrapers.length ; i++) {
					((Scraper) substitutedScrapers[i]).substitute(value);
				}
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
			return status;
		}
		public Status getStatus() {
			return status;
		}
	}
}
