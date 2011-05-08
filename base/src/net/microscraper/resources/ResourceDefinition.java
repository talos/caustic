package net.microscraper.resources;

public abstract class ResourceDefinition {
	public abstract AttributeDefinition[] getAttributeDefinitions();
	public abstract RelationshipDefinition[] getRelationshipDefinitions();
	public abstract boolean isOneToMany();
	// only true for scraper
	public boolean isPublishedToVariables() {
		return false;
	}
}
