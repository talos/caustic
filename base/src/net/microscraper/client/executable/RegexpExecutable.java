package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.client.interfaces.RegexpCompiler;
import net.microscraper.server.instruction.Regexp;

public class RegexpExecutable {
	private final Regexp regexpResource;
	private final RegexpCompiler regexpCompiler;
	private final Variables variables;
	
	protected RegexpExecutable(Interfaces interfaces, Regexp regexpResource,
			Variables variables) {
		this.regexpCompiler = interfaces.regexpCompiler;
		this.regexpResource = regexpResource;
		this.variables = variables;
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
		return regexpCompiler.compile(
				regexpResource.getPattern().compile(variables),
				regexpResource.getIsCaseSensitive(),
				regexpResource.getIsMultiline(), regexpResource.getDoesDotMatchNewline());
	}
}
