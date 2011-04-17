package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
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
			ResourceNotFoundException, InterruptedException {
		Result result;
		try {
			Hashtable posts = resourcesToHashtable(relationship(POSTS), caller);
			Hashtable headers = resourcesToHashtable(relationship(HEADERS), caller);
			Hashtable cookies = resourcesToHashtable(relationship(COOKIES), caller);
			
			AbstractResource[] terminates_resources = relationship(TERMINATES);
			Pattern[] terminates = new Pattern[terminates_resources.length];
			for(int i = 0 ; i < terminates_resources.length; i ++) {
				Result r = terminates_resources[i].getValue(caller)[0];
				if(r.successful) {
					terminates[i] = Client.context().regexp.compile(((Result.Success) r).value);
				} else {
					throw new MissingVariable((Result.Premature) r);
				}
			}
			result = new Result.Success(caller, this, this.ref().title, Client.context().browser.load(
					attribute_get(URL), posts, headers, cookies, terminates));
		} catch (MissingVariable e) {
			result = new Result.Premature(caller, this, e);
		} catch (BrowserException e) {
			result = new Result.Failure(caller, this, e);
		}
		return new Result[] { result };
	}
	
	private static final Hashtable resourcesToHashtable(AbstractResource[] resources, AbstractResult caller)
				throws MissingVariable, ResourceNotFoundException, TemplateException, InterruptedException {
		Hashtable hash = new Hashtable();
		for(int i = 0 ; i < resources.length ; i ++) {
			Result r = resources[i].getValue(caller)[0];
			if(r.successful) {
				hash.put(((Result.Success) r).key, ((Result.Success) r).value);
			} else {
				throw new MissingVariable((Result.Premature) r);
			}
		}
		return hash;
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
