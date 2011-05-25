package net.microscraper.client.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.DefaultVariables;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Scraper;
import net.microscraper.server.resource.FindOne;

/**
 * {@link ScraperExecutable} is the {@link Executable} spawned by a {@link Scraper}.
 * When {@link #run}, it spawns a variety of other {@link Executable}s.
 * @author john
 *
 */
public class ScraperExecutable extends BasicExecutable implements Variables {
	//private final UnencodedNameValuePair[] extraVariables;	
	
	private FindOneExecutable[] findOneExecutables;
	
	public ScraperExecutable(Interfaces context, Scraper scraper,
				Variables variables) {
		super(context, scraper, variables);
	}
	
	protected ScraperExecutable(Interfaces context, Executable parent,
				Scraper scraper, Variables variables) {
		super(context, scraper, variables, parent);
	}

	public String get(String key) throws MissingVariableException {
		if(getVariables().containsKey(key)) {
			return getVariables().get(key);
		}
		if(isComplete()) {
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				if(findOneExecutables[i].containsKey(key)) {
					return findOneExecutables[i].get(key);
				}
			}
		}
		throw new MissingVariableException(this, key);
	}
	
	public boolean containsKey(String key) {
		if(getVariables().containsKey(key)) {
			return true;
		}
		if(isComplete()) {
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				if(findOneExecutables[i].containsKey(key)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return The source from which this {@link ScraperExecutable}'s {@link FindExecutable}'s will work.
	 */
	protected Object generateResult()
				throws MissingVariableException, BrowserDelayException,
				ExecutionFailure, MustacheTemplateException {
		Scraper scraper = (Scraper) getResource();
		if(scraper.hasSource) {
			Page sourcePage = (Page) scraper.sourcePage;
			PageExecutable pageExecutable =
				new PageExecutable(getContext(), this, sourcePage, this);
			
			return pageExecutable.generateResult();
		} else {
			throw new ExecutionFailure(new MissingScraperSource());
		}
	}

	/**
	 * @return {@link FindOneExecutable}s, {@link FindManyExecutable}s, and {@link ScraperExecutableChild}s.
	 */
	protected Executable[] generateChildren(Object result) {
		Scraper scraper = (Scraper) getResource();
		String source = (String) result;
		
		FindOne[] findOnes = scraper.getFindOnes();
		FindMany[] findManys = scraper.getFindManys();
		Scraper[] scrapers = scraper.getScrapers();
		
		Vector children = new Vector();
		Vector findOneExecutables = new Vector();
		for(int i = 0 ; i < findOnes.length ; i ++) {
			FindOneExecutable variableExecution = new FindOneExecutable(getContext(),
					this, findOnes[i], getVariables(), source);
			findOneExecutables.add(variableExecution);
			children.add(variableExecution);
		}
		for(int i = 0 ; i < findManys.length ; i ++) {
			children.add(new FindManyExecutable(getContext(), this, findManys[i],
					getVariables(), source));
		}
		for(int i = 0 ; i < scrapers.length ; i ++) {
			children.add(new ScraperExecutableChild(getContext(), this, scrapers[i], getVariables()));
		}
		this.findOneExecutables = new FindOneExecutable[findOneExecutables.size()];
		findOneExecutables.copyInto(this.findOneExecutables);
		
		Executable[] childrenAry = new Executable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
