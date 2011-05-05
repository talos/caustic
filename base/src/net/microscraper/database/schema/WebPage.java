package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.WaitToDownload;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;
import net.microscraper.database.schema.AbstractHeader.AbstractHeaderExecution;
import net.microscraper.database.schema.Regexp.RegexpExecution;

public class WebPage extends OneToOneResource {	
	private static final AttributeDefinition URL = new AttributeDefinition("url");
	
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
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { URL }; }
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					TERMINATES, POSTS, HEADERS, COOKIES, LOGIN_WEB_PAGES
				};
			}
		};
	}
	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new WebPageExecution(this, caller);
	}
	public class WebPageExecution extends Execution {
		protected WebPageExecution(Resource resource, Execution caller) {
			super(resource, caller);
		}
		private final Hashtable resourcesToHashtable(Resource[] resources) throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
			Hashtable hash = new Hashtable();
			for(int i = 0 ; i < resources.length ; i ++) {
				AbstractHeaderExecution exc = (AbstractHeaderExecution) callResource((AbstractHeader) resources[i]);
				exc.unsafeExecute();
				hash.put(exc.getName(), exc.getValue());
			}
			return hash;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
			// terminate prematurely if we can't do all login web pages.
			Resource[] loginWebPages = getRelatedResources(LOGIN_WEB_PAGES);
			for(int i = 0 ; i < loginWebPages.length ; i ++) {
				Execution exc = callResource((WebPage) loginWebPages[i]);
				exc.unsafeExecute();
			}
			
			Hashtable posts = resourcesToHashtable(getRelatedResources(POSTS));
			Hashtable headers = resourcesToHashtable(getRelatedResources(HEADERS));
			Hashtable cookies = resourcesToHashtable(getRelatedResources(COOKIES));
			
			Resource[] terminatesResources = getRelatedResources(TERMINATES);
			Pattern[] terminates = new Pattern[terminatesResources.length];
			for(int i = 0 ; i < terminatesResources.length; i ++) {
				Execution exc = callResource((Regexp) terminatesResources[i]);
				exc.unsafeExecute();
				//terminates[i] = Client.regexp.compile(exc.unsafeExecute());
				terminates[i] = ((RegexpExecution) exc).getPattern();
			}
			try {
				return Client.browser.load(getAttributeValue(URL), posts, headers, cookies, terminates);
			} catch(WaitToDownload e) {
				throw new ExecutionDelay(getSourceExecution(), e);
			} catch(InterruptedException e) {
				throw new ExecutionFatality(getSourceExecution(), e);
			} catch(BrowserException e) {
				throw new ExecutionFatality(getSourceExecution(), e.getCause());
			}
		}
	}
}
