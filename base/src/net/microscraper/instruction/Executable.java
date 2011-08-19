package net.microscraper.instruction;

import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} is an execution-time binding of an {@link Instruction} to
 * the data it needs to run.
 * @see Load
 * @see Find
 * @author talos
 *
 */
public class Executable {
	
	private Execution lastExecution;
	
	private final MustacheTemplate name;
	private final Variables variables;
	private final String source;
	
	private final Find find;
	private final Load load;
	
	private String compiledName;
	private String[] resultValues;
	private Instruction[] childInstructions;
	
	public Executable(MustacheTemplate name, Load load, Variables variables) {
	}
	
	public Executable(MustacheTemplate name, Find find, Variables variables, String source) {
		
	}
	
	public Execution execute() {
		if(compiledName == null) {
			name.sub(variables);
		}
		return Execution.success(variables, name, resultValues, childInstructions);
	}
	
	public Execution lastExecution() {
		if(lastExecution == null) {
			throw new IllegalStateException("Has not been executed.");
		}
		return lastExecution;
	}
	
	public boolean isStuck() {
		// TODO
		return false;
	}
}
