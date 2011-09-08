package net.microscraper.client;

import java.util.Vector;

import net.microscraper.instruction.Instruction;
import net.microscraper.util.StringMap;
import net.microscraper.util.VectorUtils;

public class Scraper {
	
	private final Instruction instruction;
	private final StringMap input;
	private final String source;
	
	private ScraperResult curResult;
	private ScraperResult lastResult;
	
	/**
	 * 
	 * @param promise
	 * @param scope
	 * @param source
	 */
	public Scraper(Instruction instruction, StringMap input, String source)  {
		this.instruction = instruction;
		this.input = input;
		this.source = source;
	}
	/**
	 * 
	 * @return An array of the {@link Execution}s this {@link Scraper} generated
	 * while running.
	 * @throws InterruptedException If {@link Scraper} is interrupted.
	 */
	public ScraperResult scrape() throws InterruptedException {
		lastResult = curResult;
		curResult = instruction.execute(source, input);
		return curResult;
	}
	
	public boolean isStuck() {
		if(curResult != null && lastResult != null) {
			if(curResult.isMissingTags() && lastResult.isMissingTags()) {
				String[] curMissingTags = curResult.getMissingTags();
				String[] lastMissingTags = lastResult.getMissingTags();
				if(curMissingTags.length != lastMissingTags.length) {
					Vector curVector = new Vector();
					Vector lastVector = new Vector();
					VectorUtils.arrayIntoVector(curMissingTags, curVector);
					VectorUtils.arrayIntoVector(lastMissingTags, lastVector);
					return VectorUtils.haveSameElements(curVector, lastVector);
				}
			}
		}
		return false;
	}
}
