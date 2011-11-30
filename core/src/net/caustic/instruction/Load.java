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
import net.caustic.util.StaticStringTemplate;
import net.caustic.util.StringUtils;

/**
 * @author realest
 *
 */
public final class Load implements Instruction {
	
	/**
	 * The HTTP request type that will be used. Either {@link HttpBrowser#GET},
	 * {@link HttpBrowser#POST}, or {@link HttpBrowser#HEAD}.
	 */
	private String method = HttpBrowser.GET;

	/**
	 * {@link HashtableTemplate}s of cookie name-values.
	 */
	private final HashtableTemplate cookies = new HashtableTemplate();

	/**
	 * {@link HashtableTemplate}s of generic headers.
	 */
	private final HashtableTemplate headers = new HashtableTemplate();
	
	/**
	 * A {@link StringTemplate} of post data.  Exclusive of {@link #postTable}.
	 */
	private StringTemplate postString = new StaticStringTemplate("");
	
	/**
	 * {@link HashtableTemplate}s of post data.  Exclusive of {@link #postString}.
	 */
	private final HashtableTemplate postTable = new HashtableTemplate();
	
	private final Instruction[] children;
	
	/**
	 * A string that will be templated and evaulated as a URL.
	 */
	private final StringTemplate url;
			
	private StringSubstitution getPosts(Database db, Scope scope)
			throws HashtableSubstitutionOverwriteException, DatabaseException {
		if(postTable.size() > 0) {
			HashtableSubstitution tableSub = postTable.sub(db, scope);
			if(tableSub.isMissingTags()) {
				return StringSubstitution.missingTags(tableSub.getMissingTags());
			} else {
				return StringSubstitution.success(HashtableUtils.toFormEncoded(tableSub.getSubstituted()));
			}
		} else {
			return postString.sub(db, scope);
		}
	}
	
	/**
	 * Instantiate a {@link Load} without a special name.
	 * @param url
	 */
	public Load(StringTemplate url, Instruction[] children) {
		this.url = url;
		this.children = children;
	}
	
	/**
	 * Instantiate a {@link Load} with a special name.
	 * @param name
	 * @param url
	 */
	public Load(StringTemplate name, StringTemplate url, Instruction[] children) {
		this.url = url;
		this.children = children;
	}
	
	/**
	 * Assign {@link #nonDefaultMethod}.  Cannot be changed once it is set.
	 * @param method The {@link String} {@link HttpBrowser#POST}, {@link HttpBrowser#GET}, or 
	 * {@link HttpBrowser#HEAD}, case-insensitive.
	 */
	public void setMethod(String method) {
		if(!method.equalsIgnoreCase(HttpBrowser.POST) &&
				!method.equalsIgnoreCase(HttpBrowser.GET) &&
				!method.equalsIgnoreCase(HttpBrowser.HEAD)){
			throw new IllegalArgumentException("Method " + StringUtils.quote(method) + " is illegal.");
		} else {
			this.method = method;
		}
	}
	
	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #postTable}.
	 * If {@link #method} is not {@link HttpBrowser.POST}, this changes it to be so.
	 * @param posts A {@link HashtableTemplate} of posts to add.  Existing posts with
	 * duplicate names will be overwritten.
	 */
	public void addPosts(HashtableTemplate posts) {
		setMethod(HttpBrowser.POST);
		postTable.extend(posts, true);
	}

	/**
	 * Set the post data for this {@link Load}.
	 * If {@link #method} is not {@link HttpBrowser.POST}, this changes it to be so.
	 * @param postData The {@link StringTemplate} to use as a post.
	 */
	public void setPostData(StringTemplate postData) {
		if(postTable.size() > 0) {
			throw new IllegalArgumentException("Cannot have both postData and postTable");
		}
		setMethod(HttpBrowser.POST);
		this.postString = postData;
	}

	/**
	 * Add to this {@link Load}'s {@link #headers}.
	 * @param headers A {@link HashtableTemplate} of headers to add.  Existing headers with
	 * duplicate names will be overwritten.
	 */
	public void addHeaders(HashtableTemplate headers) {
		this.headers.extend(headers, true);
	}

	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #cookies}.
	 * @param cookies A {@link HashtableTemplate} of cookies to add.  Existing cookies with
	 * duplicate names will be overwritten.
	 */
	public void addCookies(HashtableTemplate cookies) {
		this.cookies.extend(cookies, true);
	}
	
	/**
	 * @return The raw URL template.
	 */
	public String toString() {
		return url.toString();
	}

	public boolean shouldConfirm() {
		return true;
	}
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * <code>source</code> is ignored.
	 */
	public void execute(String source, Database db, Scope scope, HttpBrowser browser)
			throws InterruptedException, DatabaseException {
		try {			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.sub(db, scope);
			final HashtableSubstitution headersSub = headers.sub(db, scope);
			final HashtableSubstitution cookiesSub = cookies.sub(db, scope);
			final StringSubstitution postData = getPosts(db, scope);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()
					|| postData.isMissingTags()) {
				String[] missingTags = StringSubstitution.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub, postData});
				
				db.putMissing(scope, source, this, missingTags);
				return;
			}
			
			// Everything is substituted in, we can actually try to load the page.
			final String url = (String) urlSub.getSubstituted();
			final String responseBody;
			
			final String postStr = postData.getSubstituted();
			Hashtable headers = headersSub.getSubstituted();
			Hashtable cookies = cookiesSub.getSubstituted();
			
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
				db.putReady(scope, responseBody, children[i]);
			}
		} catch(HashtableSubstitutionOverwriteException e) {
			// Failed because of ambiguous mapping
			db.putFailed(scope, source, this, "Instruction template substitution caused ambiguous mapping: "
					+ e.getMessage());
		} catch (HttpException e) {
			db.putFailed(scope, source, this, "Failure during HTTP request or response: " + e.getMessage());
		}
	}
}
