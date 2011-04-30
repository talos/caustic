package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.AbstractHeader.AbstractHeaderExecution;

public class WebPage extends Resource {
	private final HashtableWithNulls executions = new HashtableWithNulls();
	
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
	
	public Execution executionFromExecution(Execution caller) throws ExecutionFatality {
		if(!executions.containsKey(caller)) {
			executions.put(caller, new WebPageExecution(this, caller));
		}
		return (WebPageExecution) executions.get(caller);
	}

	public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
		WebPageExecution exc = (WebPageExecution) executionFromExecution(null);
		exc.addVariables(extraVariables);
		return exc;
	}
	
	public class WebPageExecution extends Execution {
		protected WebPageExecution(Resource resource, Execution caller)
				throws ResourceNotFoundException {
			super(resource, caller);
		}
		private final Hashtable resourcesToHashtable(Resource[] resources) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			Hashtable hash = new Hashtable();
			for(int i = 0 ; i < resources.length ; i ++) {
				AbstractHeaderExecution exc = (AbstractHeaderExecution) callResource(resources[i]);
				exc.unsafeExecute();
				hash.put(exc.getName(), exc.getValue());
			}
			return hash;
		}
		
		protected boolean isOneToMany() {
			return false;
		}

		protected Variables getLocalVariables() {
			return null;
		}

		protected String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
			// terminate prematurely if we can't do all login web pages.
			Resource[] loginWebPages = getRelatedResources(LOGIN_WEB_PAGES);
			for(int i = 0 ; i < loginWebPages.length ; i ++) {
				Execution exc = callResource(loginWebPages[i]);
				exc.unsafeExecute();
			}
			
			Hashtable posts = resourcesToHashtable(getRelatedResources(POSTS));
			Hashtable headers = resourcesToHashtable(getRelatedResources(HEADERS));
			Hashtable cookies = resourcesToHashtable(getRelatedResources(COOKIES));
			
			Resource[] terminatesResources = getRelatedResources(TERMINATES);
			Pattern[] terminates = new Pattern[terminatesResources.length];
			for(int i = 0 ; i < terminatesResources.length; i ++) {
				Execution exc = callResource(terminatesResources[i]);
				terminates[i] = Client.regexp.compile(exc.unsafeExecute());
			}
			try {
				return Client.browser.load(getAttributeValue(URL), posts, headers, cookies, terminates);
			} catch(InterruptedException e) {
				throw new ExecutionFatality(e, getSourceExecution());
			}
		}
	}
}
