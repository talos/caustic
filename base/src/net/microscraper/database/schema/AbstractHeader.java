package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.ResultSet;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;

public abstract class AbstractHeader extends AbstractResource {
	public void execute(ResultSet source_result) throws TemplateException, MissingVariable {
		
	}
	public String name(Variables variables) throws TemplateException, MissingVariable {
		return Mustache.compile(attribute_get(NAME), variables);
	}
	public String value(Variables variables) throws TemplateException, MissingVariable {
		return Mustache.compile(attribute_get(VALUE), variables);
	}
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof AbstractHeader))
			return false;
		AbstractHeader other = (AbstractHeader) obj;
		if(this.name.equals(other.name) && this.value.equals(other.value))
			return true;
		else
			return false;		
	}
	public int hashCode() {
		return name.hashCode() + value.hashCode();
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