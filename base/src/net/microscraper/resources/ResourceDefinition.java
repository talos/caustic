package net.microscraper.resources;

public interface ResourceDefinition {
	public abstract AttributeDefinition[] getAttributeDefinitions();
	public abstract AttributeDefinition[] getRelationshipDefinitions();
}
