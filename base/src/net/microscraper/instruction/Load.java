package net.microscraper.instruction;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.database.Scope;
import net.microscraper.http.HttpBrowser;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.DependsOnTemplate;
import net.microscraper.template.HashtableSubstitution;
import net.microscraper.template.HashtableSubstitutionOverwriteException;
import net.microscraper.template.HashtableTemplate;
import net.microscraper.template.MissingTags;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.StringUtils;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load implements Action {
	
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
	private StringTemplate postString;
	
	/**
	 * {@link HashtableTemplate}s of post data.  Exclusive of {@link #postString}.
	 */
	private final HashtableTemplate postTable = new HashtableTemplate();
	
	/**
	 * A string that will be templated and evaulated as a URL.
	 */
	private final StringTemplate url;
	
	/**
	 * The {@link HttpBrowser} to use when loading.
	 */
	private final HttpBrowser browser;

	/**
	 * The {@link Encoder} to use when encoding the URL.
	 */
	private final Encoder encoder;

	/**
	 * Instantiate a {@link Load}.
	 * @param browser
	 * @param encoder
	 * @param url
	 */
	public Load(HttpBrowser browser, Encoder encoder, StringTemplate url) {
		this.browser = browser;
		this.encoder = encoder;
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
		if(postString != null) {
			throw new IllegalArgumentException("Cannot have both postData and postTable");
		}
		if(posts.size() > 0) {
			setMethod(HttpBrowser.POST);
			postTable.merge(posts);
		}
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
	 * @return An {@link ActionResult} whose {@link ActionResult#getResults()} is a one-length
	 * {@link String} array containing the response body, which is a zero-length
	 * {@link String} if the {@link Load}'s method is {@link HttpBrowser#HEAD}.
	 */
	public ActionResult execute(String source, Scope scope)
			throws InterruptedException {
		try {
			final ActionResult result;
			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.subEncoded(scope, encoder);
			final HashtableSubstitution headersSub = headers.sub(scope);
			final HashtableSubstitution cookiesSub = cookies.sub(scope);
			final DependsOnTemplate postData;
			
			if(postTable.size() > 0) {
				postData = postTable.sub(scope);
			} else {
				postData = postString.sub(scope);
			}
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()
					|| postData.isMissingTags()) {
				result = ActionResult.newMissingTags(
					MissingTags.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub, postData}));
			} else {
				
				final String responseBody;

				String url = (String) urlSub.getSubstituted();
				
				Hashtable headers = (Hashtable) headersSub.getSubstituted();
				Hashtable cookies = (Hashtable) cookiesSub.getSubstituted();
				if(cookies.size() > 0) {
					browser.addCookies(url, cookies, encoder);
				}
				
				if(method.equalsIgnoreCase(HttpBrowser.HEAD)){
					browser.head(url, headers);
					responseBody = "";
				} else {
					if(method.equalsIgnoreCase(HttpBrowser.POST)) {
						final String postDataStr;
						if(postTable.size() > 0) {
							Hashtable posts = ((HashtableSubstitution) postData).getSubstituted();
							postDataStr = HashtableUtils.toFormEncoded(encoder, posts);
						} else {
							postDataStr = ((StringSubstitution) postData).getSubstituted();
						}
						
						responseBody = browser.post(url, headers, stops, postDataStr);
					} else {
						responseBody = browser.get(url, headers, stops);
					}
				}
				result = ActionResult.newSuccess(new String[] { responseBody } );
			}
			return result;
		} catch(IOException e) {
			return ActionResult.newFailure("IO Failure while loading: " + e.getMessage());
		} catch(HashtableSubstitutionOverwriteException e) {
			return ActionResult.newFailure(e.getMessage());
		}
	}

	/**
	 * Defaults to {@link #url}.
	 */
	public StringTemplate getDefaultName() {
		return url;
	}
	
}
