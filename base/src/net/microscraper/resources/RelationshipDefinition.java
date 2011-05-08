package net.microscraper.resources;

public class RelationshipDefinition {
	public final String name;
	public final Class targetClass;
	public RelationshipDefinition(String name, Class targetClass) {
		this.name = name;
		this.targetClass = targetClass;
	}
}