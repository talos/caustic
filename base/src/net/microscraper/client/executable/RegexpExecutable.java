package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.Find;
import net.microscraper.server.resource.Regexp;

public class RegexpExecutable extends BasicExecutable {

	protected RegexpExecutable(Interfaces context, Regexp regexpResource,
			Variables variables, Executable parent) {
		super(context, regexpResource, variables, parent);
	}

	/**
	 * 
	 * @return The {@link RegexpExecutable}'s {@link PatternInterface}
	 * to use when executing.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 * @throws MustacheTemplateException if the {@link MustacheTemplate} for
	 * the pattern is invalid.
	 */
	protected PatternInterface getPattern()
			throws MissingVariableException, MustacheTemplateException {
		Find find = (Find) getResource();
		return getContext().regexpCompiler.compile(
				find.pattern.compile(getVariables()),
				find.isCaseSensitive,
				find.isMultiline, find.doesDotMatchNewline);
	}
	
	protected Object generateResult() throws BrowserDelayException,
			MissingVariableException, MustacheTemplateException,
			ExecutionFailure {
		return getPattern();
	}
}
