package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Regexp;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

public class RegexpExecutable {
	private final Regexp regexpInstruction;
	private final RegexpCompiler regexpCompiler;
	private final Variables variables;
	
	protected RegexpExecutable(Interfaces interfaces, Regexp regexpInstruction,
			Variables variables) {
		this.regexpCompiler = interfaces.getRegexpCompiler();
		this.regexpInstruction = regexpInstruction;
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
				regexpInstruction.getPattern().compile(variables),
				regexpInstruction.getIsCaseSensitive(),
				regexpInstruction.getIsMultiline(), regexpInstruction.getDoesDotMatchNewline());
	}
}
