package net.caustic.instruction;

import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpException;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.template.DependsOnTemplate;
import net.caustic.template.HashtableSubstitution;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.template.HashtableTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.HashtableUtils;

/**
 * @author realest
 *
 */
public final class Load extends Instruction {

	
	/**
	 * Key for {@link Load#headers} when deserializing.
	 */
	public static final String HEADERS = "headers";
		
	/**
	 * Key for {@link Load#posts} when deserializing. 
	 */
	public static final String POSTS = "posts";

	/**
	 * Key for {@link Load#url} when deserializing.
	 */
	public static final String LOAD = "load";
	
	/**
	 * Key for {@link Load#getMethod()} when deserializing. Default is {@link #DEFAULT_METHOD},
	 */
	public static final String METHOD = "method";
	
	/**
	 * Key for {@link Load#cookies} when deserializing. Default is {@link #DEFAULT_COOKIES}.
	 */
	public static final String COOKIES = "cookies";
	
	public static final String METHOD_DEFAULT = HttpBrowser.GET;
	
	/**
	 * The HTTP request type that will be used. Either {@link HttpBrowser#GET},
	 * {@link HttpBrowser#POST}, or {@link HttpBrowser#HEAD}.
	 */
	private final String method;// = HttpBrowser.GET;

	/**
	 * {@link HashtableTemplate}s of cookie name-values.
	 */
	private final HashtableTemplate cookies;// = new HashtableTemplate();

	/**
	 * {@link HashtableTemplate}s of generic headers.
	 */
	private final HashtableTemplate headers;// = new HashtableTemplate();
	
	/**
	 * A {@link StringTemplate} of post data.  Exclusive of {@link #postTable}.
	 */
	private StringTemplate postData;// = new StaticStringTemplate("");
	
	/**
	 * {@link HashtableTemplate}s of post data.  Exclusive of {@link #postData}.
	 */
	private HashtableTemplate postTable;// = new HashtableTemplate();
	
	private final String[] children;
	
	/**
	 * A string that will be templated and evaulated as a URL.
	 */
	private final StringTemplate url;
	
	public Load(String serializedString, String uri,
			StringTemplate url, String[] children, String method,
			HashtableTemplate cookies, HashtableTemplate headers, StringTemplate postData) {
		super(serializedString, uri);
		this.url = url;
		this.method = method;
		this.cookies = cookies;
		this.headers = headers;
		this.postData = postData;
		this.children = children;
	}
	
	public Load(String serializedString, String uri,
			StringTemplate url, String[] children, String method,
			HashtableTemplate cookies, HashtableTemplate headers, HashtableTemplate postTable) {
		super(serializedString, uri);
		this.url = url;
		this.method = method;
		this.cookies = cookies;
		this.headers = headers;
		this.postTable = postTable;
		this.children = children;
	}
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * <code>source</code> is ignored.
	 */
	public void execute(Database db, Scope scope, HttpBrowser browser)
			throws InterruptedException, DatabaseException {
		try {			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.sub(db, scope);
			final HashtableSubstitution headersSub = headers.sub(db, scope);
			final HashtableSubstitution cookiesSub = cookies.sub(db, scope);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()) {
				String[] missingTags = StringSubstitution.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub});
				
				db.putMissing(scope, null, this, missingTags);
				return;
			}
			
			// pull out post string
			final String postStr;
			if(postData != null) {
				StringSubstitution sub = postData.sub(db, scope);
				if(sub.isMissingTags()) {
					db.putMissing(scope, null, this, sub.getMissingTags());
					return;
				} else {
					postStr = sub.getSubstituted();
				}
			} else if(postTable != null) {
				HashtableSubstitution sub = postTable.sub(db, scope);
				if(sub.isMissingTags()) {
					db.putMissing(scope, null, this, sub.getMissingTags());
					return;
				} else {
					postStr = HashtableUtils.toFormEncoded(sub.getSubstituted());
				}
			} else {
				postStr = null;
			}
			
			// Everything is substituted in, we can actually try to load the page.
			final String url = (String) urlSub.getSubstituted();
			final String responseBody;
			
			final Hashtable headers = headersSub.getSubstituted();
			final Hashtable cookies = cookiesSub.getSubstituted();
			
			// add cookies directly into DB
			Enumeration e = cookies.elements();
			while(e.hasMoreElements()) {
				String name = (String) e.nextElement();
				String value = (String) cookies.get(name);
				db.addCookie(scope, url, name, value);
			}
			
			if(method.equalsIgnoreCase(HttpBrowser.HEAD)){
				browser.head(url, headers, db, scope);
				responseBody = ""; // launch children with a blank source.
			} else if(method.equalsIgnoreCase(HttpBrowser.POST)) {
				responseBody = browser.post(url, headers, stops, postStr, db, scope);
			} else {
				responseBody = browser.get(url, headers, stops, db, scope);
			}
			
			// Add children to database
			for(int i = 0 ; i < children.length ; i ++) {
				db.putInstruction(scope, responseBody, children[i], uri);
			}
			
			db.putSuccess(scope, null, this.serialized, this.uri);
		} catch(HashtableSubstitutionOverwriteException e) {
			// Failed because of ambiguous mapping
			db.putFailed(scope, null, serialized, uri,
					"Instruction template substitution caused ambiguous mapping: "
					+ e.getMessage());
		} catch (HttpException e) {
			db.putFailed(scope, null, serialized, uri,
					"Failure during HTTP request or response: " + e.getMessage());
		}
	}
}
