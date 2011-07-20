package net.microscraper.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.Variables;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Scraper;

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
		Scraper scraper = (Scraper) getInstruction();
		FindOne[] findOnes = scraper.getFindOnes();
		FindMany[] findManys = scraper.getFindManys();
		Scraper[] scrapers = scraper.getScrapers();
		Page[] pages = scraper.getPages();
		
		for(int i = 0; i < sourceResults.length ; i++) {
			Result sourceResult = sourceResults[i];
			for(int j = 0 ; j < findOnes.length ; j ++) {
				FindOneExecutable findOneExecutable = new FindOneExecutable(getInterfaces(),
						findOnes[j], this, sourceResult);
				findOneExecutables.add(findOneExecutable);
				children.add(findOneExecutable);
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
