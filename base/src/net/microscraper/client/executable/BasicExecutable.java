package net.microscraper.client.executable;

import net.microscraper.client.interfaces.BrowserDelayException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
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
	private final Executable parent;
	private final Interfaces context;
	
	private final static int SLEEP_TIME = 1000; //TODO this belongs elsewhere
	
	//private Resource resource = null;
	private Object result = null;
	private Executable[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	private static int count = 0;
	private final int id;
	
	/**
	 * Construct a BasicExecution with a parent.
	 * @param context
	 * @param resource
	 * @param parent
	 */
	protected BasicExecutable(Interfaces context, Resource resource,
			Variables variables, Executable parent) {
		this.id = count;
		count++;
		
		this.context = context;
		this.variables = variables;
		this.resource = resource;
		this.parent = parent;
	}
	
	/**
	 * Construct a BasicExecution without a parent.
	 * @param context
	 * @param resourceLocation
	 */
	protected BasicExecutable(Interfaces context, Resource resource, Variables variables) {
		this.id = count;
		count++;
		
		this.context = context;
		this.variables = variables;
		this.resource = resource;
		this.parent = null;
	}
	
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				// Only generate the result if we don't have one, and we have a resource.
				if(result == null) {
					result = generateResult();
				}
				if(result != null) {
					children = generateChildren(result);
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
		//String publishName = hasPublishName() ? getPublishName() : "";
		//String publishValue = hasPublishValue() ? getPublishValue() : "";
		//context.i(toString() + " completed successfully, with '" + publishName + "'='" + publishValue + "'");
	}
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.
	 * @return An Object result for executing this particular {@link Executable}.  Will be passed to
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
	protected abstract Object generateResult() throws
			BrowserDelayException, MissingVariableException, MustacheTemplateException,
			ExecutionFailure;
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.  By default returns a 0-length array.
	 * @param context A {@link Interfaces} to use in generating the resource.
	 * @param result The Object result from {@link #generateResult}. Should be cast.
	 * @return An array of {@link Execution[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	protected Executable[] generateChildren(Object result) throws MissingVariableException, MustacheTemplateException {
		return new Executable[0];
	}
	
	public final Resource getResource() {
		return resource;
	}
	
	public final Variables getVariables() {
		return variables;
	}
	
	public final boolean hasParent() {
		if(parent != null) {
			return true;
		} else {
			return false;
		}
	}

	public int getId() {
		return id;
	}
	
	public final Executable getParent() throws NullPointerException {
		if(hasParent()) {
			return parent;
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
	
	public final Object getResult() {
		if(!isComplete()) {
			throw new IllegalStateException();
		} else {
			return result;
		}
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
	
	/**
	 * Defaults to <code>false</code>.
	 */
	public boolean hasName() {
		return false;
	}
	
	/**
	 * Defaults to throwing {@link NullPointerException}.
	 */
	public String getName() {
		throw new NullPointerException();
	}
	
	/**
	 * Defaults to <code>false</code>.
	 */
	public boolean hasValue() {
		return false;
	}
	
	/**
	 * Defaults to throwing {@link NullPointerException}.
	 */
	public String getValue() {
		if(isComplete()) {
			throw new NullPointerException();
		} else {
			throw new IllegalStateException();
		}
	}
}
