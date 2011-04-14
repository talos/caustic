package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class AbstractHeader extends AbstractResource {
	private String name;
	private String value;
	public AbstractHeader() { }
	public AbstractHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public Result[] execute(Result source_result) throws TemplateException, MissingVariable {
		Variables variables = source_result.variables();
		return new Result[] {
				new Result(
						source_result, this,
						Mustache.compile(name  != null ? name : attribute_get(NAME), variables),
						Mustache.compile(value != null ? value :attribute_get(VALUE), variables))
		};
	}
	
	public static final String NAME = "name";
	public static final String VALUE = "value";

	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() { return new String[] { NAME, VALUE }; }
			public RelationshipDefinition[] relationships() { return new RelationshipDefinition[] {}; }
		};
	}
}