package net.microscraper.database;

public class RelationshipDefinition {
	public final String key;
	public final String model_key;
	public RelationshipDefinition(String _key, String _model_key) {
		key = _key;
		model_key = _model_key;
	}
}