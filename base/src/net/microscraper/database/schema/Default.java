package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Default extends Resource {		
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
	
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
		return new DefaultExecution(caller);
	}

	public void execute(Execution caller) throws ResourceNotFoundException {
		getExecution(caller);
	}
	
	public class DefaultExecution extends ResourceExecution {
		private Resource[] substitutedScrapers;
		private String value;
		protected DefaultExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
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
