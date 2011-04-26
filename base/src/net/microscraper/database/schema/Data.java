package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.Default.DefaultExecution;

public class Data extends Resource {
	
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

	public DataExecution getExecution(Execution caller) throws ResourceNotFoundException {
		return new DataExecution(caller);
	}

	public Status execute(Execution caller) throws ResourceNotFoundException {
		try { 
			getExecution(caller).execute();
			return Status.SUCCESSFUL;
		} catch(MissingVariable e) {
			return Status.IN_PROGRESS;
		} catch(FatalExecutionException e) {
			return Status.FAILURE;
		}
	}
	
	public class DataExecution extends ResourceExecution {
		private final DefaultExecution[] defaults;
		private final Resource[] scrapers;
		public DataExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
			Resource[] defaultResources = getRelatedResources(DEFAULTS);
			defaults = new DefaultExecution[defaultResources.length];
			for(int i = 0 ; i < defaultResources.length ; i ++) {
				defaults[i] = ((Default) defaultResources[i]).getExecution(getSourceExecution());
			}
			scrapers = getRelatedResources(SCRAPERS);
		}

		protected boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			return null;
		}
		
		protected void execute() throws MissingVariable, FatalExecutionException {
			for(int i = 0 ; i < defaults.length ; i ++ ) {
				defaults[i].execute();
			}
			for(int i = 0 ; i < scrapers.length ; i ++ ) {
				
			}
		}
	}
}
