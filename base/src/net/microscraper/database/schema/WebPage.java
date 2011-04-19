package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class WebPage extends AbstractResource.Simple {
	protected String getName(AbstractResult caller) throws TemplateException,
			ResourceNotFoundException, InterruptedException, MissingVariable,
			NoMatches, FatalExecutionException {
		return this.ref().title;
	}

	protected String getValue(AbstractResult caller) throws TemplateException,
			ResourceNotFoundException, InterruptedException, MissingVariable,
			NoMatches, FatalExecutionException {
		
		AbstractResource[] login_web_pages = relationship(LOGIN_WEB_PAGES);
		for(int i = 0 ; i < login_web_pages.length ; i ++) {
			WebPage login_web_page = (WebPage) login_web_pages[i];
			login_web_page.getValue(caller); // we only care about headers.
		}
		
		Hashtable posts = resourcesToHashtable(relationship(POSTS), caller);
		Hashtable headers = resourcesToHashtable(relationship(HEADERS), caller);
		Hashtable cookies = resourcesToHashtable(relationship(COOKIES), caller);
		
		AbstractResource[] terminates_resources = relationship(TERMINATES);
		Pattern[] terminates = new Pattern[terminates_resources.length];
		for(int i = 0 ; i < terminates_resources.length; i ++) {
			String pattern = ((AbstractResource.Simple) terminates_resources[i]).getSuccess(caller).value;
			terminates[i] = Client.context().regexp.compile(pattern);
		}
		try {
			return Client.context().browser.load(attribute_get(URL), posts, headers, cookies, terminates);
		} catch(BrowserException e) {
			throw new FatalExecutionException(e);
		}
	}
	
	private static final Hashtable resourcesToHashtable(AbstractResource[] resources, AbstractResult caller)
			throws ResourceNotFoundException, TemplateException, InterruptedException, MissingVariable,
			NoMatches, FatalExecutionException{
		Hashtable hash = new Hashtable();
		for(int i = 0 ; i < resources.length ; i ++) {
			Result.Success r = ((AbstractHeader) resources[i]).getSuccess(caller);
			hash.put(((Result.Success) r).key, ((Result.Success) r).value);
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
	
	private static final RelationshipDefinition LOGIN_WEB_PAGES =
		new RelationshipDefinition( "login_web_pages", WebPage.class );
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() { return new String[] { URL }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					TERMINATES, POSTS, HEADERS, COOKIES, LOGIN_WEB_PAGES
				};
			}
		};
	}
}
