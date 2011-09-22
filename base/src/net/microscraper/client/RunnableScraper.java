package net.microscraper.client;

import net.microscraper.database.DatabaseException;
import net.microscraper.instruction.InstructionResult;

public class RunnableScraper implements Runnable {
	private final Scraper scraper;
	private DatabaseException databaseException;
	private InterruptedException interruptedException;
	private InstructionResult[] results;
	
	public RunnableScraper(Scraper scraper) {
		this.scraper = scraper;
	}
	
	public void run() {
		try {
			results = scraper.scrape();
		} catch(InterruptedException e) {
			interruptedException = e;
		} catch(DatabaseException e) {
			databaseException = e;
		}
	}
	
	public boolean isSuccess() {
		return results != null;
	}
	
	public InstructionResult[] getResults() {
		return results;
	}
	
	public DatabaseException getDatabaseException() {
		return databaseException;
	}
	
	public InterruptedException getInterruptedException() {
		return interruptedException;
	}
}
