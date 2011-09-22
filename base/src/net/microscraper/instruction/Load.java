package net.microscraper.instruction;

import java.util.Hashtable;

import net.microscraper.database.DatabasePersistException;
import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.template.DependsOnTemplate;
import net.microscraper.template.HashtableSubstitution;
import net.microscraper.template.HashtableSubstitutionOverwriteException;
import net.microscraper.template.HashtableTemplate;
import net.microscraper.template.StringSubstitution;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.StaticStringTemplate;
import net.microscraper.util.StringUtils;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load {
	
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
	
	/**
	 * A string that will be templated and evaulated as a URL.
	 */
	private final StringTemplate url;
			
	private StringSubstitution getPosts(DatabaseView input)
			throws HashtableSubstitutionOverwriteException, DatabaseReadException {
		if(postTable.size() > 0) {
			HashtableSubstitution tableSub = postTable.sub(input);
			if(tableSub.isMissingTags()) {
				return StringSubstitution.missingTags(tableSub.getMissingTags());
			} else {
				return StringSubstitution.success(HashtableUtils.toFormEncoded(tableSub.getSubstituted()));
			}
		} else {
			return postString.sub(input);
		}
	}
	
	/**
	 * Instantiate a {@link Load}.
	 * @param url
	 */
	public Load(StringTemplate url) {
		this.url = url;
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
	 * @param posts A {@link HashtableTemplate} of posts to add.
	 */
	public void addPosts(HashtableTemplate posts) {
		setMethod(HttpBrowser.POST);
		postTable.merge(posts);
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
	 * @param headers A {@link HashtableTemplate} of headers to add.
	 */
	public void addHeaders(HashtableTemplate headers) {
		this.headers.merge(headers);
	}

	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #cookies}.
	 * @param cookies A {@link HashtableTemplate} of cookies to add.
	 */
	public void addCookies(HashtableTemplate cookies) {
		this.cookies.merge(cookies);
	}
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 */
	public LoadResult execute(HttpBrowser browser, DatabaseView input)
			throws InterruptedException, DatabasePersistException, DatabaseReadException {
		try {
			final LoadResult result;
			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.sub(input);
			final HashtableSubstitution headersSub = headers.sub(input);
			final HashtableSubstitution cookiesSub = cookies.sub(input);
			final StringSubstitution postData = getPosts(input);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()
					|| postData.isMissingTags()) {
				result = LoadResult.missingTags(
					StringSubstitution.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub, postData}));
			} else {
				final String url = (String) urlSub.getSubstituted();
				final String responseBody;
				
				final String postStr = postData.getSubstituted();
				Hashtable headers = headersSub.getSubstituted();
				Hashtable cookies = cookiesSub.getSubstituted();
				if(cookies.size() > 0) {
					browser.addCookies(url, cookies);
				}
				
				if(method.equalsIgnoreCase(HttpBrowser.HEAD)){
					browser.head(url, headers);
					responseBody = ""; // launch children with a blank source.
				} else if(method.equalsIgnoreCase(HttpBrowser.POST)) {
					responseBody = browser.post(url, headers, stops, postStr);
				} else {
					responseBody = browser.get(url, headers, stops);
				}
				
				result = LoadResult.success(url, responseBody);
			}
			return result;
		} catch(HashtableSubstitutionOverwriteException e) {
			return LoadResult.fromSubstitutionOverwrite(e);
		} catch (HttpException e) {
			return LoadResult.fromHttpException(e);
		}
	}
	
	/**
	 * @return The raw URL template.
	 */
	public String toString() {
		return url.toString();
	}
}
