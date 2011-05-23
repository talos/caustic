package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.Resource;
import net.microscraper.server.resource.FindOne;

/**
 * {@link VariableExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link VariableExecutable} children.  It 
 * @see Variables
 * @see FindOne
 * @author john
 *
 */
public class VariableExecutable extends ParsableExecutable implements Variables {
	private final int matchNumber;
	private final String stringToParse;
	
	private final FindOne variable;
	private VariableExecutable[] variableExecutions = new VariableExecutable[0];
	
	private String result = null;
	
	public VariableExecutable(ExecutionContext context,
			Executable parent, FindOne variable, String stringToParse) {
		super(context, variable, parent);
		
		this.matchNumber = variable.match;
		this.stringToParse = stringToParse;
		
		this.variable = variable;
	}
	
	/**
	 * 
	 * @param key A String, corresponds to {@link VariableExecutable#getName()}.
	 * @return The {@link VariableExecutable}'s result.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this {@link VariableExecutable} and its children
	 * contain no result for this key.
	 * @throws MissingVariableException with a {@link VariableExecutable#getName()}
	 * corresponding to <code>key</code>. 
	 */
	public String get(String key) throws MissingVariableException {
		if(result != null) {
			if(hasName()) {
				if(getName().equals(key))
					return result;
			}
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				if(variableExecutions[i].containsKey(key))
					return variableExecutions[i].get(key);
			}
		}
		throw new MissingVariableException(this, key);
	}

	/**
	 * Tests if the specified object is a key in this {@link VariableExecutable} or
	 * one of its children.
	 * @param key possible key 
	 * @return <code>true</code> if and only if the specified String is a key
	 * in this {@link VariableExecutable} or one of its children.
	 * @throws NullPointerException if the key is <code>null</code>
	 */
	public boolean containsKey(String key) {
		if(result != null) {
			if(hasName()) {
				if(getName().equals(key))
					return true;
			}
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				if(variableExecutions[i].containsKey(key))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * {@link VariableExecutable} provides its result as a value if it {@link #isComplete}.
	 */
	public boolean hasValue() {
		if(result != null)
			return true;
		return false;
	}
	
	/**
	 * 
	 */
	public String getValue() {
		if(hasValue()) {
			return result;
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * A result value for the {@link VariableExecutable}.
	 */
	protected Object generateResult(ExecutionContext context, Resource resource)
				throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			Parser parser = (Parser) resource;
			PatternInterface pattern = parser.pattern.compile(this, context.regexpInterface);
			String replacement = parser.replacement.compile(this);
			return pattern.match(stringToParse, replacement, matchNumber);
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		}
	}
	
	/**
	 * @return {@link LeafExecutable}s and {@link VariableExecutable}s.
	 */
	protected Executable[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		this.result = (String) result;
		
		FindOne[] variables = variable.getVariables();
		FindMany[] leaves = variable.getLeaves();
		Vector variableExecutions = new Vector();
		Vector leafExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			variableExecutions.add(
				new VariableExecutable(context, this, variables[i], this.result));
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			leafExecutions.add(
				new LeafExecutable(context, this, this, leaves[i], this.result));
		}
		this.variableExecutions = new VariableExecutable[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Executable[] children = new Executable[this.variableExecutions.length + leafExecutions.size()];
		for(int i = 0 ; i < this.variableExecutions.length ; i++) {
			children[i] = this.variableExecutions[i];
		}
		for(int i = 0 ; i < leafExecutions.size() ; i ++) {
			children[i + this.variableExecutions.length] = (LeafExecutable) leafExecutions.elementAt(i);
		}
		return children;
	}
}
