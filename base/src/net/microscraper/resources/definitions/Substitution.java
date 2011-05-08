package net.microscraper.resources.definitions;

import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.Execution;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Execution.ExecutionDelay;
import net.microscraper.resources.Execution.ExecutionFatality;
import net.microscraper.resources.Resource.OneToOneResource;

public class Substitution extends OneToOneResource {		
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
		
	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { VALUE }; }
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
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
