package net.microscraper.execution;

import java.io.IOException;
import java.net.URI;

import com.sun.net.httpserver.Authenticator.Failure;

import net.microscraper.client.BrowserException;
import net.microscraper.client.BrowserDelayException;
import net.microscraper.client.Interfaces.Regexp.InvalidRangeException;
import net.microscraper.client.Interfaces.Regexp.MissingGroupException;
import net.microscraper.client.Interfaces.Regexp.NoMatchesException;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Utils;
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
	private final Context context;
	
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
	public BasicExecution(Context context, URI resourceLocation, Execution parent) {
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
	 * @param parent
	 */
	public BasicExecution(Context context, URI resourceLocation) {
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
						resource = generateResource();
					} catch (IOException e) {
						throw new ExecutionFailure(e);
					} catch (DeserializationException e) {
						throw new ExecutionFailure(e);
					}
				}
				
				// Only generate the result if we don't have one, and we have a resource.
				if(result == null && resource != null) {
					try {
						result = generateResult(resource);
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
				children = generateChildren(resource, result);
				handleComplete();
			}
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
	
	/**
	 * Catch-all failures.  Sets the state of the execution to failed.
	 * @param e
	 */
	private void handleFailure(ExecutionFailure e) {
		failure = e.getCause();
		context.i("Failure in " + toString());
		context.e(e);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link BasicExecution} to 'stuck'.
	 * @param e The {@link MissingVariableException}.
	 */
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
		isComplete = true;
		//String publishName = hasPublishName() ? getPublishName() : "";
		//String publishValue = hasPublishValue() ? getPublishValue() : "";
		//context.i(toString() + " completed successfully, with '" + publishName + "'='" + publishValue + "'");
	}
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
	 * @return The {@link Resource} instance that contains instructions for this {@link Execution},
	 * which will be passed to {@link generateResult} and {@link generateChildren}.
	 * @throws IOException If there is an error obtaining the {@link Resource}.
	 * @throws DeserializationException If there is an error deserializing the {@link Resource Resource}.
	 * @see #generateResult
	 * @see #generateChildren
	 */
	protected abstract Resource generateResource() throws IOException, DeserializationException;
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
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
	protected abstract Object generateResult(Resource resource) throws
			BrowserDelayException, MissingVariableException, MustacheTemplateException,
			ExecutionFailure;
	
	/**
	 * Must be overriden by {@link BasicExecution} subclass.
	 * @param resource The {@link Resource} from {@link #generateResource}.  Should be cast.
	 * @param result The Object result from {@link #generateResult}. Should be cast.
	 * @return An array of {@link Execution[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	protected abstract Execution[] generateChildren(Resource resource, Object result);

	public final int getId() {
		return id;
	}
	
	public final URI getResourceLocation() {
		return resourceLocation;
	}
	
	public final boolean hasParent() {
		if(parent != null)
			return true;
		return false;
	}

	public final Execution getParent() throws NullPointerException {
		if(hasParent())
			return parent;
		throw new NullPointerException();
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Execution} has escaped its {@link GenerateResource}, {@link generateResult},
	 * or {@link generateChildren} twice because of the same variable.  <code>False</code> otherwise.
	 */
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
	
	public final Throwable failedBecause() {
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
	
	public final Execution[] getChildren() {
		return children;
	}
}
