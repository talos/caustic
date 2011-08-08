package net.microscraper.executable;

import java.util.Vector;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.NameValuePair;
import net.microscraper.Variables;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;

/**
 * {@link FindOneExecutable} is the {@link Executable} spawned by a {@link FindOne}.
 * It implements {@link Variables}, such that it passes up the values for all of its
 * executed {@link FindOneExecutable} children.
 * @see FindOne
 * @author john
 *
 */
public class FindOneExecutable extends FindExecutable {
	
	/**
	 * The {@link FindOneExecutable}s spawned by this {@link FindOneExecutable}.
	 */
	private FindOneExecutable[] spawnedFindOneExecutables;
	
	private final ScraperExecutable enclosingScraperExecutable;
	
	public FindOneExecutable(Interfaces context,
			FindOne findOne, ScraperExecutable scraperExecutable,
			Result sourceResult) {
		super(context, findOne, scraperExecutable, sourceResult);
		this.enclosingScraperExecutable = scraperExecutable;
	}
	
	protected String localGet(String key) {
		if(isComplete()) {
			Result result = getResults()[0];
			if(result.getName().equals(key))
				return result.getValue();
			for(int i = 0 ; i < spawnedFindOneExecutables.length ; i ++) {
				String spawnedValue = spawnedFindOneExecutables[i].localGet(key);
				if(spawnedValue != null)
					return spawnedValue;
			}
		}
		return null;
	}
	
	/**
	 * A single result for {@link FindOneExecutable}.
	 */
	protected String[] generateResultValues() throws MissingVariableException,
				MustacheTemplateException, ExecutionFailure  {
		try {
			FindOne findOne = (FindOne) getInstruction();
			String replacement = getReplacement();
			return new String[] { getPattern().match(getSource().getValue(), replacement, findOne.getMatch()) };
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
		FindOne findOne = (FindOne) getInstruction();
		
		FindOne[] childFindOnes = findOne.getFindOnes();
		FindMany[] childFindManys = findOne.getFindManys();
		Vector findOneExecutables = new Vector();
		Vector findManyExecutables = new Vector();
		
		for(int i = 0 ; i < results.length ; i ++) {
			Result sourceResult = results[i];
			for(int j = 0 ; j < childFindOnes.length ; j ++) {
				findOneExecutables.add(
					new FindOneExecutable(getInterfaces(), childFindOnes[j], enclosingScraperExecutable, sourceResult));
			}
			for(int j = 0 ; j < childFindManys.length ; j ++) {
				findManyExecutables.add(
					new FindManyExecutable(getInterfaces(), childFindManys[j], enclosingScraperExecutable, sourceResult));
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
