package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Variable;

public class VariableExecution extends ParsableExecution implements HasVariableExecutions, HasLeafExecutions {
	private final int matchNumber;
	private final MustacheCompiler mustache;
	private final Context context;
	private final String stringToParse;
	
	private final Variable[] variables;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	private final Leaf[] leaves;
	private LeafExecution[] leafExecutions = new LeafExecution[0];
	
	private String result = null;
		
	public VariableExecution(Context context, Execution caller, MustacheCompiler mustache, Variable variable, String stringToParse) {
		super(context, variable, caller);
		this.matchNumber = variable.match;
		this.mustache = mustache;
		this.context = context;
		this.stringToParse = stringToParse;
		
		variables = variable.getVariables();
		
		leaves = variable.getLeaves();
	}
	
	protected boolean protectedRun() throws MissingVariableException, MustacheTemplateException,
				NoMatchesException, MissingGroupException, IOException, DeserializationException {
		Interfaces.Regexp.Pattern pattern;
		pattern = mustache.compile(getParser().pattern);
		String replacement = mustache.compile(getParser().replacement);
		result = pattern.match(stringToParse, replacement, matchNumber);
		variableExecutions = new VariableExecution[variables.length];
		for(int i = 0 ; i < variables.length ; i ++) {
			variableExecutions[i] = new VariableExecution(context, this, mustache, variables[i], result);
		}
		
		leafExecutions = new LeafExecution[leaves.length];
		for(int i = 0 ; i < leaves.length ; i ++) {
			leafExecutions[i] = new LeafExecution(context, mustache, this, leaves[i], result);
		}
		return true;
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
	
	public Execution[] getChildren() {
		Execution[] children = new Execution[variableExecutions.length + leafExecutions.length];
		for(int i = 0 ; i < variableExecutions.length ; i++) {
			children[i] = variableExecutions[i];
		}
		for(int i = 0 ; i < leafExecutions.length ; i ++) {
			children[i + variableExecutions.length] = leafExecutions[i];
		}
		return children;
	}

	public LeafExecution[] getLeafExecutions() {
		return leafExecutions;
	}

	public VariableExecution[] getVariableExecutions() {
		return variableExecutions;
	}

	public boolean hasPublishValue() {
		if(result != null)
			return true;
		return false;
	}

	public String getPublishValue() {
		return result;
	}
}
