package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.FindMany;
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
	private final String stringToParse;
	
	private FindOneExecutable[] variableExecutions = new FindOneExecutable[0];
	
	public FindOneExecutable(Interfaces context,
			Executable parent, FindOne findOne, Variables variables,
			String stringToParse) {
		super(context, findOne, variables, parent);
		
		this.stringToParse = stringToParse;
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
		if(isComplete()) {
			FindOne findOne = (FindOne) getResource();
			if(findOne.hasName) {
				if(findOne.name.equals(key))
					return (String) getResult();
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
		if(isComplete()) {
			FindOne findOne = (FindOne) getResource();
			if(findOne.hasName) {
				if(findOne.name.equals(key))
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
	 * A result value for the {@link FindOneExecutable}.
	 */
	protected Object generateResult()
				throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			FindOne findOne = (FindOne) getResource();
			PatternInterface pattern = getPattern();
			String replacement = getReplacement();
			return pattern.match(stringToParse, replacement, findOne.match);
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		}
	}
	
	/**
	 * @return {@link FindManyExecutable}s and {@link FindOneExecutable}s.
	 */
	protected Executable[] generateChildren(Object result) {
		String source = (String) result;
		FindOne findOne = (FindOne) getResource();
		
		FindOne[] findOnes = findOne.getFindOnes();
		FindMany[] findManys = findOne.getFindManys();
		Vector variableExecutions = new Vector();
		Vector leafExecutions = new Vector();
		
		for(int i = 0 ; i < findOnes.length ; i ++) {
			variableExecutions.add(
				new FindOneExecutable(getContext(), this, findOnes[i], getVariables(), source));
		}
		for(int i = 0 ; i < findManys.length ; i ++) {
			leafExecutions.add(
				new FindManyExecutable(getContext(), this, findManys[i], getVariables(), source));
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
