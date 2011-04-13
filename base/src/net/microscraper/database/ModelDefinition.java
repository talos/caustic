package net.microscraper.database;


public interface ModelDefinition {
	public String[] attributes();
	public RelationshipDefinition[] relationships();
}
