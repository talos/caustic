package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.Default.DefaultExecution;

public class Data extends Resource {
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

	public DataExecution getExecution(Execution caller) throws ResourceNotFoundException {
		if(!executions.containsKey(caller)) {
			executions.put(caller, new DataExecution(this, caller));
		}
		return (DataExecution) executions.get(caller);
	}

	public Status execute(Variables extraVariables) throws ResourceNotFoundException, InterruptedException {
		DataExecution exc = getExecution(null);
		exc.addVariables(extraVariables);
		return exc.execute();
	}
	
	public class DataExecution extends ResourceExecution {
		private final DefaultExecution[] defaults;
		private final Resource[] scrapers;
		public DataExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
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
		
		protected Status privateExecute() throws ResourceNotFoundException, InterruptedException {
			Status status = Status.SUCCESSFUL;
			for(int i = 0 ; i < defaults.length ; i ++ ) {
				status.join(defaults[i].execute());
			}
			for(int i = 0 ; i < scrapers.length ; i ++ ) {
				status.join(((Scraper) scrapers[i]).execute(getSourceExecution()));
			}
			return status;
		}
		public String getPublishValue() {
			// TODO Auto-generated method stub
			return "";
		}
	}
}
