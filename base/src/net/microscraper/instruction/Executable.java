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
	
	
	
	public Executable(MustacheTemplate name, Load load, Variables variables) {
	}
	
	public Executable(MustacheTemplate name, Find find, Variables variables, String source) {
		
	}
	
	public Execution execute() {
		// TODO
		return null;
	}
	
	public boolean isStuck() {
		// TODO
		return false;
	}
}
