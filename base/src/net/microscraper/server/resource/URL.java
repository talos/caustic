package net.microscraper.server.resource;

import java.net.MalformedURLException;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Mustache;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL {
	private final MustacheTemplate urlTemplate;
	public URL(MustacheTemplate urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Create a {@link URL} from a String.
	 * @param String Input string.
	 * @return A {@link URL} instance.
	 */
	public static URL fromString(String urlTemplate) {
		return new URL(new MustacheTemplate(urlTemplate));
	}
	
	/**
	 * Mustache-compile this {@link URL}.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link URL}'s compiled url as a {@link java.net.url}.
	 * @throws MalformedURLException If the compiled {@link java.net.URL} is invalid.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 * @throws MustacheTemplateException If the {@link MustacheTemplate} was invalid.
	 * @see MustacheTemplate#compile(Variables variables)
	 */
	public java.net.URL compile(Variables variables)
			throws MalformedURLException, MissingVariableException, MustacheTemplateException {
		return new java.net.URL(urlTemplate.compile(variables));
	}
}