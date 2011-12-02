package net.caustic.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequestException;
import net.caustic.http.HttpRequester;
import net.caustic.http.HttpResponse;

/**
 * A browser implementation using {@link java.net.HttpURLConnection} and 
 * {@link java.net.CookieManager}.
 * @author john
 *
 */
public class JavaNetHttpRequester implements HttpRequester {
	
	private int timeoutMilliseconds = HttpBrowser.DEFAULT_TIMEOUT_MILLISECONDS;
	
	/**
	 * Request a {@link HttpURLConnection}, and follow any redirects while adding cookies.
	 * @param method The {@link Method} to use.
	 * @param urlStr A URL to load.  Also defaults to be the Referer in the request header.
	 * @param headers A {@link Hashtable} of additional headers.
	 * @param encodedPostData A {@link String} of post data to send, already encoded.
	 * @return A {@link HttpResponse}.
	 */
	private HttpResponse getResponse(String method, String urlStr, Hashtable requestHeaders,
			String encodedPostData) throws HttpRequestException {
		HttpURLConnection.setFollowRedirects(false); // we handle this manually
		
		try {
			HttpURLConnection conn = (HttpURLConnection) (new URL(urlStr)).openConnection();	
			
			// Add additional headers
			Enumeration<String> headerNames = requestHeaders.keys();
			while(headerNames.hasMoreElements()) {
				String headerName = (String) headerNames.nextElement();
				String headerValue = (String) requestHeaders.get(headerName);
				conn.setRequestProperty(headerName, headerValue);
			}
			
			conn.setDoInput(true);
			conn.setReadTimeout(timeoutMilliseconds);
			
			// Set method
			if(method.equalsIgnoreCase(HttpBrowser.POST)) {
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(encodedPostData);
				writer.flush();
			} else {
				conn.setRequestMethod(method.toUpperCase());
			}
			return new JavaNetHttpResponse(conn);
		} catch(IOException e) {
			throw new HttpRequestException(e.getMessage());
		}
	}
	
	@Override
	public HttpResponse head(String url, Hashtable requestHeaders)
			throws InterruptedException, HttpRequestException {
		return getResponse("HEAD", url, requestHeaders, null);
	}

	@Override
	public HttpResponse get(String url, Hashtable requestHeaders)
			throws InterruptedException, HttpRequestException {
		return getResponse("GET", url, requestHeaders, null);
	}

	@Override
	public HttpResponse post(String url, Hashtable requestHeaders,
			String encodedPostData) throws InterruptedException, HttpRequestException {
		return getResponse("POST", url, requestHeaders, encodedPostData);
	}

	@Override
	public void setTimeout(int timeoutMilliseconds) {
		this.timeoutMilliseconds = timeoutMilliseconds;
	}
	
}
