package net.microscraper.client.executable;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.server.resource.URL;

public class URLExecutable extends BasicExecutable {
	
	public URLExecutable(ExecutionContext context, URL url, Variables variables) {
		super(context, url, variables);
	}
	
	/**
	 * Mustache-compile this {@link URL}.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link URL}'s compiled url as a {@link URLInterface}.
	 * @throws MalformedURLException If the compiled {@link java.net.URL} is invalid.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 * @throws MustacheTemplateException If the {@link MustacheTemplate} was invalid.
	 * @see MustacheTemplate#compile(Variables variables)
	 */
	protected Object generateResult(ExecutionContext context)
			throws BrowserDelayException, MissingVariableException,
			MustacheTemplateException, ExecutionFailure {
		URL url = (URL) getResource();
		return context.browser.newURL(url.template.compile(getVariables()));
	}

	protected Executable[] generateChildren(ExecutionContext context,
			Object result) {
		return new Executable[0];
	}
}
