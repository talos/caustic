package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.DatabaseException;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.AbstractHeader.AbstractHeaderExecution;
import net.microscraper.database.schema.Regexp.RegexpExecution;

public class WebPage extends Resource {
	protected ResourceExecution[] generateExecutions(Execution caller)
			throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
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

	protected ResourceExecution getExecution(Execution caller)
			throws ResourceNotFoundException {
		return new WebPageExecution(caller);
	}
	
	public class WebPageExecution extends ResourceExecution {
		String webPageString;
		protected WebPageExecution(Execution caller)
				throws ResourceNotFoundException {
			super(caller);
		}
		
		protected String load() throws TemplateException,
				ResourceNotFoundException, InterruptedException, MissingVariable,
				NoMatches, FatalExecutionException {
			return webPageString;
		}

		private final Hashtable resourcesToHashtable(Resource[] resources)
				throws ResourceNotFoundException, TemplateException, MissingVariable,
				NoMatches, FatalExecutionException {
			Hashtable hash = new Hashtable();
			for(int i = 0 ; i < resources.length ; i ++) {
				AbstractHeaderExecution exc = (AbstractHeaderExecution) call(resources[i]);
				hash.put(exc.getName(), exc.getValue());
			}
			return hash;
		}
		
		protected boolean isOneToMany() {
			return false;
		}

		protected Variables getLocalVariables() {
			return new Variables();
		}

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			try {
				Resource[] loginWebPages = getRelatedResources(LOGIN_WEB_PAGES);
				for(int i = 0 ; i < loginWebPages.length ; i ++) {
					call(loginWebPages[i]);
				}
				
				Hashtable posts = resourcesToHashtable(getRelatedResources(POSTS));
				Hashtable headers = resourcesToHashtable(getRelatedResources(HEADERS));
				Hashtable cookies = resourcesToHashtable(getRelatedResources(COOKIES));
				
				Resource[] terminatesResources = getRelatedResources(TERMINATES);
				RegexpExecution[] terminates = new RegexpExecution[terminatesResources.length];
				for(int i = 0 ; i < terminatesResources.length; i ++) {
					//String pattern = ((AbstractResource.Simple) terminates_resources[i]).getSuccess(caller).value;
					//terminates[i] = Client.context().regexp.compile(pattern);
					terminates[i] = (RegexpExecution) call(terminatesResources[i]);
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
