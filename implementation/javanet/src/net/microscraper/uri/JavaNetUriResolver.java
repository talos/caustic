package net.microscraper.uri;

import java.net.URI;
import java.net.URISyntaxException;

public class JavaNetUriResolver implements UriResolver {

	public String resolve(String uriStr, String resolveURIStr)
			throws MalformedUriException,
			RemoteToLocalSchemeResolutionException {
		
		try {
			URI uri = new URI(uriStr);
			URI resolveURI = new URI(resolveURIStr);
			
			if(uri.getScheme() == null) {
				return uri.resolve(resolveURI).toString();
			} else if(resolveURI.getScheme() == null) {
				return uri.resolve(resolveURI).toString();
			} else if(uri.getScheme().equals(resolveURI.getScheme())) {
				return uri.resolve(resolveURI).toString();
			} else if(uri.getScheme().equalsIgnoreCase(FILE_SCHEME)) {
				return uri.resolve(resolveURI).toString();
			} else {
				throw new RemoteToLocalSchemeResolutionException(uriStr, resolveURIStr);
			}
		} catch(URISyntaxException e) {
			throw new MalformedUriException(e);
		}
	}
}
