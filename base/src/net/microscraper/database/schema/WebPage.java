package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.DatabaseException;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.AbstractHeader.AbstractHeaderExecution;
import net.microscraper.database.schema.Regexp.RegexpExecution;

public class WebPage extends Resource {
	private final Hashtable executions = new Hashtable();
	
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

	protected WebPageExecution getExecution(Execution caller) throws ResourceNotFoundException {
		if(!executions.containsKey(caller)) {
			executions.put(caller, new WebPageExecution(this, caller));
		}
		return (WebPageExecution) executions.get(caller);
	}

	public Status execute(Variables extraVariables) throws ResourceNotFoundException {
		try {
			WebPageExecution exc = getExecution(null);
			exc.addVariables(extraVariables);
			exc.execute();
			return Status.SUCCESSFUL;
		} catch(MissingVariable e) {
			return Status.IN_PROGRESS;
		} catch(BrowserException e) {
			return Status.FAILURE;
		} catch(FatalExecutionException e) {
			return Status.FAILURE;
		}
	}
	
	public class WebPageExecution extends ResourceExecution {
		String webPageString;
		protected WebPageExecution(Resource resource, Execution caller)
				throws ResourceNotFoundException {
			super(resource, caller);
		}
		
		protected String load() throws TemplateException,
				ResourceNotFoundException, InterruptedException, MissingVariable,
				FatalExecutionException {
			return webPageString;
		}

		private final Hashtable resourcesToHashtable(Resource[] resources)
				throws ResourceNotFoundException, TemplateException, MissingVariable,
				FatalExecutionException {
			Hashtable hash = new Hashtable();
			for(int i = 0 ; i < resources.length ; i ++) {
				AbstractHeaderExecution exc = ((AbstractHeader) resources[i]).getExecution(getSourceExecution());
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

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException {
			try {
				Resource[] loginWebPages = getRelatedResources(LOGIN_WEB_PAGES);
				for(int i = 0 ; i < loginWebPages.length ; i ++) {
					((WebPage) loginWebPages[i]).getExecution(getSourceExecution()).execute();
				}
				
				Hashtable posts = resourcesToHashtable(getRelatedResources(POSTS));
				Hashtable headers = resourcesToHashtable(getRelatedResources(HEADERS));
				Hashtable cookies = resourcesToHashtable(getRelatedResources(COOKIES));
				
				Resource[] terminatesResources = getRelatedResources(TERMINATES);
				RegexpExecution[] terminates = new RegexpExecution[terminatesResources.length];
				for(int i = 0 ; i < terminatesResources.length; i ++) {
					terminates[i] = ((Regexp) terminatesResources[i]).getExecution(getSourceExecution());
				}
				webPageString = Client.browser.load(getAttributeValue(URL), posts, headers, cookies, terminates);
			} catch(DatabaseException e) {
				throw new FatalExecutionException(e);
			} catch(BrowserException e) {
				throw new FatalExecutionException(e);
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			} catch(InterruptedException e ) {
				throw new FatalExecutionException(e);				
			}
		}
	}
}
