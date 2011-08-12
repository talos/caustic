package net.microscraper.executable;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.Instruction;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.regexp.InvalidRangeException;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;
import net.microscraper.interfaces.regexp.RegexpCompiler;

public class FindManyExecutable extends FindExecutable {
	private final Variables variables;
	public FindManyExecutable(FindMany findMany, 
			RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database) {
		super(findMany, compiler, browser, source, database);
		this.variables = variables;
	}

	
	public final String get(String key) throws MissingVariableException {
		return variables.get(key);
	}
	
	public final boolean containsKey(String key) {
		return variables.containsKey(key);
	}

	public Result[] getResults() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getDefaultName() throws MustacheTemplateException,
			MissingVariableException {
		// TODO Auto-generated method stub
		return null;
	}

}
