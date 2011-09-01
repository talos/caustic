package net.microscraper.console;

import java.util.HashMap;
import java.util.Map;

public class Option  {
	private static final Map<String, Option> validOptions = new HashMap<String, Option>();

	private static final String PREPEND = "--";
	private final String defaultValue;
	private final String name;
	private Option(String nonPrependedName, String defaultValue) {
		this.name = PREPEND + nonPrependedName;
		this.defaultValue = defaultValue;
		validOptions.put(this.name, this);
	}
	static Option withoutDefault(String nonPrependedName) {
		return new Option(nonPrependedName, null);
	}
	static Option withDefault(String nonPrependedName, String defaultValue) {
		return new Option(nonPrependedName, defaultValue);
	}
	private static boolean exists(String name) {
		return validOptions.containsKey(name);
	}
	static Option retrieve(String name) throws InvalidOptionException {
		if(exists(name)) {
			return validOptions.get(name);
		} else {
			throw new InvalidOptionException(name + " is not a valid option.");
		}
	}
	public String toString() {
		return name;
	}
	public boolean hasDefault() {
		return defaultValue != null;
	}
	public String getDefault() {
		return defaultValue;
	}
}