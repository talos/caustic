package net.microscraper.client.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.NameValuePair;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.InvalidRangeException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.FindMany;
import net.microscraper.server.instruction.Page;
import net.microscraper.server.instruction.Regexp;
import net.microscraper.server.instruction.Scraper;

public class FindManyExecutable extends FindExecutable {
	public FindManyExecutable(Interfaces context, FindMany findMany, 
			Variables variables, Result sourceResult) {
		super(context, findMany, variables, sourceResult);
	}
	
	
	/**
	 * {@link FindManyExecutable} returns an array of {@link NameValuePair}s.
	 */
	protected Result[] generateResults()
			throws MissingVariableException, ExecutionFailure {
		try {
			FindMany findMany = (FindMany) getResource();
			
			String replacement = getReplacement();
			String name = getName();
			String[] values = getPattern().allMatches(
					getSource().getValue(),
					replacement,
					findMany.minMatch,
					findMany.maxMatch);
			Result[] results = new Result[values.length];
			for(int i = 0 ; i < results.length ; i++) {
				results[i] = generateResult(name, values[i]);
			}
			return results;
		} catch(MustacheTemplateException e) {
			throw new ExecutionFailure(e);
		} catch (NoMatchesException e) {
			throw new ExecutionFailure(e);
		} catch (MissingGroupException e) {
			throw new ExecutionFailure(e);
		} catch (InvalidRangeException e) {
			throw new ExecutionFailure(e);
		}
	}

	/**
	 * @return An array of {@link ScraperExecutable}s.
	 * @throws MustacheTemplateException 
	 * @throws MissingVariableException 
	 * @throws IOException 
	 * @throws DeserializationException 
	 */
	protected Executable[] generateChildren(Result[] results) throws MissingVariableException, MustacheTemplateException, DeserializationException, IOException {
		FindMany findMany = (FindMany) getResource();
		Scraper[] scrapers = findMany.getScrapers();
		Page[] pages = findMany.getPages();
		
		Vector children = new Vector();
		
		for(int i = 0 ; i < results.length ; i ++) {
			Result source = results[i];
			for(int j = 0 ; j < scrapers.length ; j++) {
				children.add(new SpawnedScraperExecutable(getContext(), scrapers[j], getVariables(), source));
			}
			for(int j = 0 ; j < pages.length ; j++) {
				children.add(new PageExecutable(getContext(), pages[j], getVariables(), source));
			}
		}
		Executable[] childrenAry = new ScraperExecutable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
