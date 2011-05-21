package net.microscraper.execution;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.model.Leaf;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;
import net.microscraper.model.Variable;

public class VariableExecution extends ParsableExecution implements Variables {
	private final int matchNumber;
	private final String stringToParse;
	
	private final Variable variable;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	private String result = null;
		
	public VariableExecution(ExecutionContext context,
			Execution parent, Variable variable, String stringToParse) {
		super(context, variable, parent);
		
		this.matchNumber = variable.match;
		this.stringToParse = stringToParse;
		
		this.variable = variable;
	}
	
	/**
	 * 
	 * @param key A String, corresponds to {@link VariableExecution#getName}.
	 * @return The {@link VariableExecution}'s result.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this {@link VariableExecution} and its children
	 * contain no result for this key.
	 * @throws MissingVariableException with a {@link VariableExecution#getName()}
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
	 * Tests if the specified object is a key in this {@link VariableExecution} or
	 * one of its children.
	 * @param key possible key 
	 * @return <code>true</code> if and only if the specified String is a key
	 * in this {@link VariableExecution} or one of its children.
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

	public boolean hasPublishValue() {
		if(result != null)
			return true;
		return false;
	}

	public String getPublishValue() {
		return result;
	}

	/**
	 * A result value for the {@link VariableExecution}.
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
	 * @return {@link LeafExecution}s and {@link VariableExecution}s.
	 */
	protected Execution[] generateChildren(ExecutionContext context, Resource resource, Object result) {
		this.result = (String) result;
		
		Variable[] variables = variable.getVariables();
		Leaf[] leaves = variable.getLeaves();
		Vector variableExecutions = new Vector();
		Vector leafExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			variableExecutions.add(
				new VariableExecution(context, this, variables[i], this.result));
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			leafExecutions.add(
				new LeafExecution(context, this, this, leaves[i], this.result));
		}
		this.variableExecutions = new VariableExecution[variableExecutions.size()];
		variableExecutions.copyInto(this.variableExecutions);
		
		Execution[] children = new Execution[this.variableExecutions.length + leafExecutions.size()];
		for(int i = 0 ; i < this.variableExecutions.length ; i++) {
			children[i] = this.variableExecutions[i];
		}
		for(int i = 0 ; i < leafExecutions.size() ; i ++) {
			children[i + this.variableExecutions.length] = (LeafExecution) leafExecutions.elementAt(i);
		}
		return children;
	}
}
