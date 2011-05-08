package net.microscraper.resources.definitions;

import net.microscraper.client.Client;
import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.Execution;
import net.microscraper.resources.OneToOneResourceDefinition;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Result;

public class Substitution extends OneToOneResourceDefinition {		
	private static final AttributeDefinition VALUE = new AttributeDefinition("value");
	private static final RelationshipDefinition SUBSTITUTED_SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class );
		
	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { VALUE }; }
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] { SUBSTITUTED_SCRAPERS };
	}
	
	public Execution generateExecution(Client client, Resource resource,
			Execution caller) throws ExecutionFatality {
		return new DefaultExecution(client, resource, caller);
	}
	
	public static class DefaultExecution extends Execution {
		private Resource[] substitutedScrapers;
		private String value;
		protected DefaultExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
			super(client, resource, caller);
			substitutedScrapers = getRelatedResources(SUBSTITUTED_SCRAPERS);
		}
		protected Result privateExecute() throws ExecutionDelay, ExecutionFatality {
			value = getStringAttributeValue(VALUE);
			for(int i = 0 ; i < substitutedScrapers.length ; i++) {
				((ScraperDefinition) substitutedScrapers[i]).substitute(value);
			}
			return new DefaultResult();
		}
	}
	
	public static class DefaultResult implements Result {
		
	}
}
