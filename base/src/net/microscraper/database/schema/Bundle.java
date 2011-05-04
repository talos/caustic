package net.microscraper.database.schema;

import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;
import net.microscraper.database.Status;

public class Bundle extends OneToOneResource {	
	private static final RelationshipDefinition SUBSTITUTIONS =
		new RelationshipDefinition( "substitutions", Substitution.class);
	private static final RelationshipDefinition SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { SUBSTITUTIONS, SCRAPERS };
			}
		};
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
