package net.microscraper.client.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.FindMany;
import net.microscraper.server.instruction.FindOne;
import net.microscraper.server.instruction.Page;
import net.microscraper.server.instruction.Scraper;

/**
 * @see {@link SpawnedScraperExecutable}, {@link PageExecutable}, {@link Variables}, {@link BasicExecutable}
 * @author talos
 *
 */
public abstract class ScraperExecutable extends BasicExecutable {
	private FindOneExecutable[] findOneExecutables;
	private final Variables extendedVariables;

	protected ScraperExecutable(Interfaces context, Scraper scraper,
			Variables extendedVariables, Result source) {
		super(context, scraper, source);
		this.extendedVariables = extendedVariables;
	}
	
	public final String get(String key) throws MissingVariableException {
		if(isComplete()) {
			//Executable[] children = getChildren();
			for(int i = 0 ; i < findOneExecutables.length ; i ++) {
				String localValue = findOneExecutables[i].localGet(key);
				if(localValue != null) {
					return localValue;
				}
			}
		}
		if(hasSource()) {
			if(getSource().hasName()) {
				if(getSource().getName().equals(key)) {
					return getSource().getValue();
				}
			}
		}
		return extendedVariables.get(key);
	}
	
	public final boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
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
				FindOneExecutable variableExecution = new FindOneExecutable(getInterfaces(),
						findOnes[j], this, sourceResult);
				findOneExecutables.add(variableExecution);
				children.add(variableExecution);
			}
			for(int j = 0 ; j < findManys.length ; j ++) {
				children.add(new FindManyExecutable(getInterfaces(), findManys[j],
						this, sourceResult));
			}
			for(int j = 0 ; j < pages.length ; j ++) {
				children.add(new PageExecutable(getInterfaces(), pages[j],
						this, sourceResult));
			}
			for(int j = 0 ; j < scrapers.length ; j ++) {
				children.add(new SpawnedScraperExecutable(getInterfaces(), scrapers[j],
						this, sourceResult));
			}
		}
		this.findOneExecutables = new FindOneExecutable[findOneExecutables.size()];
		findOneExecutables.copyInto(this.findOneExecutables);
		
		Executable[] childrenAry = new Executable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}
}
