package net.microscraper.database.schema;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

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

	public ResourceExecution getExecution(Execution caller) throws ResourceNotFoundException {
		return new DataExecution(caller);
	}
	
	public class DataExecution extends ResourceExecution {
		public DataExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
		}

		protected boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
		
		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			
		}
	}
}
