package net.microscraper.database;

import net.microscraper.database.RelationshipDefinition;

public interface ModelDefinition {
	public String key();
	public String[] attributes();
	public RelationshipDefinition[] relationships();
}
