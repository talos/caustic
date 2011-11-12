package net.caustic.instruction;

import java.util.Hashtable;

import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpException;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.DependsOnTemplate;
import net.caustic.template.HashtableSubstitution;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.template.HashtableTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StaticStringTemplate;
import net.caustic.util.StringUtils;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load extends Instruction {
	
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
			
	private StringSubstitution getPosts(DatabaseView source)
			throws HashtableSubstitutionOverwriteException, DatabaseReadException {
		if(postTable.size() > 0) {
			HashtableSubstitution tableSub = postTable.sub(source);
			if(tableSub.isMissingTags()) {
				return StringSubstitution.missingTags(tableSub.getMissingTags());
			} else {
				return StringSubstitution.success(HashtableUtils.toFormEncoded(tableSub.getSubstituted()));
			}
		} else {
			return postString.sub(source);
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
	 * @return The raw URL template.
	 */
	public String toString() {
		return url.toString();
	}

	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * <code>source</code> is ignored.
	 */
	public InstructionResult execute(String source, DatabaseView view,
			HttpBrowser browser) throws InterruptedException, DatabaseException {
		final InstructionResult result;
		try {			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.sub(view);
			final HashtableSubstitution headersSub = headers.sub(view);
			final HashtableSubstitution cookiesSub = cookies.sub(view);
			final StringSubstitution postData = getPosts(view);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()
					|| postData.isMissingTags()) {
				result = InstructionResult.missingTags(
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
				result = InstructionResult.success(url, new String[] { responseBody }, getChildren(), false);
			}
		} catch(HashtableSubstitutionOverwriteException e) {
			return InstructionResult.fromSubstitutionOverwrite(e);
		} catch (HttpException e) {
			return InstructionResult.fromHttpException(e);
		}
		return result;
	}
}
