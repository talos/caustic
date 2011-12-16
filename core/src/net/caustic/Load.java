package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.http.BrowserResponse;
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
import net.caustic.util.StringMap;

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
	
	public Load(String serializedString, String description, String uri,
			StringTemplate url, String[] children, String method,
			HashtableTemplate cookies, HashtableTemplate headers, StringTemplate postData) {
		super(serializedString, description, uri);
		this.url = url;
		this.method = method;
		this.cookies = cookies;
		this.headers = headers;
		this.postData = postData;
		this.children = children;
	}
	
	public Load(String serializedString, String description, String uri,
			StringTemplate url, String[] children, String method,
			HashtableTemplate cookies, HashtableTemplate headers, HashtableTemplate postTable) {
		super(serializedString, description, uri);
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
	public Response execute(HttpBrowser browser, Request request) throws InterruptedException {
		if(request.force == false) {
			return Response.Wait(request, description); // don't run a load unless it's forced.
		}
		
		final Response response;
		try {
			final StringMap tags = request.tags;
			
			final Pattern[] stops = new Pattern[] { };
			final StringSubstitution urlSub = url.sub(tags);
			final HashtableSubstitution headersSub = headers.sub(tags);
			final HashtableSubstitution cookiesSub = cookies.sub(tags);
			
			// Cannot execute if any of these substitutions was not successful
			if(urlSub.isMissingTags()
					|| headersSub.isMissingTags()
					|| cookiesSub.isMissingTags()) {
				String[] missingTags = StringSubstitution.combine(new DependsOnTemplate[] {
						urlSub, headersSub, cookiesSub});
				
				response = Response.Missing(request, description, missingTags);
			
			} else {
				
				// pull out post string
				final String postStr;
				if(postData != null) {
					StringSubstitution sub = postData.sub(tags);
					if(sub.isMissingTags()) {
						return Response.Missing(request, description, sub.getMissingTags()); // break out early
					} else {
						postStr = sub.getSubstituted();
					}
				} else if(postTable != null) {
					HashtableSubstitution sub = postTable.sub(tags);
					if(sub.isMissingTags()) {
						return Response.Missing(request, description, sub.getMissingTags()); // break out early
					} else {
						postStr = HashtableUtils.toFormEncoded(sub.getSubstituted());
					}
				} else {
					postStr = null;
				}
				
				// Everything is substituted in, we can actually try to load the page.
				final String url = (String) urlSub.getSubstituted();
				
				final Hashtable headers = headersSub.getSubstituted();
				final Hashtable templateCookies = cookiesSub.getSubstituted();
				
				// add cookies directly into DB
				Enumeration e = templateCookies.elements();
				String[] templateCookiesAry = new String[templateCookies.size()];
				int i = 0;
				while(e.hasMoreElements()) {
					String name = (String) e.nextElement();
					String value = (String) templateCookies.get(name);
					
					// TODO the construction of cookies doesn't belong here
					templateCookiesAry[i] = name + '=' + value + "; ";
					i++;
				}
				
				String[] cookiesAry = new String[request.cookies.length
						+ templateCookiesAry.length];
				System.arraycopy(stops, 0, request.cookies, 0, stops.length);
				System.arraycopy(templateCookiesAry, 0, request.cookies, stops.length,
						templateCookiesAry.length);
				
				BrowserResponse bResp = browser.request(url, method, headers, cookiesAry, postStr);
				
				response = Response.DoneLoad(request, description, children, bResp.content, bResp.cookies);
			}
			return response;
		} catch(HashtableSubstitutionOverwriteException e) {
			// Failed because of ambiguous mapping
			return Response.Failed(request, description, "Instruction template substitution caused ambiguous mapping: "
					+ e.getMessage());
		} catch (HttpException e) {
			return Response.Failed(request, description, "Failure during HTTP request or response: " + e.getMessage());
		}
	}
}
