package net.microscraper.database.schema;

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

public class Default extends Resource {		
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
	
	private final HashtableWithNulls executions = new HashtableWithNulls();
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { VALUE }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
			}
		};
	}
	
	public Execution executionFromExecution(Execution caller) throws ExecutionFatality {
		if(!executions.containsKey(caller)) {
			executions.put(caller, new DefaultExecution(this, caller));
		}
		return (DefaultExecution) executions.get(caller);
	}

	public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
		DefaultExecution exc = (DefaultExecution) executionFromExecution(null);
		exc.addVariables(extraVariables);
		return exc;
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
		
		protected String privateExecute() throws TemplateException, MissingVariable {
			value = getAttributeValue(VALUE);
			for(int i = 0 ; i < substitutedScrapers.length ; i++) {
				((Scraper) substitutedScrapers[i]).substitute(value);
			}
			return value;
		}
	}
}
