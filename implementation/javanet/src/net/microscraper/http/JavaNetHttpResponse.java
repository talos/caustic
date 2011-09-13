package net.microscraper.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;

import net.microscraper.http.BadURLException;
import net.microscraper.http.HttpResponse;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.util.StringUtils;

/**
 * Implementation of {@link HttpResponse} using {@link java.net}.
 * @author realest
 *
 */
public class JavaNetHttpResponse implements HttpResponse {
	private final int responseCode;
	private final InputStreamReader contentStream;
	private final ResponseHeaders responseHeaders;
	private final URL url;
	private final String redirectLocation;
	
	public JavaNetHttpResponse(HttpURLConnection conn) throws HttpRequestException {
		try {
			conn.connect();
			responseCode = conn.getResponseCode();
			
			responseHeaders = new JavaNetResponseHeaders(conn.getHeaderFields());
			url = conn.getURL();
			
			// Attempt to determine charset from response headers.
			String[] contentTypeHeaders = responseHeaders.getHeaderValues(CONTENT_TYPE_HEADER_NAME);
			String charset = null;
			if(contentTypeHeaders != null) {
				for(int i = 0 ; i < contentTypeHeaders.length ; i ++) {
					String[] nameValuePairs = StringUtils.split(contentTypeHeaders[i], "; ");
					for(int j = 0 ; j < nameValuePairs.length ; j++) {
						String nameValuePair = nameValuePairs[j];
						if(nameValuePair.indexOf('=') != -1) {
							String name = nameValuePair.substring(0, nameValuePair.indexOf('='));
							if(name.equalsIgnoreCase(CHARSET_KEY)) {
								charset = nameValuePair.substring(nameValuePair.indexOf('=') + 1).toUpperCase();
							}
						}
					}
				}
			}
			
			if(isSuccess()) {
				try {
					if(charset != null) {
						contentStream = new InputStreamReader(conn.getInputStream(), charset);				
					} else {
						contentStream = new InputStreamReader(conn.getInputStream());
					}
				} catch(UnsupportedEncodingException e) { // the charset of the response not supported.
					throw new HttpRequestException("Response charset encoding "
								+ StringUtils.quote(charset) + " not supported: " + e.getMessage());
				}
			} else {
				contentStream = null;
			}
			
			if(isRedirect()) {
				redirectLocation = conn.getHeaderField(LOCATION_HEADER_NAME);
			} else {
				redirectLocation = null;
			}
		} catch(SocketTimeoutException e) { // provide information about timeout.
			throw new HttpRequestException("Timeout after " + conn.getReadTimeout() + " milliseconds " +
					", " + e.bytesTransferred + " bytes transferred.");
		} catch(IOException e) { // some other error.
			throw new HttpRequestException(e.getMessage());
		}
	}
	
	public InputStreamReader getContentStream() {
		if(isSuccess() && contentStream != null) {
			return contentStream;
		} else {
			throw new IllegalStateException();
		}
	}

	public boolean isSuccess() {
		return responseCode >= 200 && responseCode < 300;
	}

	public ResponseHeaders getResponseHeaders() {
		return responseHeaders;
	}

	public boolean isRedirect() {
		return responseCode >= 300 && responseCode < 400 ? true : false;
	}

	public String getRedirectLocation() throws BadURLException {
		if(isRedirect()) {
			try {
				return url.toURI().resolve(redirectLocation).toString();
			} catch(URISyntaxException e) {
				throw new BadURLException(url.toString(), e.getMessage());
			}
		} else {
			throw new IllegalStateException();
		}
	}

	public int getResponseCode() {
		return responseCode;
	}
}
