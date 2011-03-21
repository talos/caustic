package net.microscraper.client;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Collector;
import net.microscraper.client.Gatherer;
import net.microscraper.client.Information;
import net.microscraper.client.LogInterface;
import net.microscraper.client.Gatherer.InsufficientInformationException;


public class SyncCollector implements Collector {
	private final boolean preview;
	private final LogInterface logger;
	private final Timer timer;
	private final int sleepMilliseconds;
	private final int longSleepMilliseconds;
	public SyncCollector(boolean p, LogInterface l, Timer t, int sleepMilli, int longSleepMilli) {
		preview = p;
		logger = l;
		timer = t;
		sleepMilliseconds = sleepMilli;
		longSleepMilliseconds = longSleepMilli;
	}
	@Override
	public void collect(Information information) throws InterruptedException {
		@SuppressWarnings("unchecked")
		Vector<Gatherer> gatherers = information.gatherers;
		
		int attemptsSinceSuccess = 0;
		int gatherersFinished = 0;
		while(gatherers.size() > 0) {
			Gatherer g = (Gatherer) gatherers.firstElement();
			gatherers.removeElementAt(0);
			try {
				
				if (Thread.interrupted()) {
			        throw new InterruptedException();
				}
				
				g.execute(information, preview);
				
				if (Thread.interrupted()) {
			        throw new InterruptedException();
				}
				
				gatherersFinished ++;
				information.publishProgress(gatherersFinished);
				attemptsSinceSuccess = 0;
				
				try { 
					if(timer.isActive() == true) {
						logger.i("Starting to sleep (regular timing).");
						Thread.sleep(sleepMilliseconds);
					} else {
						logger.i("Starting to sleep (extra timing).");
						Thread.sleep(longSleepMilliseconds);
					}
				} catch(InterruptedException e) {}
				
			} catch(InsufficientInformationException e) { // We just have to try again.
				logger.e("Insufficient information to run " + g.id + ", missing " + e.fieldName + " from Information.",e);
				gatherers.addElement(g);
			} catch(IOException e) { // Major error here, we won't be able to collect information from this gatherer.
				logger.e("Major error collecting information.", e);
				gatherersFinished ++;
			}
			attemptsSinceSuccess++;
			if(attemptsSinceSuccess > gatherers.size())
				break;
		}
	}
	
}
