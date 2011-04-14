package net.microscraper.database.schema;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class WebPage extends AbstractResource {

	public Result[] execute(AbstractResult caller) throws TemplateException,
			MissingVariable, ResourceNotFoundException, InterruptedException, BrowserException {
		return new Result[] {
			new Result(caller, this, this.ref().title, Client.context().browser.load(
				attribute_get(URL), relationship(POSTS), relationship(HEADERS), relationship(COOKIES), relationship(TERMINATES),
				caller
			))
		};
	}
	private static final String URL = "url";
	
	private static final RelationshipDefinition TERMINATES =
		new RelationshipDefinition( "terminates", Regexp.class );
	private static final RelationshipDefinition POSTS =
		new RelationshipDefinition( "posts", Post.class );
	private static final RelationshipDefinition HEADERS =
		new RelationshipDefinition( "headers", Header.class );
	private static final RelationshipDefinition COOKIES =
		new RelationshipDefinition( "cookies", Cookie.class );
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() { return new String[] { URL }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					TERMINATES, POSTS, HEADERS, COOKIES
				};
			}
		};
	}
}
