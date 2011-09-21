package net.microscraper.client;

import java.util.Vector;

import net.microscraper.database.DatabaseException;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.util.VectorUtils;

public class DynamicThreadExecutor implements Executor {
	
	public InstructionResult[] scrape(Scraper[] scrapers)
			throws InterruptedException, DatabaseException {
		Thread[] threads = new Thread[scrapers.length];
		RunnableScraper[] runnables = new RunnableScraper[scrapers.length];
		
		// start the scrapers
		for(int i = 0 ; i < scrapers.length ; i ++) {
			runnables[i] = new RunnableScraper(scrapers[i]);
			threads[i] = new Thread(runnables[i]);
			threads[i].start();
		}
		
		// wait for them all to finish
		for(int i = 0 ; i < threads.length ; i ++) {
			try {
				threads[i].join();
			} catch(InterruptedException e) {
				// interrupt everything else before tossing the exception higher up
				for(int j = i+1 ; j < threads.length ; j ++) {
					threads[j].interrupt();
				}
				throw e;
			}
		}
		
		Vector results = new Vector();
		for(int i = 0 ; i < runnables.length ; i ++) {
			RunnableScraper runnable = runnables[i];
			if(runnable.isSuccess()) {
				VectorUtils.arrayIntoVector(runnable.getResults(), results);
			} else {
				if(runnable.getDatabaseException() != null) {
					throw runnable.getDatabaseException();
				}
				if(runnable.getInterruptedException() != null) {
					throw runnable.getInterruptedException();
				}
			}
		}
		
		InstructionResult[] resultsAry = new InstructionResult[results.size()];
		results.copyInto(resultsAry);
		return resultsAry;
	}

}
