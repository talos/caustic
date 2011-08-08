package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.instruction.FindMany;
import net.microscraper.interfaces.regexp.InvalidRangeException;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;

public class FindManyExecutable extends FindExecutable {
	public FindManyExecutable(Interfaces context, FindMany findMany, 
			Executable enclosingExecutable, Result sourceResult) {
		super(context, findMany, enclosingExecutable, sourceResult);
	}
	
	/**
	 * {@link FindManyExecutable} returns many strings.
	 */
	protected String[] generateResultValues()
			throws MissingVariableException, ExecutionFailure {
		try {
			FindMany findMany = (FindMany) getInstruction();
			
			String replacement = getReplacement();
			return getPattern().allMatches(
					getSource().getValue(),
					replacement,
					findMany.getMinMatch(),
					findMany.getMaxMatch());
			
		} catch(MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		} catch (InvalidRangeException e) {
			throw new ExecutionFailure(e);
		}
	}

	protected boolean generatesManyResults() {
		return true;
	}
}
