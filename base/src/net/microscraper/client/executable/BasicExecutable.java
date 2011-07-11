package net.microscraper.client.executable;

import java.io.IOException;

import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.Resource;

/**
 * {@link BasicExecutable} is a partial implementation of {@link Executable}.  It provides a framework
 * for implementing all of its interfaces except {@link Executable#hasName()},
 * {@link Executable#getName()}, {@link Executable#hasValue()},
 * and {@link Executable#getValue()}.
 * <p>Subclasses must provide implementations of {@link #generateResource}, {@link #generateResult},
 * and {@link #generateChildren}.
 * @author john
 *
 */
public abstract class BasicExecutable implements Executable {
	private final Resource resource;
	private final Variables variables;
	private final Result source;
	private final Interfaces context;
	
	private final static int SLEEP_TIME = 1000; //TODO this belongs elsewhere
	
	//private Resource resource = null;
	private Result[] results = null;
	private Executable[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	/**
	 * Construct a new {@link BasicExecutable}.
	 * @param context The {@link Interfaces} to use.
	 * @param resource The {@link Resource} with instructions for execution.
	 * @param variables The {@link Variables} to use when substituting for tags.
	 * @param source The {@link Result} which is the source of this {@link Executable}.  Can
	 * be <code>null</code> if there was none.
	 * @see #run
	 */
	protected BasicExecutable(Interfaces context, Resource resource,
			Variables variables, Result source) {
		this.context = context;
		this.variables = variables;
		this.resource = resource;
		this.source = source;
	}
	
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				// Only generate the result if we don't have one, and we have a resource.
				if(results == null) {
					results = generateResults();
				}
				if(results != null) {
					children = generateChildren(results);
					handleComplete(); 
				}
			} catch(ExecutionFailure e) {
				handleFailure(e);
			} catch(MustacheTemplateException e) {
				handleFailure(new ExecutionFailure(e));
			} catch (BrowserDelayException e) {
				handleDelay(e);
			} catch(MissingVariableException e) {
				handleMissingVariable(e);
			} catch(IOException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(DeserializationException e) {
				handleFailure(new ExecutionFailure(e));
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
	 * Catch-all failures.  Sets the state of the {@link Executable} to failed.
	 * @param e The {@link ExecutionFailure}.
	 */
	private void handleFailure(ExecutionFailure e) {
		failure = e.getCause();
		context.log.i("Failure in " + toString());
		context.log.e(e);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link BasicExecutable} to 'stuck'.
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
	}
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.
	 * @return An array of {@link Result}s from executing this particular {@link Executable}.  Will be passed to
	 * {@link generateChildren}
	 * @throws BrowserDelayException If a {@link Browser} must wait before having this {@link Executable}
	 * generate a result.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws ExecutionFailure If there is some other exception that prevents this {@link Executable} from
	 * running successfully.  Use {@link ExecutionFailure#getCause} to determine why.
	 * @see #generateResource
	 * @see #generateChildren
	 */
	protected abstract Result[] generateResults() throws
			BrowserDelayException, MissingVariableException, MustacheTemplateException,
			ExecutionFailure;
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.  Should return 0-length array if there are
	 * no children.
	 * @param results The {@link Result} array from {@link #generateResult}.
	 * @return An array of {@link Execution[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws IOException If there was an error loading the {@link Resource} for one of the children.
	 * @throws DeserializationException If there was an error deserializing the {@link Resource} for one
	 * of the children.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	protected abstract Executable[] generateChildren(Result[] results)
			throws MissingVariableException, MustacheTemplateException, DeserializationException, IOException;
	
	public final Resource getResource() {
		return resource;
	}
	
	public final Variables getVariables() {
		return variables;
	}
	
	public final boolean hasSource() {
		if(source != null) {
			return true;
		}
		return false;
	}
	
	public final Result getSource() {
		if(hasSource()) {
			return source;
		} else {
			throw new NullPointerException();
		}
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has escaped its {@link GenerateResource}, {@link generateResult},
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
	 * @return A String identifying this {@link BasicExecutable} in the following form:
	 * <p><code>Execution {@link #getId()} {@link #getResourceLocation()}.toString()
	 */
	public final String toString() {
		return "Execution " + getResource().location.toString();
	}
	
	public final Executable[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * 
	 * @return The {@link Interfaces} that provides access to interfaces during execution.
	 */
	public final Interfaces getContext() {
		return context;
	}
	
	public Result[] getResults() throws IllegalStateException {
		if(isComplete()) {
			return results;
		}
		throw new IllegalStateException();
	}
	
	/**
	 * Convenience method to generate a {@link Result} for an {@link Executable}.
	 * @param name The name to attach to the {@link Result}.  Can be <code>null</code>.
	 * @param value The value to attach to the {@link Result}.  Cannot be <code>null</code>.
	 * @return A {@link Result} from this {@link Executable}.
	 * @throws NullPointerException If <b>value</b> is <code>null</code>.
	 */
	protected Result generateResult(String name, String value) {
		if(value == null)
			throw new NullPointerException("Result value cannot be null");
		return new BasicResult(this, name, value);
	}
}
