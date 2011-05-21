package net.microscraper.execution;

import java.io.IOException;
import java.net.URI;

import com.sun.net.httpserver.Authenticator.Failure;

import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.model.DeserializationException;
import net.microscraper.model.Resource;

/**
 * BasicExecution is a partial implementation of {@link Execution}.  It provides a framework
 * for implementing all of its interfaces except {@link Execution.hasPublishName},
 * {@link Execution.getPublishName}, {@link Execution.hasPublishValue}, and {@link Execution.getPublishValue}.
 * <p>Subclasses must provide implementations of {@link #generateResource}, {@link #generateResult},
 * and {@link #generateChildren}.
 * @author john
 *
 */
public abstract class BasicExecution implements Execution {
	private final URI resourceLocation;
	private final int id;
	private final Execution parent;
	private final ExecutionContext context;
	
	private final static int SLEEP_TIME = 1000; //TODO this belongs elsewhere
	
	private Resource resource = null;
	private Object result = null;
	private Execution[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	private static int count = 0;
	
	/**
	 * Construct a BasicExecution with a parent.
	 * @param context
	 * @param resourceLocation
	 * @param parent
	 */
	protected BasicExecution(ExecutionContext context, URI resourceLocation, Execution parent) {
		id = count;
		count++;
		
		this.context = context;
		this.resourceLocation = resourceLocation;
		this.parent = parent;
	}
	
	/**
	 * Construct a BasicExecution without a parent.
	 * @param context
	 * @param resourceLocation
	 */
	protected BasicExecution(ExecutionContext context, URI resourceLocation) {
		id = count;
		count++;

		this.context = context;
		this.resourceLocation = resourceLocation;
		this.parent = null;
	}
	
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				
				// Only generate the resource if we don't have one.
				if(resource == null) {
					try {
						resource = generateResource(context);
					} catch (IOException e) {
						throw new ExecutionFailure(e);
					} catch (DeserializationException e) {
						throw new ExecutionFailure(e);
					}
				}
				
				// Only generate the result if we don't have one, and we have a resource.
				if(result == null && resource != null) {
					try {
						result = generateResult(context, resource);
					} catch(MustacheTemplateException e) {
						throw new ExecutionFailure(e);
					} catch (BrowserDelayException e) {
						handleDelay(e);
					} catch(MissingVariableException e) {
						handleMissingVariable(e);
					}
				}
			} catch(ExecutionFailure e) {
				handleFailure(e);
			}
			
			if(result != null) {
				children = generateChildren(context, resource, result);
				handleComplete();
			}
		}
	}
	
	private void handleDelay(BrowserDelayException e) {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch(InterruptedException interrupt) {
			context.log.e(interrupt);
		}
		context.log.i("Delaying load of " + Utils.quote(e.url.toString()) +
				", current KBPS " +
				Utils.quote(Float.toString(e.kbpsSinceLastLoad)));
	}
	
	/**
	 * Catch-all failures.  Sets the state of the execution to failed.
	 * @param e The {@link ExecutionFailure}.
	 */
	private void handleFailure(ExecutionFailure e) {
		failure = e.getCause();
		context.log.i("Failure in " + toString());
		context.log.e(e);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link BasicExecution} to 'stuck'.
	 * @param e The {@link MissingVariableException}.
	 */
	private void handleMissingVariable(MissingVariableException e) {
		context.log.i("Missing " + Utils.quote(e.name) + " from " + toString());
		if(missingVariable != null) {
			lastMissingVariable = new String(missingVariable);
			missingVariable = e.name;
			if(lastMissingVariable.equals(missingVariable)) {
				isStuck = true;
				context.log.i("Stuck on " + Utils.quote(missingVariable) + " in " + toString());
			}
		} else {
			missingVariable = e.name;
		}
	}
	
	/**
	 * Sets {@link isComplete} to <code>true</code>.
	 */
	private void handleComplete() {
		isComplete = true;
		//String publishName = hasPublishName() ? getPublishName() : "";
		//String publishValue = hasPublishValue() ? getPublishValue() : "";
		//context.i(toString() + " completed successfully, with '" + publishName + "'='" + publishValue + "'");
	}
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
	 * @param context A {@link Context} to use in generating the resource.
	 * @return The {@link Resource} instance that contains instructions for this {@link Execution},
	 * which will be passed to {@link generateResult} and {@link generateChildren}.
	 * @throws IOException If there is an error obtaining the {@link Resource}.
	 * @throws DeserializationException If there is an error deserializing the {@link Resource Resource}.
	 * @see #generateResult
	 * @see #generateChildren
	 */
	protected abstract Resource generateResource(ExecutionContext context)
			throws IOException, DeserializationException;
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
	 * @param context A {@link ExecutionContext} to use in generating the resource.
	 * @param resource The {@link Resource} from {@link #generateResource}.  Should be cast.
	 * @return An Object result for executing this particular {@link Execution}.  Will be passed to
	 * {@link generateChildren}
	 * @throws BrowserDelayException If a {@link Browser} must wait before having this {@link Execution}
	 * generate a result.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Execution}'s {@link Variables}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws ExecutionFailure If there is some other exception that prevents this {@link Execution} from
	 * running successfully.  Use {@link ExecutionFailure#getCause} to determine why.
	 * @see #generateResource
	 * @see #generateChildren
	 */
	protected abstract Object generateResult(ExecutionContext context, Resource resource) throws
			BrowserDelayException, MissingVariableException, MustacheTemplateException,
			ExecutionFailure;
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
	 * @param context A {@link ExecutionContext} to use in generating the resource.
	 * @param resource The {@link Resource} from {@link #generateResource}.  Should be cast.
	 * @param result The Object result from {@link #generateResult}. Should be cast.
	 * @return An array of {@link Execution[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	protected abstract Execution[] generateChildren(ExecutionContext context, Resource resource, Object result);

	public final int getId() {
		return id;
	}
	
	public final URI getResourceLocation() {
		return resourceLocation;
	}
	
	public final boolean hasParent() {
		if(parent != null) {
			return true;
		} else {
			return false;
		}
	}

	public final Execution getParent() throws NullPointerException {
		if(hasParent()) {
			return parent;
		} else {
			throw new NullPointerException();
		}
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has escaped its {@link GenerateResource}, {@link generateResult},
	 * or {@link generateChildren} twice because of the same variable.  <code>False</code> otherwise.
	 * @see #stuckOn()
	 */
	public final boolean isStuck() {
		return isStuck;
	}
	
	/**
	 * @see #isStuck()
	 */
	public final String stuckOn() throws IllegalStateException {
		if(isStuck()) {
			return missingVariable;
		} else {
			throw new IllegalStateException();
		}
	}
	
	public final boolean hasFailed() {
		if(failure != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public final Throwable failedBecause() throws IllegalStateException {
		if(hasFailed()) {
			return failure;
		} else {
			throw new IllegalStateException();
		}
	}

	public final boolean isComplete() {
		return isComplete;
	}
	
	/**
	 * @return A String identifying this {@link BasicExecution} in the following form:
	 * <p><code>Execution {@link #getId()} {@link #getResourceLocation()}.toString()
	 */
	public final String toString() {
		return "Execution " + Integer.toString(getId()) + " " + getResourceLocation().toString();
	}
	
	public final Execution[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
}
