package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.FindOne;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;

/**
 * {@link FindOneExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link FindOneExecutable} children.
 * @see FindOne
 * @author john
 *
 */
public class FindOneExecutable extends FindExecutable {	
	public FindOneExecutable(Interfaces context,
			FindOne findOne, Executable enclosingExecutable,
			Result sourceResult) {
		super(context, findOne, enclosingExecutable, sourceResult);
	}
	
	protected String localGet(String key) {
		if(isComplete()) {
			Result result = getResults()[0];
			if(result.getName().equals(key))
				return result.getValue();
			for(int i = 0 ; i < getFindOneExecutableChildren().length ; i ++) {
				String spawnedValue = getFindOneExecutableChildren()[i].localGet(key);
				if(spawnedValue != null)
					return spawnedValue;
			}
		}
		return null;
	}
	
	/**
	 * A single result for {@link FindOneExecutable}.
	 */
	protected String[] generateResultValues() throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			FindOne findOne = (FindOne) getInstruction();
			String replacement = getReplacement();
			return new String[] { getPattern().match(getSource().getValue(), replacement, findOne.getMatch()) };
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		}
	}
}
