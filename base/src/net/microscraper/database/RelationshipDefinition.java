package net.microscraper.database;

public class RelationshipDefinition {
	public final String key;
	public final Model target_model;
	public RelationshipDefinition(String key, Class klass) {
		this.key = key;
		this.target_model = Model.get(klass);
	}
}