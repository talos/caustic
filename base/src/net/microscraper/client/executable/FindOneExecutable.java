package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.FindOne;

/**
 * {@link FindOneExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link FindOneExecutable} children.  It 
 * @see Variables
 * @see FindOne
 * @author john
 *
 */
public class FindOneExecutable extends FindExecutable implements Variables {
	private final int matchNumber;
	private final String stringToParse;
	
	private final FindOne variable;
	private FindOneExecutable[] variableExecutions = new FindOneExecutable[0];
	
	private String result = null;
	
	public FindOneExecutable(ExecutionContext context,
			Executable parent, FindOne variable, String stringToParse) {
		super(context, variable, parent);
		
		this.matchNumber = variable.match;
		this.stringToParse = stringToParse;
		
		this.variable = variable;
	}
	
	/**
	 * 
	 * @param key A String, corresponds to {@link FindOneExecutable#getName()}.
	 * @return The {@link FindOneExecutable}'s result.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this {@link FindOneExecutable} and its children
	 * contain no result for this key.
	 * @throws MissingVariableException with a {@link FindOneExecutable#getName()}
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
	 * Tests if the specified object is a key in this {@link FindOneExecutable} or
	 * one of its children.
	 * @param key possible key 
	 * @return <code>true</code> if and only if the specified String is a key
	 * in this {@link FindOneExecutable} or one of its children.
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
	 * {@link FindOneExecutable} provides its result as a value if it {@link #isComplete}.
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
	 * A result value for the {@link FindOneExecutable}.
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
	 * @return {@link FindManyExecutable}s and {@link FindOneExecutable}s.
	 */
	protected Executable[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		this.result = (String) result;
		
		FindOne[] variables = variable.getFindOnes();
		FindMany[] leaves = variable.getFindMany();
		Vector variableExecutions = new Vector();
		Vector leafExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			variableExecutions.add(
				new FindOneExecutable(context, this, variables[i], this.result));
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			leafExecutions.add(
				new FindManyExecutable(context, this, this, leaves[i], this.result));
		}
		this.variableExecutions = new FindOneExecutable[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Executable[] children = new Executable[this.variableExecutions.length + leafExecutions.size()];
		for(int i = 0 ; i < this.variableExecutions.length ; i++) {
			children[i] = this.variableExecutions[i];
		}
		for(int i = 0 ; i < leafExecutions.size() ; i ++) {
			children[i + this.variableExecutions.length] = (FindManyExecutable) leafExecutions.elementAt(i);
		}
		return children;
	}
}
