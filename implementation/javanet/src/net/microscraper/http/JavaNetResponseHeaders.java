package net.microscraper.http;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ResponseHeaders} using {@link java.net}.
 * @author talos
 *
 */
public class JavaNetResponseHeaders implements ResponseHeaders {
	private final Map<String, List<String>> responseHeaders;
	
	public JavaNetResponseHeaders(final Map<String, List<String>> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	
	@Override
	public String[] getHeaderValues(String headerName) {
		if(responseHeaders.containsKey(headerName)) {
			List<String> headerValues = responseHeaders.get(headerName);
			return headerValues.toArray(new String[headerValues.size()]);
		} else {
			return null;
		}
	}
	
	@Override
	public String[] getHeaderNames() {
		return responseHeaders.keySet().toArray(new String[responseHeaders.size()]);
	}
}
