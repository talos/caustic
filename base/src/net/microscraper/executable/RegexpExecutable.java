package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Regexp;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

public class RegexpExecutable {
	private final Regexp regexpResource;
	private final RegexpCompiler regexpCompiler;
	private final Variables variables;
	
	protected RegexpExecutable(Interfaces interfaces, Regexp regexpResource,
			Variables variables) {
		this.regexpCompiler = interfaces.getRegexpCompiler();
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
