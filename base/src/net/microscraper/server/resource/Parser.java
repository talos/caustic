package net.microscraper.server.resource;

import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

public final class Parser extends Resource {
	public static final String KEY = "parser";
	
	public final Pattern pattern;
	public final MustacheTemplate replacement;
	public final Pattern[] tests;
	public Parser(URI location, Pattern pattern,
			MustacheTemplate replacement, Pattern[] tests) throws URIMustBeAbsoluteException {
		super(location);
		this.replacement = replacement;
		this.pattern = pattern;
		this.tests = tests;
	}
	
	private static final String REPLACEMENT = "replacement";
	private static final String TESTS = "tests";
	
	/**
	 * Deserialize a {@link Parser} from a {@link JSONInterfaceObject}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param location {@link URI} from which the resource was loaded.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Parser} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a Parser.
	 */
	public static Parser deserialize(JSONInterface jsonInterface,
				URI location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Pattern pattern = Pattern.deserialize(jsonInterface, jsonObject);
			MustacheTemplate replacement = new MustacheTemplate(jsonObject.getString(REPLACEMENT));
			Pattern[] tests = jsonObject.has(TESTS) ? Pattern.deserializeArray(jsonInterface, jsonObject.getJSONArray(TESTS)) : new Pattern[0];
			
			return new Parser(location, pattern, replacement, tests);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
