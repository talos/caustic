package net.microscraper.execution;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.BrowserException;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Parser;
import net.microscraper.model.Resource;
import net.microscraper.model.Variable;

public class VariableExecution extends ParsableExecution implements Variables {
	private final int matchNumber;
	private final MustacheCompiler mustache;
	private final Context context;
	private final String stringToParse;
	
	//private final Variable[] variables;
	private final Variable variable;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	//private final Leaf[] leaves;
	
	private String result = null;
		
	public VariableExecution(Context context, Execution caller, MustacheCompiler mustache, Variable variable, String stringToParse) {
		super(context, variable, caller);
		this.matchNumber = variable.match;
		this.mustache = mustache;
		this.context = context;
		this.stringToParse = stringToParse;
		
		//variables = variable.getVariables();
		this.variable = variable;
		//leaves = variable.getLeaves();
	}
	
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
	protected Object generateResult(Resource resource) throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			Parser parser = (Parser) resource;
			Interfaces.Regexp.Pattern pattern = mustache.compile(parser.pattern);
			String replacement = mustache.compile(parser.replacement);
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
	protected Execution[] generateChildren(Resource resource, Object result) {
		this.result = (String) result;
		
		Variable[] variables = variable.getVariables();
		Leaf[] leaves = variable.getLeaves();
		Vector variableExecutions = new Vector();
		Vector leafExecutions = new Vector();
		for(int i = 0 ; i < variables.length ; i ++) {
			variableExecutions.add(
					new VariableExecution(context, this, mustache, variables[i], this.result));
		}
		for(int i = 0 ; i < leaves.length ; i ++) {
			leafExecutions.add(
					new LeafExecution(context, mustache, this, leaves[i], this.result));
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
