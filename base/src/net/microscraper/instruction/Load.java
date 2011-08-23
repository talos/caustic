package net.microscraper.instruction;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.NameValuePairTemplate;
import net.microscraper.template.Template;
import net.microscraper.util.Encoder;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load implements Action {
	
	/**
	 * The HTTP request type that will be used if {@link #nonDefaultMethod}
	 * is <code>null</code>.
	 */
	private final String defaultMethod = Browser.GET;
	
	/**
	 * If non-<code>null</code>, the HTTP request type that will be used instead of
	 * {@link #defaultMethod}.  Either {@link Browser#GET},
	 * {@link Browser#POST}, or {@link Browser#HEAD}.
	 */
	private String nonDefaultMethod;

	/**
	 * {@link NameValuePairTemplate}s of cookies.
	 */
	private final Vector cookies = new Vector();

	/**
	 * {@link NameValuePairTemplate}s of generic headers.
	 */
	private final Vector headers = new Vector();
	
	/**
	 * {@link PatternTemplate}s that terminate the loading of this page's body.
	 */
	//private final Vector stops = new Vector();
	
	private final Pattern[] stops = new Pattern[] {};
	
	/**
	 * A {@link Template} of post data.  Exclusive of {@link #postNameValuePairs}.
	 */
	private Template postData;
	
	/**
	 * {@link NameValuePairTemplate}s of post data.  Exclusive of {@link #postData}.
	 */
	private final Vector postNameValuePairs = new Vector();
	
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
		} else if(nonDefaultMethod == null) {
			this.nonDefaultMethod = method;
		} else if(!nonDefaultMethod.equalsIgnoreCase(method)) {
			throw new IllegalArgumentException("Cannot reassign method.");
		}
	}
	
	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #postNameValuePairs}.
	 * If {@link #method} is not {@link Browser.POST}, this changes it to be so.
	 * @param postNameValuePair The {@link NameValuePairTemplate} to add as a post.
	 */
	public void addPostNameValuePair(NameValuePairTemplate postNameValuePair) {
		if(this.postData != null) {
			throw new IllegalArgumentException("Cannot have both postData and postNameValuePairs");
		}
		setMethod(Browser.POST);
		this.postNameValuePairs.add(postNameValuePair);
	}

	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #postNameValuePairs}.
	 * If {@link #method} is not {@link Browser.POST}, this changes it to be so.
	 * @param postNameValuePair The {@link NameValuePairTemplate} to add as a post.
	 */
	public void setPostData(Template postData) {
		if(this.postNameValuePairs.size() > 0) {
			throw new IllegalArgumentException("Cannot have both postData and postNameValuePairs");
		}
		setMethod(Browser.POST);
		if(this.postData == null) {
			this.postData = postData;
		} else {
			throw new IllegalArgumentException("Cannot reassign postData");
		}
	}

	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #headers}.
	 * @param header The {@link NameValuePairTemplate} to add as a header.
	 */
	public void addHeader(NameValuePairTemplate header) {
		this.headers.add(header);
	}

	/**
	 * Add a {@link NameValuePairTemplate} to this {@link Load}'s {@link #cookies}.
	 * @param cookie The {@link NameValuePairTemplate} to add as a cookie.
	 */
	public void addCookie(NameValuePairTemplate cookie) {
		this.cookies.add(cookie);
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
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a one-length {@link String}
	 * with the response body, which is a zero-length {@link String} if the {@link Load}'s method
	 * is Head.
	 */
	public Execution execute(String source, Variables variables)
			throws InterruptedException {		
		try {
			String method = nonDefaultMethod == null ? defaultMethod : nonDefaultMethod;
			
			Execution urlSub = url.subEncoded(variables, encoder, Browser.UTF_8);
			
			NameValuePairTemplate[] headersAry = new NameValuePairTemplate[headers.size()];
			headers.copyInto(headersAry);
			Execution headersSub = Execution.arraySubNameValuePair(headersAry, variables);
			
			NameValuePairTemplate[] cookiesAry = new NameValuePairTemplate[cookies.size()];
			cookies.copyInto(cookiesAry);
			Execution cookiesSub = Execution.arraySubNameValuePair(cookiesAry, variables);
			
			if(!urlSub.isSuccessful() || !headersSub.isSuccessful() || !cookiesSub.isSuccessful()) {
				return Execution.combine(new Execution[] {
						urlSub, headersSub, cookiesSub
				});
			} else {
				
				final String responseBody;

				String url = (String) urlSub.getExecuted();
				
				NameValuePair[] headers = (NameValuePair[]) headersSub.getExecuted();
				NameValuePair[] cookies = (NameValuePair[]) cookiesSub.getExecuted();
				
				if(method.equalsIgnoreCase(Browser.HEAD)){
					browser.head(url, headers, cookies);
					responseBody = "";
				} else {
					/*Find[] stopsAry = new Find[stops.size()];
					stops.copyInto(stopsAry);
					Execution stopsSub = Execution.arraySubPattern(stopsAry, variables);*/
					if(method.equalsIgnoreCase(Browser.POST)) {
						if(postNameValuePairs != null) {
							NameValuePairTemplate[] postsAry = new NameValuePairTemplate[postNameValuePairs.size()];
							postNameValuePairs.copyInto(postsAry);
							Execution postsSub = Execution.arraySubNameValuePair(postsAry, variables);
							if(!postsSub.isSuccessful()) {
								return Execution.missingVariables(postsSub.getMissingVariables());
							} else {
								NameValuePair[] posts = (NameValuePair[]) postsSub.getExecuted();
								responseBody = browser.post(url, headers, cookies, stops, posts);
							}
						} else {
							//Execution postsSub = postData.sub(variables, encoder, Browser.UTF_8);
							Execution postsSub = postData.sub(variables);
							if(!postsSub.isSuccessful()) {
								return Execution.missingVariables(postsSub.getMissingVariables());
							} else {
								String postData = (String) postsSub.getExecuted();
								responseBody = browser.post(url, headers, cookies, stops, postData);
							}
						}
					} else {
						responseBody = browser.get(url, headers, cookies, stops);
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
	 * {@Link Load}'s default name is its {@link #url}.
	 */
	public Template getDefaultName() {
		return url;
	}

	/**
	 * {@link Load} does not persist its value by default, because entire pages tend to be large.
	 */
	public boolean getDefaultShouldPersistValue() {
		return false;
	}
}
