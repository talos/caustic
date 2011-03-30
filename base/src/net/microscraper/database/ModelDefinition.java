package net.microscraper.database;

import net.microscraper.database.Relationships.Relationship;

public interface ModelDefinition {
	public String key();
	public String[] attributes();
	public Relationship[] relationships();
	
	//protected final Relationships relationships = new Relationships(relationships());
	/*
	public final String key = key();
	public final String[] attributes = attributes();
	public final Relationships relationships = new Relationships(relationships());
	*/
}
