package net.microscraper.client.interfaces;

/**
 * A class for resolving very simple paths within JSON objects.
 * @author john
 *
 */
public class JSONPath {
	private JSONPath(String path) {
		
	}
	
	public static JSONPath self() {
		return new JSONPath("#");
	}
	
	public static JSONPath key(String key) {
		return new JSONPath(key);
	}
}
