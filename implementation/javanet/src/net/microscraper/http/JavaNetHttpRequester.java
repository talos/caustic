package net.microscraper.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.HttpResponse;

/**
 * A browser implementation using {@link java.net.HttpURLConnection} and 
 * {@link java.net.CookieManager}.
 * @author john
 *
 */
public class JavaNetHttpRequester implements HttpRequester {
	
	private int timeoutSeconds = HttpRequester.DEFAULT_TIMEOUT_SECONDS;
	
	/**
	 * Request a {@link HttpURLConnection}, and follow any redirects while adding cookies.
	 * @param method The {@link Method} to use.
	 * @param urlStr A URL to load.  Also defaults to be the Referer in the request header.
	 * @param headers A {@link Hashtable} of additional headers.
	 * @param encodedPostData A {@link String} of post data to send, already encoded.
	 * @return A {@link HttpResponse}.
	 * @throws IOException If there was an error generating the {@link HttpURLConnection}.
	 */
	private HttpResponse getResponse(String method, String urlStr, Hashtable requestHeaders,
			String encodedPostData) throws IOException {
		HttpURLConnection.setFollowRedirects(false); // this is handled manually
		HttpURLConnection conn = (HttpURLConnection) (new URL(urlStr)).openConnection();	
		
		// Add additional headers
		Enumeration<String> headerNames = requestHeaders.keys();
		while(headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			String headerValue = (String) requestHeaders.get(headerName);
			conn.setRequestProperty(headerName, headerValue);
		}
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(timeoutSeconds * 1000);
		
		// Set method
		if(method.equalsIgnoreCase(HttpBrowser.POST)) {
			conn.setRequestMethod("POST");
			OutputStreamWriter writer = null;
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(encodedPostData);
			writer.flush();
		} else {
			conn.setRequestMethod(method.toUpperCase());
		}
		
		// Try to connect.
		try {
			return new JavaNetHttpResponse(conn);
		} catch(SocketTimeoutException e) {
			throw new IOException("Timeout after " + conn.getReadTimeout() + " seconds " +
					", " + e.bytesTransferred + " bytes transferred.");
		}
	}
	
	@Override
	public HttpResponse head(String url, Hashtable requestHeaders)
			throws IOException, InterruptedException {
		return getResponse("HEAD", url, requestHeaders, null);
	}

	@Override
	public HttpResponse get(String url, Hashtable requestHeaders)
			throws IOException, InterruptedException {
		return getResponse("GET", url, requestHeaders, null);
	}

	@Override
	public HttpResponse post(String url, Hashtable requestHeaders,
			String encodedPostData) throws IOException, InterruptedException {
		return getResponse("POST", url, requestHeaders, null);
	}

	@Override
	public void setTimeout(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
	
}
