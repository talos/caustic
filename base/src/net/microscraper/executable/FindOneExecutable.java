package net.microscraper.executable;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.FindOne;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * {@link FindOneExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link FindOneExecutable} children.
 * @see FindOne
 * @author john
 *
 */
public class FindOneExecutable extends FindExecutable {	
	public FindOneExecutable(
			FindOne findOne, RegexpCompiler compiler,
			Browser browser, Variables variables,
			Result source, Database database) {
		super(findOne, compiler, browser, source, database);
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
}
