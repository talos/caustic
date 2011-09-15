package net.microscraper.instruction;

import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.deserializer.Deserializer;
import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.http.HttpBrowser;

public class Instruction {
	
	private Find find;
	
	private Load load;
	
	private String serializedString;
	private Deserializer deserializer;
	private String uri;
	
	private Instruction[] children = new Instruction[] { };
	
	public Instruction(Find find) {
		this.find = find;
	}
	
	public Instruction(Load load) {
		this.load = load;
	}
	
	public Instruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
	}
	
	public void setChildren(Instruction[] children) {
		this.children = children;
	}
	
	/**
	 * 
	 * @param source The {@link String} to use as the source
	 * for this {@link Instruction}.
	 * @param view The {@link DatabaseView} to use as input
	 * for template substitutions.
	 * @return A {@link ScraperResult} object with either successful
	 * values and children, or information about why
	 * this method did not work.
	 * @throws InterruptedException if the user interrupted during
	 * the method.
	 * @throws DatabaseException if there was an error persisting to 
	 * or reading from <code>view</code>.
	 */
	public InstructionResult execute(String source, DatabaseView view, HttpBrowser browser)
			throws InterruptedException, DatabaseException {
		
		if(deserializer != null && (find == null && load == null)) {
			DeserializerResult deserializerResult = deserializer.deserialize(serializedString, view, uri);
		
			if(deserializerResult.isMissingTags()) {
				return InstructionResult.missingTags(deserializerResult.getMissingTags());
			} else if(deserializerResult.getFind() != null) {
				find = deserializerResult.getFind();
				children = deserializerResult.getChildren();
			} else if(deserializerResult.getLoad() != null) {
				load = deserializerResult.getLoad();
				children = deserializerResult.getChildren();
			} else {
				return InstructionResult.failed(deserializerResult);
			}
		}
		
		if(find != null) {
			final FindResult findResult = find.execute(source, view);
			if(findResult.isMissingTags()) {
				return InstructionResult.missingTags(findResult.getMissingTags());
			} else if(findResult.getMatches() != null) {		
				return InstructionResult.success(
						findResult.getName(),
						findResult.getMatches(),
						children,
						findResult.shouldStoreValues());
			} else {
				return InstructionResult.failed(findResult);
			}
		}
		
		if(load != null) {
			LoadResult loadResult = load.execute(browser, view);
			
			if(loadResult.isMissingTags()) {
				return InstructionResult.missingTags(loadResult.getMissingTags());
			} else if(loadResult.getResponseBody() != null) {
				return InstructionResult.success(
						loadResult.getUrl(),
						new String[] { loadResult.getResponseBody() },
						children,
						false // Load results are big -- don't store them.
						);
				
			} else {
				return InstructionResult.failed(loadResult);
			}
		}
		
		throw new RuntimeException();
	}
}
