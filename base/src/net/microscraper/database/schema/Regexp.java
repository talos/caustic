package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.ResultSet;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;

public class Regexp extends AbstractResource {
	/*
	public final Interfaces.Regexp.Pattern pattern;
	public Regexp(Resource resource, Variables variables)
					throws TemplateException, MissingVariable {
		pattern = Client.context().regexp.compile(Mustache.compile(resource.attribute_get(Model.REGEXP), variables));
	}
	public Regexp(String pattern_string, Variables variables)
					throws TemplateException, MissingVariable {
		pattern = Client.context().regexp.compile(Mustache.compile(pattern_string, variables));
	}
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Regexp))
			return false;
		Regexp other = (Regexp) obj;
		return this.pattern.toString().equals(other.pattern.toString());	
	}
	public int hashCode() {
		return pattern.toString().hashCode();
	}
	*/
	private static final String REGEXP = "regexp";

	public String[] execute(ResultSet source_result) throws TemplateException, MissingVariable {
		//Pattern pattern = Client.context().regexp.compile(Mustache.compile(attribute_get(REGEXP), source_result.variables()));
		return new String[] {
			Mustache.compile(attribute_get(REGEXP), source_result.variables())
		};
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

}