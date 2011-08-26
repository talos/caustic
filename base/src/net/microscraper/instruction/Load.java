package net.microscraper.instruction;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.client.Browser;
import net.microscraper.client.BasicCookie;
import net.microscraper.database.Variables;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.HashtableTemplate;
import net.microscraper.template.Template;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
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
	 * The HTTP request type that will be used. Either {@link Browser#GET},
	 * {@link Browser#POST}, or {@link Browser#HEAD}.
	 */
	private String method = Browser.GET;

	/**
	 * {@link HashtableTemplate}s of cookie name-values.
	 */
	private final HashtableTemplate cookies = new HashtableTemplate();

	/**
	 * {@link HashtableTemplate}s of generic headers.
	 */
	private final HashtableTemplate headers = new HashtableTemplate();
	
	/**
	 * {@link PatternTemplate}s that terminate the loading of this page's body.
	 */
	//private final Vector stops = new Vector();
	
	private final Pattern[] stops = new Pattern[] {};
	
	/**
	 * A {@link Template} of post data.  Exclusive of {@link #postTable}.
	 */
	private Template postData;
	
	/**
	 * {@link HashtableTemplate}s of post data.  Exclusive of {@link #postData}.
	 */
	private final HashtableTemplate postTable = new HashtableTemplate();
	
	/**
	 * A string that will be templated and evaulated as a URL.
	 */
	private final Template url;
	
	/**
	 * The {@link Browser} to use when loading.
	 */
	private final Browser browser;

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
	public Load(Browser browser, Encoder encoder, Template url) {
		this.browser = browser;
		this.encoder = encoder;
		this.url = url;
	}
	
	/**
	 * Assign {@link #nonDefaultMethod}.  Cannot be changed once it is set.
	 * @param method The {@link String} {@link Browser#POST}, {@link Browser#GET}, or 
	 * {@link Browser#HEAD}, case-insensitive.
	 */
	public void setMethod(String method) {
		if(!method.equalsIgnoreCase(Browser.POST) &&
				!method.equalsIgnoreCase(Browser.GET) &&
				!method.equalsIgnoreCase(Browser.HEAD)){
			throw new IllegalArgumentException("Method " + StringUtils.quote(method) + " is illegal.");
		} else {
			this.method = method;
		}
	}
	
	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #postTable}.
	 * If {@link #method} is not {@link Browser.POST}, this changes it to be so.
	 * @param posts A {@link HashtableTemplate} of posts to add.
	 */
	public void addPosts(HashtableTemplate posts) {
		if(postData != null) {
			throw new IllegalArgumentException("Cannot have both postData and postTable");
		}
		setMethod(Browser.POST);
		postTable.merge(posts);
	}

	/**
	 * Set the post data for this {@link Load}.
	 * If {@link #method} is not {@link Browser.POST}, this changes it to be so.
	 * @param postData The {@link Template} to use as a post.
	 */
	public void setPostData(Template postData) {
		if(postTable.size() > 0) {
			throw new IllegalArgumentException("Cannot have both postData and postNameValuePairs");
		}
		setMethod(Browser.POST);
		this.postData = postData;
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
	 * Add a {@link Find} to this {@link Load}'s {@link #stops}.
	 * @param cookie The {@link NameValuePairTemplate} to add as a cookie.
	 */
	/*public void addStop(Find stop) {
		this.stops.add(stop);
	}*/
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a one-length
	 * {@link String} array containing the response body, which is a zero-length
	 * {@link String} if the {@link Load}'s method is {@link Browser#HEAD}.
	 */
	public Execution execute(String source, Variables variables)
			throws InterruptedException {
		try {			
			Execution urlSub = url.subEncoded(variables, encoder, Browser.UTF_8);
			Execution headersSub = headers.sub(variables);
			Execution cookiesSub = cookies.sub(variables);
			
			// Cannot execute if any of these substitutions was not successful
			if(!urlSub.isSuccessful() || !headersSub.isSuccessful() || !cookiesSub.isSuccessful()) {
				return Execution.combine(new Execution[] {
						urlSub, headersSub, cookiesSub
				});
			} else {
				
				final String responseBody;

				String url = (String) urlSub.getExecuted();
				
				Hashtable headers = (Hashtable) headersSub.getExecuted();
				Hashtable cookies = (Hashtable) cookiesSub.getExecuted();
				if(cookies.size() > 0) {
					browser.addCookies(BasicCookie.fromHashtable(url, cookies));
				}
				
				if(method.equalsIgnoreCase(Browser.HEAD)){
					browser.head(url, headers);
					responseBody = "";
				} else {
					if(method.equalsIgnoreCase(Browser.POST)) {
						String postDataStr;
						if(postTable.size() > 0) {
							Execution postsSub = postTable.subEncoded(variables, encoder, Browser.UTF_8);
							if(!postsSub.isSuccessful()) {
								return Execution.missingVariables(postsSub.getMissingVariables());
							} else {
								Hashtable posts = (Hashtable) postsSub.getExecuted();
								postDataStr = HashtableUtils.toFormEncoded(encoder, posts, Browser.UTF_8);
							}
						} else if(postData != null) {
							Execution postsSub = postData.sub(variables);
							if(!postsSub.isSuccessful()) {
								return Execution.missingVariables(postsSub.getMissingVariables());
							} else {
								postDataStr = (String) postsSub.getExecuted();
							}
						} else {
							postDataStr = "";
						}
						responseBody = browser.post(url, headers, stops, postDataStr);
					} else {
						responseBody = browser.get(url, headers, stops);
					}
				}
				return Execution.success(new String[] { responseBody } );
			}
			//return result;
		} catch(IOException e) {
			return Execution.ioException(e);
		}
	}

	/**
	 * Defaults to {@link #url}.
	 */
	public Template getDefaultName() {
		return url;
	}
	
}
