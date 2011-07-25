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
	public static final String JSON_PATH_SEP = "/";
	
	private static final String HTTP_SCHEME = "http";
	private static final String FILE_SCHEME = "file";
	
	private final URI uri;
	
	public JavaNetJSONLocation(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}
	
	public JavaNetJSONLocation(URI uri) {
		this.uri = uri;
	}
	
	public JSONLocation resolve(JSONLocation otherLocation) throws JSONInterfaceException {
		try {
			String thisFragment = uri.getFragment();
			String otherFragment = otherLocation.getFragment();
			URI resolvedFragment;
			if(thisFragment != null && otherFragment != null) {
				resolvedFragment = new URI(thisFragment).resolve(otherFragment);
			} else if(otherFragment != null) {
				resolvedFragment = new URI(otherFragment);
			} else if(thisFragment != null) {
				resolvedFragment = new URI(thisFragment);
			} else {
				resolvedFragment = new URI("");
			}
			
			URI resolvedNonFragment = uri.resolve(otherLocation.toString());
			
			URI resolvedURI = new URI(resolvedNonFragment.getScheme(),
					resolvedNonFragment.getSchemeSpecificPart(),
					resolvedFragment.toString());
			
			return new JavaNetJSONLocation(resolvedURI);
		} catch(URISyntaxException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	public JSONLocation resolve(String path) throws JSONInterfaceException {
		try {
			return resolve(new JavaNetJSONLocation(path));
		} catch(URISyntaxException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	public JSONLocation resolve(int index) throws JSONInterfaceException {
		return resolve(Integer.toString(index));
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
		} else if(getFragment().equals("")) {
			return new String[] {};
		} else {
			return Utils.split(getFragment(), JSON_PATH_SEP);
		}
	}
}
