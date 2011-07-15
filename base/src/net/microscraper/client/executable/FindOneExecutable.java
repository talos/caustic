package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.NameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.server.instruction.FindMany;
import net.microscraper.server.instruction.FindOne;

/**
 * {@link FindOneExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link FindOneExecutable} children.
 * @see Variables
 * @see FindOne
 * @author john
 *
 */
public class FindOneExecutable extends FindExecutable implements Variables {
	
	/**
	 * The {@link FindOneExecutable}s spawned by this {@link FindOneExecutable}.
	 */
	private FindOneExecutable[] spawnedFindOneExecutables;
	
	public FindOneExecutable(Interfaces context,
			FindOne findOne, Variables variables,
			Result sourceResult) {
		super(context, findOne, variables, sourceResult);

	}
	
	/**
	 * 
	 * @param key A String, corresponds to the name of {@link FindOneExecutable}'s result
	 * or the name of one of its children.
	 * @return The value corresponding to this name, either in this {@link FindOneExecutable}
	 * or one of its children.
	 * @throws NullPointerException if the specified key is <code>null</code>.
	 * @throws MissingVariableException if this {@link FindOneExecutable} and its children
	 * contain no result for this key.
	 */
	public String get(String key) throws MissingVariableException {
		if(isComplete()) {
			NameValuePair result = (NameValuePair) getResults()[0];
			
			if(result.getName().equals(key))
				return result.getValue();
			
			Executable[] children = getChildren();
			for(int i = 0 ; i < children.length ; i ++) {
				Variables child = (Variables) children[i]; // All children are Variables
				if(child.containsKey(key))
					return child.get(key);
			}
		}
		Variables variables = getVariables();
		if(variables.containsKey(key))
			return variables.get(key);
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
			NameValuePair result = (NameValuePair) getResults()[0];
			if(result.getName().equals(key))
				return true;
			
			Executable[] children = getChildren();
			for(int i = 0 ; i < children.length ; i ++) {
				Variables child = (Variables) children[i]; // All children are Variables
				if(child.containsKey(key))
					return true;
			}
		}
		return getVariables().containsKey(key);
	}
	
	/**
	 * A single {@link NameValuePair} {@link Result} for {@link FindOneExecutable}.
	 */
	protected Result[] generateResults() throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			FindOne findOne = (FindOne) getResource();
			String replacement = getReplacement();
			String value = getPattern().match(getSource().getValue(), replacement, findOne.match);
			String name = getName();
			return new Result[] { generateResult(name, value) };
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		}
	}
	
	/**
	 * @return {@link FindManyExecutable}s and {@link FindOneExecutable}s.
	 */
	protected Executable[] generateChildren(Result[] results) {
		FindOne findOne = (FindOne) getResource();
		
		FindOne[] childFindOnes = findOne.getFindOnes();
		FindMany[] childFindManys = findOne.getFindManys();
		Vector findOneExecutables = new Vector();
		Vector findManyExecutables = new Vector();
		
		for(int i = 0 ; i < results.length ; i ++) {
			Result sourceResult = results[i];
			for(int j = 0 ; j < childFindOnes.length ; j ++) {
				findOneExecutables.add(
					new FindOneExecutable(getContext(), childFindOnes[j], getVariables(), sourceResult));
			}
			for(int j = 0 ; j < childFindManys.length ; j ++) {
				findManyExecutables.add(
					new FindManyExecutable(getContext(), childFindManys[j], getVariables(), sourceResult));
			}
		}
		this.spawnedFindOneExecutables = new FindOneExecutable[findOneExecutables.size()];
		findOneExecutables.copyInto(this.spawnedFindOneExecutables);
		
		Executable[] children = new Executable[this.spawnedFindOneExecutables.length + findManyExecutables.size()];
		for(int i = 0 ; i < this.spawnedFindOneExecutables.length ; i++) {
			children[i] = this.spawnedFindOneExecutables[i];
		}
		for(int i = 0 ; i < findManyExecutables.size() ; i ++) {
			children[i + this.spawnedFindOneExecutables.length] = (FindManyExecutable) findManyExecutables.elementAt(i);
		}
		return children;
	}
}
