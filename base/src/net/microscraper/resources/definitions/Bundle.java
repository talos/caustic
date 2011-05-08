package net.microscraper.resources.definitions;

import net.microscraper.client.Client;
import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFailure;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.DefaultExecutionProblem.StatusException;
import net.microscraper.resources.Execution;
import net.microscraper.resources.OneToOneResourceDefinition;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Result;
import net.microscraper.resources.Status;

public class Bundle extends OneToOneResourceDefinition {	
	private static final RelationshipDefinition SUBSTITUTIONS =
		new RelationshipDefinition( "substitutions", Substitution.class);
	private static final RelationshipDefinition SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class);
	
	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { }; }
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] { SUBSTITUTIONS, SCRAPERS };
	}
	
	public Execution generateExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
		return new BundleExecution(client, resource, caller);
	}
	
	public static class BundleExecution extends Execution {
		private final Resource[] defaults;
		private final Resource[] scrapers;
		public BundleExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
			super(client, resource, caller);
			defaults = getRelatedResources(SUBSTITUTIONS);
			scrapers = getRelatedResources(SCRAPERS);
		}
		protected Result privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
			Status status = new Status();
			for(int i = 0 ; i < defaults.length ; i ++ ) {
				Execution exc = callResource((Substitution) defaults[i]);
				status.merge(exc.safeExecute());
			}
			for(int i = 0 ; i < scrapers.length ; i ++ ) {
				status.merge(((Scraper) scrapers[i]).execute(getSourceExecution()));
			}
			if(status.hasDelay() || status.hasFailure()) {
				throw new StatusException(status);
			}

			return new BundleResult();
		}
	}
	
	public static class BundleResult implements Result {
		
	}
}
