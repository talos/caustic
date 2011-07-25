package net.microscraper.impl.json;

import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.Utils;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * Location of a {@link JSONObject}, used to resolve references.
 * @author realest
 *
 */
public class JavaNetJSONLocation implements JSONLocation {
	private static final String HTTP_SCHEME = "http";
	private static final String FILE_SCHEME = "file";
	
	private final URI uri;
	
	public JavaNetJSONLocation(String uriString) throws JSONInterfaceException {
		try {
			URI uri = new URI(uriString);
			if(uri.getFragment() == null) {
				uri = uri.resolve("#/");
			} else if(!uri.getFragment().startsWith(JSON_PATH_SEP)) {
				uri = uri.resolve("#/" + uri.getFragment());
			}
			this.uri = uri;

		} catch(URISyntaxException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	public JavaNetJSONLocation(URI uri) throws JSONInterfaceException {
		if(uri.getFragment() == null) {
			uri = uri.resolve("#/");
		} else if(!uri.getFragment().startsWith(JSON_PATH_SEP)) {
			uri = uri.resolve("#/" + uri.getFragment());
		}
		this.uri = uri;
	}
	
	public JSONLocation resolve(JSONLocation otherLocation) throws JSONInterfaceException {
		if(otherLocation.getSchemeSpecificPart() != null || otherLocation.isAbsolute()) {
			return new JavaNetJSONLocation( uri.resolve(otherLocation.toString()) );
		} else {
			return resolveFragment(otherLocation.getFragment());
		}
	}
	
	public JSONLocation resolve(String path) throws JSONInterfaceException {
		return resolve(new JavaNetJSONLocation(path));
	}

	public JSONLocation resolveFragment(String path) throws JSONInterfaceException {
		try {
			String thisFragment = uri.getFragment();
			
			String resolvedFragment;
			if(thisFragment != null && path != null) {
				if(!thisFragment.endsWith(JSON_PATH_SEP)) {
					thisFragment = thisFragment + JSON_PATH_SEP;
				}
				resolvedFragment = new URI(thisFragment).resolve(path).toString();
			} else if(path != null) {
				resolvedFragment = path;
			} else if(thisFragment != null) {
				resolvedFragment = thisFragment;
			} else {
				resolvedFragment = "";
			}
			
			return new JavaNetJSONLocation(new URI(getScheme(),
					getSchemeSpecificPart(),
					resolvedFragment));
		} catch(URISyntaxException e) {
			throw new JSONInterfaceException(e);
		}
	}

	public JSONLocation resolveFragment(int index) throws JSONInterfaceException {
		return resolveFragment(Integer.toString(index));
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

	public String[] explodeJSONPath() {
		if(getFragment() == null) {
			return new String[] {};
		} else if(getFragment().equals(JSON_PATH_SEP)) {
			return new String[] {};
		} else {
			return Utils.split(getFragment().substring(1), JSON_PATH_SEP);
		}
	}
	
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof JSONLocation) {
			JSONLocation that = (JSONLocation) obj;
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
