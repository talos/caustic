package net.microscraper.resources;

public class AttributeDefinition {
	public final String name;
	public final Class targetClass;
	public AttributeDefinition(String name, Class targetClass) {
		this.name = name;
		this.targetClass = targetClass;
	}
}
