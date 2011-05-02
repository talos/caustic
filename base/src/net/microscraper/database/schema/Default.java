package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;

public class Default extends OneToOneResource {		
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
	
	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new DefaultExecution(this, caller);
	}
	
	public class DefaultExecution extends Execution {
		private Resource[] substitutedScrapers;
		private String value;
		protected DefaultExecution(Resource resource, Execution caller) throws ExecutionFatality {
			super(resource, caller);
			substitutedScrapers = getRelatedResources(SUBSTITUTED_SCRAPERS);
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFatality {
			value = getAttributeValue(VALUE);
			for(int i = 0 ; i < substitutedScrapers.length ; i++) {
				((Scraper) substitutedScrapers[i]).substitute(value);
			}
			return value;
		}
	}
}
