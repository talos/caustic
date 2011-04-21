package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Result;

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

	public class DataResult implements Result {
		
	}

	public ResourceExecution getExecution(Execution caller)
			throws MissingVariable, FatalExecutionException {
		return new DataExecution(caller);
	}
	
	public class DataExecution extends ResourceExecution {
		public DataExecution(Execution caller) {
			super(caller);
		}

		public Result getResult() throws MissingVariable,
				FatalExecutionException {
			// TODO Auto-generated method stub
			return null;
		}

		protected Status execute(Variables v) {
			// TODO Auto-generated method stub
			return null;
		}

		protected Status execute() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public Variables variables() {
			Variables variables = new Variables();
			
		}

		public boolean isOneToMany() {
			return false;
		}
	}
}
