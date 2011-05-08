package net.microscraper.resources.definitions;

import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.Execution;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Status;

public class Bundle extends OneToOneResource {	
	private static final RelationshipDefinition SUBSTITUTIONS =
		new RelationshipDefinition( "substitutions", Substitution.class);
	private static final RelationshipDefinition SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class);
	
	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { }; }
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] { SUBSTITUTIONS, SCRAPERS };
	}
			
	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new DataExecution(this, caller);
	}
	
	public class DataExecution extends Execution {
		private final Resource[] defaults;
		private final Resource[] scrapers;
		public DataExecution(Resource resource, Execution caller) throws ExecutionFatality {
			super(resource, caller);
			defaults = getRelatedResources(SUBSTITUTIONS);
			scrapers = getRelatedResources(SCRAPERS);
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
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
			// TODO what would be meaningful here?
			return "";
		}
	}
}
