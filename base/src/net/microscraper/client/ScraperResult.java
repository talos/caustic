package net.microscraper.client;

import net.microscraper.util.Result;

public class ScraperResult extends Result {
	private String name;
	private String[] values;
	private Scraper[] children;
	private ScraperResult(String name, String[] values, Scraper[] children) {
		super((Object) null);
		this.name = name;
		this.values = values;
		this.children = children;
	}
	
	private ScraperResult(String[] missingTags) {
		super(missingTags);
	}
	
	private ScraperResult(String failedBecause) {
		super(failedBecause);
	}
	
	public String getName() {
		getSuccess();
		return name;
	}
	
	public String[] getValues() {
		getSuccess();
		return values;
	}
	
	public Scraper[] getChildren() {
		getSuccess();
		return children;
	}
	
	public static ScraperResult success(String name, String[] values, Scraper[] children) {
		return new ScraperResult(name, values, children);
	}
	
	public static ScraperResult missingTags(String[] missingTags) {
		return new ScraperResult(missingTags);
	}
	
	public static ScraperResult failure(String failedBecause) {
		return new ScraperResult(failedBecause);
	}
}
