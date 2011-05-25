package net.microscraper.client;

import java.util.Hashtable;

/**
 * An implementation of {@link Variables} initialized with
 * several {@link NameValuePair}s.
 * @author realest
 *
 */
public class DefaultVariables implements Variables {
	
	private final Hashtable defaults = new Hashtable();
	
	public DefaultVariables(NameValuePair[] nameValuePairs) {
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			defaults.put(nameValuePairs[i].getName(), nameValuePairs[i].getValue());
		}
	}
	
	public String get(String key) throws MissingVariableException {
		Object value = defaults.get(key);
		if(value == null) {
			throw new MissingVariableException(this, key);
		}
		return (String) value;
	}

	public boolean containsKey(String key) {
		return defaults.containsKey(key);
	}
}
