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

public class AbstractHeader extends OneToOneResourceDefinition {
	public static final AttributeDefinition NAME = new AttributeDefinition("name");
	public static final AttributeDefinition VALUE = new AttributeDefinition("value");

	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { NAME, VALUE }; }
	public RelationshipDefinition[] getRelationshipDefinitions() { return new RelationshipDefinition[] {}; }
	
	public Execution generateExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
		return new AbstractHeaderExecution(client, resource, caller);
	}
	
	protected static class AbstractHeaderExecution extends Execution {
		public AbstractHeaderExecution(Client client, Resource resource, Execution caller) {
			super(client, resource, caller);
		}
		protected Result privateExecute() throws ExecutionDelay, ExecutionFatality {
			return new AbstractHeaderResult(getStringAttributeValue(NAME), getStringAttributeValue(VALUE));
		}
	}
	
	public static class AbstractHeaderResult implements Result {
		public final String name;
		public final String value;
		public AbstractHeaderResult(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public String toString() {
			return name + '=' + value;
		}
	}
}