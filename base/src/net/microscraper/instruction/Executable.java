package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.impl.log.Log;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;

/**
 * {@link Executable}s provide a way to retry individual {@link Instruction#execute}
 * runs.
 * @author john
 *
 */
public final class Executable extends Log implements Variables {
	private final Instruction instruction;
	private final Result source;
	private final Browser browser;
	private final RegexpCompiler compiler;
	private final Database database;
	private final Variables variables;
	
	private Result[] results = null;
	
	private Executable[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	/**
	 * Construct a new {@link Executable}.
	 * @param instruction The {@link Instruction} with instructions for execution.
	 * @param compiler the {@link RegexpCompiler} to use.
	 * @param browser the {@link Browser} to use.
	 * @param variables the {@link Variables} surrounding this {@link Executable}.
	 * @param source The {@link Result} which is the source of this {@link Executable}.  Can
	 * be <code>null</code> if there was none.
	 * @param database The {@link Database} to use when storing {@link Result}s.
	 * @see #run
	 */
	public Executable(Instruction instruction, RegexpCompiler compiler,
			Browser browser, Variables variables, Result source, Database database) {
		this.instruction = instruction;
		this.compiler = compiler;
		this.browser = browser;
		this.variables = variables;
		this.source = source;
		this.database = database;
	}

	/**
	 * 
	 * Run the {@link Executable}.
	 * @see #isComplete()
	 * @see #isStuck()
	 * @see #hasFailed()
	 */
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				// Only generate the result if we don't have one, and we have a resource.
				if(results == null) {
					results = instruction.execute(compiler, browser, this, source, database);
				}
				if(results != null) {
					children = instruction.generateChildren(compiler, browser, this, results, database);
					isComplete = true;
				}
			} catch(RegexpException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(MissingVariableException e) {
				handleMissingVariable(e);
			} catch(BrowserException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(IOException e) {
				handleFailure(new ExecutionFailure(e));				
			} catch(DeserializationException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(DatabaseException e) {
				handleFailure(new ExecutionFailure(e));
			}
		}
	}
	
	/**
	 * Catch-all failures.  Sets the state of the {@link Executable} to failed.
	 * @param e The {@link ExecutionFailure}.
	 */
	private void handleFailure(ExecutionFailure e) {
		failure = e.getCause();
		i("Failure in " + toString());
		e(failure);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link Executable} to 'stuck'.
	 * @param e The {@link MissingVariableException}.
	 */
	private void handleMissingVariable(MissingVariableException e) {
		i("Missing " + StringUtils.quote(e.name) + " from " + toString());
		if(missingVariable != null) {
			lastMissingVariable = new String(missingVariable);
			missingVariable = e.name;
			if(lastMissingVariable.equals(missingVariable)) {
				isStuck = true;
				i("Stuck on " + StringUtils.quote(missingVariable) + " in " + toString());
			}
		} else {
			missingVariable = e.name;
		}
	}

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has not {@link #run} successfully, and will not do so
	 * without an update to its {@link Variables}, <code>false</code> otherwise.
	 * @see #run()
	 * @see #stuckOn()
	 */
	public final boolean isStuck() {
		return isStuck;
	}

	/**
	 * 
	 * @return The name of the {@link MissingVariable} that is stopping this {@link Executable} 
	 * from completing its {@link #run}, if the {@link #isStuck} is returning <code>true</code>.
	 * @throws IllegalStateException If called when {@link #isStuck} is not <code>true</code>.
	 * @see #isStuck()
	 */
	public final String stuckOn() throws IllegalStateException {
		if(isStuck()) {
			return missingVariable;
		} else {
			throw new IllegalStateException();
		}
	}


	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has failed to {@link #run},
	 * and cannot do so, <code>false</code> otherwise.
	 * @see #run()
	 * @see #failedBecause()
	 */
	public final boolean hasFailed() {
		if(failure != null) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * 
	 * @return The {@link Throwable} that caused the {@link Executable} to fail to {@link #run},
	 * @throws IllegalStateException If {@link #hasFailed} is <code>false</code>.
	 * @see #run()
	 * @see #hasFailed()
	 */
	public final Throwable failedBecause() throws IllegalStateException {
		if(hasFailed()) {
			return failure;
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has {@link #run} successfully, and can have
	 * {@link #getChildren} called upon it, <code>false</code> otherwise.
	 * @see #run()
	 * @see #getChildren()
	 * @see #getResult()
	 */
	public final boolean isComplete() {
		return isComplete;
	}
	
	public final String toString() {
		return instruction.toString();
	}

	
	/**
	 * 
	 * @return An array of fresh {@link Executable}s that this {@link Executable} has created.
	 * @throws IllegalStateException if called before the {@link Executable} {@link #isComplete()}.
	 * @see #run()
	 * @see #isComplete()
	 */
	public final Executable[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Search for <code>key</code> only within this {@link Executable} or its
	 * immediate descendents.
	 * @param key the {@link String} to retrieve.
	 * @return the value mapped to {@link String}, or <code>null</code> if it
	 * is not mapped.
	 */
	private String localGet(String key) {
		if(results != null) {
			if(results.length == 1) {
				if(results[0].getName().equals(key)) {
					return results[0].getValue();
				}
			}
		}
		if(source != null) {
			if(source.getName().equals(key)) {
				return source.getValue();
			}
		}
		if(children != null) {
			for(int i = 0 ; i < children.length ; i++) {
				String localValue = children[i].localGet(key);
				if(localValue != null) {
					return localValue;
				}
			}
		}
		return null;
	}
	
	public String get(String key) throws MissingVariableException {
		String localValue = localGet(key);
		if(localValue != null) {
			return localValue;
		} else {
			return variables.get(key); // search up.
		}
	}

	public boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
	}
}
