package net.microscraper.resources;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Reference;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Resources;
import net.microscraper.client.Resources.ResourceException;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;

public class Resource {
	private final Resources resources;
	private final ResourceDefinition definition;
	private final Reference ref;
	private final Attributes attributes;
	private final Relationships relationships;

	/**
	 * Inflate a resource serialized in JSON.
	 * @param model
	 * @param reference
	 * @param json_obj
	 * @return
	 * @throws JSONInterfaceException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException
	 */
	public Resource(Resources resources, ResourceDefinition definition, String fullName, Interfaces.JSON.Object jsonObject)
					throws JSONInterfaceException, InstantiationException, IllegalAccessException {
		this.resources = resources;
		this.definition = definition;
		this.ref = new Reference(definition.getClass(), fullName);
		this.attributes = new Attributes(definition.getAttributeDefinitions(), jsonObject);
		this.relationships = new Relationships(definition.getRelationshipDefinitions(), jsonObject);
	}
	
	public Reference ref() {
		return ref;
	}
	public String getRawStringAttribute(AttributeDefinition def) {
		return attributes.getString(def);
	}
	public Integer getIntegerAttribute(AttributeDefinition def) {
		return attributes.getInteger(def);
	}
	public boolean getBooleanAttribute(AttributeDefinition def) {
		return attributes.getBoolean(def);
	}
	public Resource[] getRelatedResources(RelationshipDefinition def) throws ResourceException {
		return relationships.get(resources, def);
	}
	public int getNumberOfRelatedResources(RelationshipDefinition def) {
		return relationships.getSize(def);
	}
	public boolean isOneToMany() {
		return definition.isOneToMany();
	}
	public Execution[] getExecutions(Client client, Execution caller) throws ExecutionFatality {
		return definition.generateExecutions(client, this, caller);
	}
}
