package net.microscraper.database.schema;

import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;

public class AbstractHeader extends OneToOneResource {
	public static final AttributeDefinition NAME = new AttributeDefinition("name");
	public static final AttributeDefinition VALUE = new AttributeDefinition("value");

	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { NAME, VALUE }; }
			public RelationshipDefinition[] relationships() { return new RelationshipDefinition[] {}; }
		};
	}
	
	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new AbstractHeaderExecution(this, caller);
	}
	
	protected static class AbstractHeaderExecution extends Execution {
		private String name = null;
		private String value = null;
		public AbstractHeaderExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
		}
		
		public boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			return null;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFatality {
			name = getAttributeValue(NAME);
			value = getAttributeValue(VALUE);
			return getName() + '=' + getValue();
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
	}

}