package net.microscraper.impl.uri;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.interfaces.uri.URIInterface;
import net.microscraper.interfaces.uri.URIInterfaceException;

public class JavaNetURI implements URIInterface {

	private static final String HTTP_SCHEME = "http";
	private static final String FILE_SCHEME = "file";
	
	private final URI uri;
	
	public JavaNetURI(String uriString) throws URIInterfaceException {
		try {
			uri = new URI(uriString);

		} catch(URISyntaxException e) {
			throw new URIInterfaceException(e);
		}
	}
	
	public JavaNetURI(URI uri) {
		this.uri = uri;
	}
	
	public URIInterface resolve(URIInterface otherLocation) {
			return new JavaNetURI( uri.resolve(otherLocation.toString()) );
	}
	
	public URIInterface resolve(String path) throws URIInterfaceException {
		return resolve(new JavaNetURI(path));
	}

	public boolean isAbsolute() {
		return getScheme() != null;
	}
	
	public String getScheme() {
		return uri.getScheme();
	}
	
	public String getSchemeSpecificPart() {
		return uri.getSchemeSpecificPart();
	}
	
	public String getFragment() {
		return uri.getFragment();
	}
	
	public String toString() {
		return uri.toString();
	}

	public boolean isFile() {
		if(getScheme() == null) // assumes could be path
			return true;
		return getScheme().equalsIgnoreCase(FILE_SCHEME);
	}

	public boolean isHttp() {
		if(getScheme() == null)
			return false;
		return getScheme().equalsIgnoreCase(HTTP_SCHEME);
	}

	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof URIInterface) {
			URIInterface that = (URIInterface) obj;
			boolean schemesMatch, sspMatch, fragMatch;
			
			if(this.getScheme() == null && that.getScheme() == null) {
				schemesMatch = true;
			} else if(this.getScheme() == null) {
				return false;
			} else {
				schemesMatch = this.getScheme().equals(that.getScheme());
			}

			if(this.getSchemeSpecificPart() == null && that.getSchemeSpecificPart() == null) {
				sspMatch = true;
			} else if(this.getSchemeSpecificPart() == null) {
				return false;
			} else {
				sspMatch = this.getSchemeSpecificPart().equals(that.getSchemeSpecificPart());
			}
			
			if(this.getFragment() == null && that.getFragment() == null) {
				fragMatch = true;
			} else if(this.getFragment() == null) {
				return false;
			} else {
				fragMatch = this.getFragment().equals(that.getFragment());
			}
			
			return schemesMatch && sspMatch && fragMatch;
		}
		return false;
	}

}
