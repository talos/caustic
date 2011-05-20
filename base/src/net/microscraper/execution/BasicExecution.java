package net.microscraper.execution;

import java.io.IOException;
import java.net.URI;

import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Utils;
import net.microscraper.model.DeserializationException;

public abstract class BasicExecution implements Execution {
	private final URI resourceLocation;
	private final int id;
	private final Execution caller;
	private final Context context;
	
	private final static int SLEEP_TIME = 1000;
	
	private Exception failure = null;
	private String lastMissingVariable = null;
	private String missingVariable = null;
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	private static int count = 0;
	
	public BasicExecution(Context context, URI resourceLocation, Execution caller) {
		id = count;
		count++;
		
		this.context = context;
		this.resourceLocation = resourceLocation;
		this.caller = caller;
	}
	
	public BasicExecution(Context context, URI resourceLocation) {
		id = count;
		count++;

		this.context = context;
		this.resourceLocation = resourceLocation;
		this.caller = null;
	}
	
	public final void run() {
		isStuck = false;
		try {
			isComplete = protectedRun();
		} catch(NoMatchesException e) {
			handleFailure(e);
		} catch(MissingGroupException e) {
			handleFailure(e);
		} catch(InvalidRangeException e) {
			handleFailure(e);
		} catch(MustacheTemplateException e) {
			handleFailure(e);
		} catch(MissingVariableException e) {
			handleMissingVariable(e);
		} catch (IOException e) {
			handleFailure(e);
		} catch (DeserializationException e) {
			handleFailure(e);
		} catch (BrowserDelayException e) {
			handleDelay(e);
		} catch (BrowserException e) {
			handleFailure(e);
		} catch (InvalidBodyMethodException e) {
			handleFailure(e);
		} catch (ScraperSourceException e) {
			handleFailure(e);
		}
		if(isComplete) {
			handleComplete();
		}
	}
	
	private void handleDelay(BrowserDelayException e) {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch(InterruptedException interrupt) {
			context.e(interrupt);
		}
		context.i("Delaying load of " + Utils.quote(e.url.toString()) +
				", current KBPS " +
				Utils.quote(Float.toString(e.kbpsSinceLastLoad)));
	}
	
	private void handleFailure(Exception e) {
		failure = e;
		context.i("Failure in " + toString());
		context.e(e);
	}
	
	private void handleMissingVariable(MissingVariableException e) {
		context.i("Missing " + Utils.quote(e.name) + " from " + toString());
		if(missingVariable != null) {
			lastMissingVariable = new String(missingVariable);
			missingVariable = e.name;
			if(lastMissingVariable.equals(missingVariable)) {
				isStuck = true;
				context.i("Stuck on " + Utils.quote(missingVariable) + " in " + toString());
			}
		} else {
			missingVariable = e.name;
		}
	}
	
	private void handleComplete() {
		String publishName = hasPublishName() ? getPublishName() : "";
		String publishValue = hasPublishValue() ? getPublishValue() : "";
		//context.i(toString() + " completed successfully, with '" + publishName + "'='" + publishValue + "'");
	}
	
	/*
	 * returns whether isComplete
	 */
	protected abstract boolean protectedRun() throws NoMatchesException, MissingGroupException,
			InvalidRangeException, MustacheTemplateException, MissingVariableException, IOException,
			DeserializationException, BrowserDelayException, BrowserException, InvalidBodyMethodException, ScraperSourceException;

	public final int getId() {
		return id;
	}

	public final URI getResourceLocation() {
		return resourceLocation;
	}

	public final boolean hasCaller() {
		if(caller != null)
			return true;
		return false;
	}

	public final Execution getCaller() {
		if(hasCaller())
			return caller;
		throw new NullPointerException();
	}
	
	public final boolean isStuck() {
		return isStuck;
	}
	
	public final String stuckOn() {
		if(isStuck())
			return missingVariable;
		throw new NullPointerException();
	}

	public final boolean hasFailed() {
		if(failure != null)
			return true;
		return false;
	}
	
	public final Exception failedBecause() {
		if(failure != null)
			return failure;
		throw new NullPointerException();
	}

	public final boolean isComplete() {
		return isComplete;
	}
	
	public final String toString() {
		return "Execution " + Integer.toString(getId()) + " " + resourceLocation.toString();
	}
}
