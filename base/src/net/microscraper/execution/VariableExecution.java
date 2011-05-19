package net.microscraper.execution;

import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.model.Leaf;
import net.microscraper.model.Resource;
import net.microscraper.model.Variable;

public class VariableExecution extends ParsableExecution implements HasVariableExecutions, HasLeafExecutions {
	private final int matchNumber;
	private final MustacheCompiler mustache;
	private final Context context;
	private final String stringToParse;
	private final Execution caller;
	
	private final Variable[] variables;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	private final Leaf[] leaves;
	private LeafExecution[] leafExecutions = new LeafExecution[0];
	
	private String result = null;
	
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private Exception failure = null;
	
	public VariableExecution(Context context, Execution caller, MustacheCompiler mustache, Variable variable, String stringToParse) {
		super(context, variable);
		this.matchNumber = variable.match;
		this.mustache = mustache;
		this.context = context;
		this.stringToParse = stringToParse;
		this.caller = caller;
		
		variables = variable.getVariables();
		
		leaves = variable.getLeaves();
	}
	
	public void run() {
		if(!hasFailed()) {
			if(result == null) {
				Interfaces.Regexp.Pattern pattern;
				try {
					pattern = mustache.compile(getParser().pattern);
					String replacement = mustache.compile(getParser().replacement);
					result = pattern.match(stringToParse, replacement, matchNumber);
					
				} catch (MissingVariableException e) {
					lastMissingVariable = missingVariable;
					missingVariable = e.name;
				} catch (MustacheTemplateException e) {
					failure = e;
				} catch (NoMatchesException e) {
					failure = e;
				} catch (MissingGroupException e) {
					failure = e;
				}
			}
			if(result != null) {
				variableExecutions = new VariableExecution[variables.length];
				for(int i = 0 ; i < variables.length ; i ++) {
					variableExecutions[i] = new VariableExecution(context, this, mustache, variables[i], result);
				}
				
				leafExecutions = new LeafExecution[leaves.length];
				for(int i = 0 ; i < leaves.length ; i ++) {
					leafExecutions[i] = new LeafExecution(context, mustache, this, leaves[i], result);
				}
			}
		}
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
	
	public boolean isStuck() {
		if(result == null) {
			if(lastMissingVariable != null && missingVariable != null && result == null) {
				if(lastMissingVariable.equals(missingVariable))
					return true;
			}
		}
		return false;
	}
	
	public boolean isComplete() {
		allChildrenComplete: if(result != null) {
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				if(!variableExecutions[i].isComplete())
					break allChildrenComplete;
			}
			return true;
		}
		return false;
	}
	
	public boolean hasFailed() {
		if(super.hasFailed() == true)
			return true;
		if(failure != null)
			return true;
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

	public Execution getCaller() {
		return caller;
	}
	public boolean hasCaller() {
		return true;
	}

	public boolean hasPublishValue() {
		return true;
	}

	public String getPublishValue() {
		return result;
	}
}
