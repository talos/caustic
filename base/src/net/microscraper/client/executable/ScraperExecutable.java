package net.microscraper.client.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.resource.FindMany;
import net.microscraper.server.resource.FindOne;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Scraper;

public abstract class ScraperExecutable extends BasicExecutable implements Variables {
	private FindOneExecutable[] findOneExecutables;

	protected ScraperExecutable(Interfaces context, Scraper scraper,
			Variables variables, Result source) {
		super(context, scraper, variables, source);
	}
	
	public String get(String key) throws MissingVariableException {
		if(isComplete()) {
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				if(findOneExecutables[i].containsKey(key)) {
					return findOneExecutables[i].get(key);
				}
			}
		}
		if(getVariables().containsKey(key)) {
			return getVariables().get(key);
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
	
	protected final Executable[] generateChildren(Result[] sourceResults) throws DeserializationException, IOException {
		Vector children = new Vector();
		Vector findOneExecutables = new Vector();
		Scraper scraper = (Scraper) getResource();
		FindOne[] findOnes = scraper.getFindOnes();
		FindMany[] findManys = scraper.getFindManys();
		Scraper[] scrapers = scraper.getScrapers();
		Page[] pages = scraper.getPages();
		
		for(int i = 0; i < sourceResults.length ; i++) {
			Result sourceResult = sourceResults[i];
			for(int j = 0 ; j < findOnes.length ; j ++) {
				FindOneExecutable variableExecution = new FindOneExecutable(getContext(),
						findOnes[j], getVariables(), sourceResult);
				findOneExecutables.add(variableExecution);
				children.add(variableExecution);
			}
			for(int j = 0 ; j < findManys.length ; j ++) {
				children.add(new FindManyExecutable(getContext(), findManys[j],
						getVariables(), sourceResult));
			}
			for(int j = 0 ; j < pages.length ; j ++) {
				children.add(new PageExecutable(getContext(), pages[j],
						getVariables(), sourceResult));
			}
			for(int j = 0 ; j < scrapers.length ; j ++) {
				children.add(new SpawnedScraperExecutable(getContext(), scrapers[j],
						getVariables(), sourceResult));
			}
		}
		this.findOneExecutables = new FindOneExecutable[findOneExecutables.size()];
		findOneExecutables.copyInto(this.findOneExecutables);
		
		Executable[] childrenAry = new Executable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
	
}
