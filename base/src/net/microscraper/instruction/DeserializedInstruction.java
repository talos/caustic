package net.microscraper.instruction;

import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.StringMap;

public class DeserializedInstruction implements Instruction {
			
	/**
	 * The {@link StringTemplate} name for this {@link Instruction}.
	 */
	private StringTemplate name;
	
	/**
	 * A {@link Vector} of {@link Instruction}s
	 * dependent upon this {@link Instruction}.
	 */
	private final Vector childInstructions = new Vector();
	
	/**
	 * The {@link Load} done by this {@link Instruction},
	 * if it is a .
	 */
	private final Load load;
	
	private final Find find;
	
	private Scraper[] getChildren(String name, String source, StringMap input) {
		input.put(name, source);
		Scraper[] children = new Scraper[childInstructions.size()];
		for(int i = 0 ; i < children.length ; i ++) {
			children[i] = new Scraper((Instruction) childInstructions.elementAt(i), input, source);
		}
		return children;
	}
	
	private Scraper[] getChildren(String name, String[] sources, StringMap input) {
		if(sources.length == 1) {
			return getChildren(name, sources[0], input);
		}
		Scraper[] children = new Scraper[sources.length * childInstructions.size()];
		for(int i = 0 ; i < sources.length ; i ++ ) {
			final String source = sources[i];
			final StringMap spawnedInput = input.spawnChild();
			spawnedInput.put(name, source);
						
			// Generate children from both promises and real instructions.
			int childNum = i * childInstructions.size();
			Enumeration e = childInstructions.elements();
			while(e.hasMoreElements()) {
				Instruction instruction = (Instruction) e.nextElement();
				children[childNum] = new Scraper(instruction, spawnedInput, source);
				childNum++;
			}
		}
		return children;
	}
	
	/**
	 * Create a new {@link DeserializedInstruction} that runs a {@link Load}.
	 * @param name 
	 * @param load
	 */
	public DeserializedInstruction(StringTemplate name, Load load) {
		this.load = load;
		this.find = null;
		this.name = name;
	}

	/**
	 * Create a new {@link DeserializedInstruction} that runs a {@link Find}.
	 * @param name 
	 * @param load
	 */
	public DeserializedInstruction(StringTemplate name, Find find) {
		this.load = null;
		this.find = find;
		this.name = name;
	}

	/**
	 * Add an {@link Instruction} that will be used to create {@link Executable}s.
	 * @param child The {@link Instruction} to add.
	 */
	public void addChild(Instruction child) {
		childInstructions.add(child);
	}
	
	/**
	 * @return The raw {@link StringTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	public ScraperResult execute(String source, StringMap input) throws InterruptedException {
		final ScraperResult result;
		final String nameStr;
		StringSubstitution nameSub = name.sub(input);
		// Didn't get the name.
		if(nameSub.isMissingTags()) {
			result = ScraperResult.missingTags(nameSub.getMissingTags());
		} else {
			nameStr = nameSub.getSubstituted();
			if(load != null) {
				LoadResult loadResult = load.execute(input);
				if(loadResult.isSuccess()) {
					String responseBody = loadResult.getResponseBody();
					Scraper[] children = getChildren(nameStr, responseBody, input);
					result = ScraperResult.success(nameStr, new String[] { responseBody }, children);
				} else if(loadResult.isMissingTags()) {
					result = ScraperResult.missingTags(loadResult.getMissingTags());					
				} else {
					result = ScraperResult.failure(loadResult.getFailedBecause());
				}
			} else {
				FindResult findResult = find.execute(source, input);
				if(findResult.isSuccess()) {
					String[] matches = findResult.getMatches();
					Scraper[] children = getChildren(nameStr, matches, input);
					result = ScraperResult.success(nameStr, matches, children);
				} else if(findResult.isMissingTags()) {
					result = ScraperResult.missingTags(findResult.getMissingTags());					
				} else {
					result = ScraperResult.failure(findResult.getFailedBecause());
				}
			}
		}
		return result;
	}
}
