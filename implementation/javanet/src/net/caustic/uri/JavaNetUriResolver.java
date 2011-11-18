package net.caustic.uri;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.caustic.uri.MalformedUriException;
import net.caustic.uri.RemoteToLocalSchemeResolutionException;
import net.caustic.uri.UriResolver;

public class JavaNetUriResolver implements UriResolver {

	public String resolve(String uriStr, String resolveURIStr)
			throws MalformedUriException,
			RemoteToLocalSchemeResolutionException {
		
		try {
			URI uri;
			try {
				uri = new URI(uriStr);
			} catch(URISyntaxException e) {
				uri = new File(uriStr).toURI();
			}
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
