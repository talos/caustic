package net.microscraper.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Scraper;
import net.microscraper.interfaces.regexp.InvalidRangeException;
import net.microscraper.interfaces.regexp.MissingGroupException;
import net.microscraper.interfaces.regexp.NoMatchesException;

public class FindManyExecutable extends FindExecutable {
	public FindManyExecutable(Interfaces context, FindMany findMany, 
			ScraperExecutable scraperExecutable, Result sourceResult) {
		super(context, findMany, scraperExecutable, sourceResult);
	}
	
	/**
	 * {@link FindManyExecutable} returns many strings.
	 */
	protected String[] generateResultValues()
			throws MissingVariableException, ExecutionFailure {
		try {
			FindMany findMany = (FindMany) getInstruction();
			
			String replacement = getReplacement();
			return getPattern().allMatches(
					getSource().getValue(),
					replacement,
					findMany.getMinMatch(),
					findMany.getMaxMatch());
			
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
		FindMany findMany = (FindMany) getInstruction();
		Scraper[] scrapers = findMany.getScrapers();
		Page[] pages = findMany.getPages();
		
		Vector children = new Vector();
		
		for(int i = 0 ; i < results.length ; i ++) {
			Result source = results[i];
			for(int j = 0 ; j < scrapers.length ; j++) {
				children.add(new SpawnedScraperExecutable(getInterfaces(), scrapers[j], this, source));
			}
			for(int j = 0 ; j < pages.length ; j++) {
				children.add(new PageExecutable(getInterfaces(), pages[j], this, source));
			}
		}
		Executable[] childrenAry = new ScraperExecutable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
