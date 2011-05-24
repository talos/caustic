package net.microscraper.client.executable;

import java.io.IOException;
import java.net.URI;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Regexp;

public abstract class RegexpExecutable extends BasicExecutable {

	protected RegexpExecutable(ExecutionContext context, Regexp regexpResource, Variables variables) {
		super(context, regexpResource, variables);
	}

	/**
	 * Mustache-compile this {@link RegexpExecutable}.
	 * @return The {@link Pattern}'s compiled url as a {@link PatternInterface}.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 * @throws MustacheTemplateException If the {@link MustacheTemplate} was invalid.
	 * @see {@link MustacheTemplate#compile(Variables)}
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
			throws BrowserDelayException, MissingVariableException,
			MustacheTemplateException, ExecutionFailure {
		Regexp regexp = (Regexp) getResource();
		return context.regexpInterface.compile(
				regexp.compile(getVariables()),
				isCaseInsensitive,
				isMultiline,
				doesDotMatchNewline);
	}

	public PatternInterface compile(Variables variables, RegexpCompiler regexpInterface)
				throws MissingVariableException, MustacheTemplateException {
	}

	public Resource getResource() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Object generateResult(ExecutionContext context)
			throws BrowserDelayException, MissingVariableException,
			MustacheTemplateException, ExecutionFailure {
		// TODO Auto-generated method stub
		return null;
	}

	protected Executable[] generateChildren(ExecutionContext context,
			Object result) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Mustache-compile an array of {@link Pattern}s.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link Pattern}'s compiled url as a {@link PatternInterface}.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 * @throws MustacheTemplateException If one of the {@link MustacheTemplate}s was invalid.
	 * @see {@link Pattern#compile(Variables, net.microscraper.client.interfaces.Interfaces.RegexpInterface)}
	 */
	/*public static PatternInterface[] compile(Pattern[] uncompiledPatterns, Variables variables,
				RegexpInterface regexpInterface)
			throws MissingVariableException, MustacheTemplateException {
		PatternInterface[] patterns = new PatternInterface[uncompiledPatterns.length];
		for(int i = 0 ; i < uncompiledPatterns.length ; i ++) {
			patterns[i] = uncompiledPatterns[i].compile(variables, regexpInterface);
		}
		return patterns;
	}*/
}
