package net.microscraper.instruction;

import java.util.Hashtable;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabasePersistException;
import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpException;
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
	private StringTemplate postString = StringTemplate.staticTemplate("");
	
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
	
	private Instruction[] children = new Instruction[] { };
	
	private StringSubstitution getPosts(DatabaseView input)
			throws HashtableSubstitutionOverwriteException, DatabaseReadException {
		if(postTable.size() > 0) {
			HashtableSubstitution tableSub = postTable.sub(input);
			if(tableSub.isMissingTags()) {
				return StringSubstitution.missingTags(tableSub.getMissingTags());
			} else {
				return StringSubstitution.success(HashtableUtils.toFormEncoded(encoder, tableSub.getSubstituted()));
			}
		} else {
			return postString.sub(input);
		}
	}
	
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
	
	public void setChildren(Instruction[] children) {
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
	public ScraperResult execute(String source, DatabaseView input)
			throws InterruptedException, DatabasePersistException, DatabaseReadException {
		try {
			final ScraperResult result;
			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.subEncoded(input, encoder);
			final HashtableSubstitution headersSub = headers.sub(input);
			final HashtableSubstitution cookiesSub = cookies.sub(input);
			final StringSubstitution postData = getPosts(input);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()
					|| postData.isMissingTags()) {
				result = ScraperResult.missingTags(
					MissingTags.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub, postData}));
			} else {
				final String url = (String) urlSub.getSubstituted();
				final String responseBody;
				
				final String postStr = postData.getSubstituted();
				Hashtable headers = headersSub.getSubstituted();
				Hashtable cookies = cookiesSub.getSubstituted();
				if(cookies.size() > 0) {
					browser.addCookies(url, cookies, encoder);
				}
				
				if(method.equalsIgnoreCase(HttpBrowser.HEAD)){
					browser.head(url, headers);
					responseBody = ""; // launch children with a blank source.
				} else if(method.equalsIgnoreCase(HttpBrowser.POST)) {
					responseBody = browser.post(url, headers, stops, postStr);
				} else {
					responseBody = browser.get(url, headers, stops);
				}
				
				// Each instruction is turned into one Scraper child, launched with the
				// responseBody as the source.
				Scraper[] scraperChildren = new Scraper[children.length];
				for(int i = 0 ; i < children.length ; i ++) {
					scraperChildren[i] = new Scraper(children[i], input, responseBody);
				}
				
				result = ScraperResult.success(url, new DatabaseView[] { input } , scraperChildren);
			}
			return result;
		} catch(HashtableSubstitutionOverwriteException e) {
			return ScraperResult.fromSubstitutionOverwrite(e);
		} catch (HttpException e) {
			return ScraperResult.fromHttpException(e);
		}
	}
	
	/**
	 * @return The raw URL template.
	 */
	public String toString() {
		return url.toString();
	}
}
