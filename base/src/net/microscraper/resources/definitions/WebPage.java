package net.microscraper.resources.definitions;

import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.WaitToDownload;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFailure;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.DefaultExecutionProblem.StatusException;
import net.microscraper.resources.Execution;
import net.microscraper.resources.OneToOneResourceDefinition;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Result;
import net.microscraper.resources.definitions.AbstractHeader.AbstractHeaderExecution;
import net.microscraper.resources.definitions.Regexp.RegexpExecution;

public class WebPage extends OneToOneResourceDefinition {	
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

	public AttributeDefinition[] getAttributeDefinitions() { return new AttributeDefinition[] { URL }; }
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] {
			TERMINATES, POSTS, HEADERS, COOKIES, LOGIN_WEB_PAGES
		};
	}
	public Execution generateExecution(Client client, Resource resource,
			Execution caller) throws ExecutionFatality {
		return new WebPageExecution(client, resource, caller);
	}
	public class WebPageExecution extends Execution {
		private final Browser browser;
		protected WebPageExecution(Client client, Resource resource, Execution caller) {
			super(client, resource, caller);
			browser = client.browser;
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
		protected Result privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
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
				return new BrowserResult(browser.load(getStringAttributeValue(URL), posts, headers, cookies, terminates));
			} catch(WaitToDownload e) {
				throw new ExecutionDelay(getSourceExecution(), e);
			} catch(InterruptedException e) {
				throw new ExecutionFatality(getSourceExecution(), e);
			} catch(BrowserException e) {
				throw new ExecutionFatality(getSourceExecution(), e.getCause());
			}
		}
	}
	
	public static class BrowserResult implements Result {
		public BrowserResult(String loaded) {
			// TODO
		}
	}
}
