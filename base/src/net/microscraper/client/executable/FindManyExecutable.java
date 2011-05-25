package net.microscraper.client.executable;

import java.util.Vector;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.InvalidRangeException;
import net.microscraper.client.interfaces.MissingGroupException;
import net.microscraper.client.interfaces.NoMatchesException;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Scraper;

public class FindManyExecutable extends FindExecutable {
	private final String stringToParse;
	public FindManyExecutable(Interfaces context,
			Executable parent, FindMany findMany, 
			Variables variables, String stringToParse) {
		super(context, findMany, variables, parent);
		this.stringToParse = stringToParse;
	}
	
	
	/**
	 * Returns a <code>String[]</code>.
	 */
	// TODO: NameValuePair[]
	protected Object generateResult()
			throws MissingVariableException, ExecutionFailure {
		try {
			FindMany findMany = (FindMany) getResource();
			PatternInterface pattern = getPattern();
			String replacement = getReplacement();
			return pattern.allMatches(
					stringToParse,
					replacement,
					findMany.minMatch,
					findMany.maxMatch);
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
	 * @return An array of {@link ScraperExecutableChild}s.
	 * @throws MustacheTemplateException 
	 * @throws MissingVariableException 
	 */
	protected Executable[] generateChildren(Object result) throws MissingVariableException, MustacheTemplateException {
		String[] results = (String[]) result;
		FindMany findMany = (FindMany) getResource();
		Scraper[] scrapers = findMany.getScrapers();
		
		Vector scraperExecutions = new Vector();
		for(int i = 0 ; i < scrapers.length ; i ++) {
			if(findMany.hasName) {
				for(int j = 0 ; j < results.length ; j ++ ) {
					scraperExecutions.add(
						new ScraperExecutableChild(
								getContext(),
								this, scrapers[i],
								getVariables(),
								findMany.name.compile(getVariables()), results[j]));
				}
			} else {
				new ScraperExecutableChild(
						getContext(),
						this, scrapers[i], getVariables());
			}
		}
		Executable[] children = new ScraperExecutable[scraperExecutions.size()];
		scraperExecutions.copyInto(children);
		return children;
	}
}
