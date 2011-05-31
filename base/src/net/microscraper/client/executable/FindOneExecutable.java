package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.NameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
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
	
	/**
	 * The {@link FindOneExecutable}s spawned by this {@link FindOneExecutable}.
	 */
	private FindOneExecutable[] findOneExecutables;
	
	public FindOneExecutable(Interfaces context,
			FindOne findOne, Variables variables,
			Result sourceResult) {
		super(context, findOne, variables, sourceResult);
	}
	
	/**
	 * 
	 * @param key A String, corresponds to the name of {@link FindOneExecutable}'s result.
	 * @return The {@link FindOneExecutable}'s result's value.
	 * @throws NullPointerException if the specified key is null
	 * @throws MissingVariableException if this {@link FindOneExecutable} and its children
	 * contain no result for this key.
	 * @throws MissingVariableException if the key is not in this {@link FindOneExecutable}s {@link Variable}s.
	 */
	public String get(String key) throws MissingVariableException {
		if(isComplete()) {
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				if(findOneExecutables[i].containsKey(key))
					return findOneExecutables[i].get(key);
			}
			NameValuePair result = (NameValuePair) getResult();
			if(result.getName().equals(key))
				return result.getValue();
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
			NameValuePair result = (NameValuePair) getResult();
			if(result.getName().equals(key))
				return true;
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				if(findOneExecutables[i].containsKey(key))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * A {@link NameValuePair} result for {@link FindOneExecutable}.
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
		
		FindOne[] findOnes = findOne.getFindOnes();
		FindMany[] findManys = findOne.getFindManys();
		Vector findOneExecutables = new Vector();
		Vector findManyExecutables = new Vector();
		
		for(int i = 0 ; i < results.length ; i ++) {
			Result sourceResult = results[i];
			for(int j = 0 ; j < findOnes.length ; j ++) {
				findOneExecutables.add(
					new FindOneExecutable(getContext(), findOnes[j], getVariables(), sourceResult));
			}
			for(int j = 0 ; j < findManys.length ; j ++) {
				findManyExecutables.add(
					new FindManyExecutable(getContext(), findManys[j], getVariables(), sourceResult));
			}
		}
		this.findOneExecutables = new FindOneExecutable[findOneExecutables.size()];
		findOneExecutables.copyInto(this.findOneExecutables);
		
		Executable[] children = new Executable[this.findOneExecutables.length + findManyExecutables.size()];
		for(int i = 0 ; i < this.findOneExecutables.length ; i++) {
			children[i] = this.findOneExecutables[i];
		}
		for(int i = 0 ; i < findManyExecutables.size() ; i ++) {
			children[i + this.findOneExecutables.length] = (FindManyExecutable) findManyExecutables.elementAt(i);
		}
		return children;
	}
}
