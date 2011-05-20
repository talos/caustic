package net.microscraper.execution;

import java.io.IOException;
import java.net.URI;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;

public abstract class BasicExecution implements Execution {
	private final URI resourceLocation;
	private final int id;
	private final Execution caller;
	private final Context context;
	
	private Exception failure = null;
	private String lastMissingVariable = null;
	private String missingVariable = null;
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
		} catch (DelayRequest e) {
			
		} catch (BrowserException e) {
			handleFailure(e);
		} catch (InvalidBodyMethodException e) {
			handleFailure(e);
		}
	}
	
	private void handleFailure(Exception e) {
		failure = e;
		context.e(e);
	}
	
	private void handleMissingVariable(MissingVariableException e) {
		lastMissingVariable = missingVariable;
		missingVariable = e.name;
		context.i("Missing " + missingVariable);
	}
	
	/*
	 * returns whether isComplete
	 */
	protected abstract boolean protectedRun() throws NoMatchesException, MissingGroupException,
			InvalidRangeException, MustacheTemplateException, MissingVariableException, IOException,
			DeserializationException, DelayRequest, BrowserException, InvalidBodyMethodException;

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
/*
	public Execution[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}
*/
	public final boolean isStuck() {
		if(!isComplete() && lastMissingVariable != null && missingVariable != null) {
			if(lastMissingVariable.equals(missingVariable))
				return true;
		}
		return false;
	}

	public final boolean hasFailed() {
		if(failure != null)
			return true;
		return false;
	}

	public final boolean isComplete() {
		return isComplete;
	}
	
/*
	public boolean hasPublishName() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getPublishName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasPublishValue() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getPublishValue() {
		// TODO Auto-generated method stub
		return null;
	}*/
}
