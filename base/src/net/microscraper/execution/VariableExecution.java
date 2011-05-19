package net.microscraper.execution;

import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.Variables;
import net.microscraper.model.Leaf;
import net.microscraper.model.Variable;

public class VariableExecution extends ParsableExecution implements Variables, HasLeafExecutions, HasVariableExecutions {
	private final int matchNumber;
	private final MustacheCompiler mustache;
	private final Context context;
	private final String stringToParse;
	
	private final Variable[] variables;
	private VariableExecution[] variableExecutions = new VariableExecution[0];
	
	private final Leaf[] leaves;
	private LeafExecution[] leafExecutions = new LeafExecution[0];
	
	private String result = null;
	
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private Exception failure = null;
	
	public VariableExecution(Context context, MustacheCompiler mustache, Variable variable, String stringToParse) {
		super(context, variable);
		this.matchNumber = variable.match;
		this.mustache = mustache;
		this.context = context;
		this.stringToParse = stringToParse;
		
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
					variableExecutions[i] = new VariableExecution(context, mustache, variables[i], result);
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
	
	public VariableExecution[] getVariableExecutions() {
		return variableExecutions;
	}

	public LeafExecution[] getLeafExecutions() {
		return leafExecutions;
	}
}
