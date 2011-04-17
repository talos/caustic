package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;

public class Regexp extends AbstractResource.Simple {
	private static final String REGEXP = "regexp";
	private String regexp;
	public Regexp() { }
	public Regexp(String regexp) {
		this.regexp = regexp;
	}
	
	public ModelDefinition definition() {
		return new ModelDefinition() {	
			public String[] attributes() {
				return new String[] { REGEXP };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { };
			}
		};
	}
	
	protected String getName(AbstractResult caller) {
		return this.ref().title;
	}
	protected String getValue(AbstractResult caller) throws TemplateException, MissingVariable {
		return Mustache.compile(regexp != null ? regexp : attribute_get(REGEXP), caller.variables());
	}
}