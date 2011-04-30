package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;

public class Data extends OneToOneResource {
	private HashtableWithNulls executions = new HashtableWithNulls();
	
	private static final RelationshipDefinition DEFAULTS =
		new RelationshipDefinition( "defaults", Default.class);
	private static final RelationshipDefinition SCRAPERS =
		new RelationshipDefinition( "scrapers", Scraper.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { DEFAULTS, SCRAPERS };
			}
		};
	}

	
	public class DataExecution extends Execution {
		private final Resource[] defaults;
		private final Resource[] scrapers;
		public DataExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
			defaults = getRelatedResources(DEFAULTS);
			//defaults = new DefaultExecution[defaultResources.length];
			/*for(int i = 0 ; i < defaultResources.length ; i ++) {
				//defaults[i] = ((Default) defaultResources[i]).getExecution(getSourceExecution());
				
			}*/
			scrapers = getRelatedResources(SCRAPERS);
		}

		protected boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			return null;
		}
		
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			//Status status = new Status.Successful(getPublishValue());
			//Status status = new Status();
			Vector statuses = new Vector();
			for(int i = 0 ; i < defaults.length ; i ++ ) {
				//status.merge(defaults[i].execute());
				Execution exc = callResource(defaults[i]);
				statuses.addElement(exc.safeExecute());
			}
			for(int i = 0 ; i < scrapers.length ; i ++ ) {
				//status.merge(((Scraper) scrapers[i]).execute(getSourceExecution()));
				Execution exc = callResource(scrapers[i]);
				statuses.addElement(exc.safeExecute());
			}
			for(int i = 0 ; i < statuses.size() ; i ++ ) {
				
			}
		}
	}
}
