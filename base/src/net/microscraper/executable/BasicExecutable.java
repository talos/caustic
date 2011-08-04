package net.microscraper.executable;

import java.io.IOException;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Utils;
import net.microscraper.Variables;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Instruction;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;

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
	private final Instruction instruction;
	private final Result source;
	private final Interfaces interfaces;
	
	private Result[] results = null;
	private Executable[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	
	/**
	 * Construct a new {@link BasicExecutable}.
	 * @param context The {@link Interfaces} to use.
	 * @param instruction The {@link Instruction} with instructions for execution.
	 * @param source The {@link Result} which is the source of this {@link Executable}.  Can
	 * be <code>null</code> if there was none.
	 * @see #run
	 */
	protected BasicExecutable(Interfaces context, Instruction instruction,
			Result source) {
		this.interfaces = context;
		this.instruction = instruction;
		this.source = source;
	}
	
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				// Only generate the result if we don't have one, and we have a resource.
				if(results == null) {
					String[] resultValues = generateResultValues();
					results = new Result[resultValues.length];
					for(int i = 0 ; i < resultValues.length ; i ++) {
						if(hasSource()) {
							results[i] = interfaces.getDatabase().store(getSource(),
									getName(), resultValues[i]);
						} else {
							results[i] = interfaces.getDatabase().store(
									getName(), resultValues[i]);							
						}
					}
				}
				if(results != null) {
					children = generateChildren(results);
				}
			} catch(ExecutionFailure e) {
				handleFailure(e);
			} catch(MustacheTemplateException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(MissingVariableException e) {
				handleMissingVariable(e);
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
		interfaces.getLog().i("Failure in " + toString());
		interfaces.getLog().e(failure);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link BasicExecutable} to 'stuck'.
	 * @param e The {@link MissingVariableException}.
	 */
	private void handleMissingVariable(MissingVariableException e) {
		interfaces.getLog().i("Missing " + Utils.quote(e.name) + " from " + toString());
		if(missingVariable != null) {
			lastMissingVariable = new String(missingVariable);
			missingVariable = e.name;
			if(lastMissingVariable.equals(missingVariable)) {
				isStuck = true;
				interfaces.getLog().i("Stuck on " + Utils.quote(missingVariable) + " in " + toString());
			}
		} else {
			missingVariable = e.name;
		}
	}
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.
	 * @return An array of {@link String}s from executing this particular {@link Executable}.  Will be passed to
	 * {@link generateChildren}.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws ExecutionFailure If there is some other exception that prevents this {@link Executable} from
	 * running successfully.  Use {@link ExecutionFailure#getCause} to determine why.
	 * @see #generateResource
	 * @see #generateChildren
	 */
	protected abstract String[] generateResultValues() throws
			MissingVariableException, MustacheTemplateException, ExecutionFailure;
	
	/**
	 * Must be overriden by {@link BasicExecutable} subclass.  Should return 0-length array if there are
	 * no children.
	 * @param results The {@link Result} array from {@link #generateResult}.
	 * @return An array of {@link Execution[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws IOException If there was an error loading the {@link Instruction} for one of the children.
	 * @throws DeserializationException If there was an error deserializing the {@link Instruction} for one
	 * of the children.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	protected abstract Executable[] generateChildren(Result[] results)
			throws MissingVariableException, MustacheTemplateException, DeserializationException, IOException;
	
	public final Instruction getInstruction() {
		return instruction;
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
		if(results == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @return A String identifying this {@link BasicExecutable} in the following form:
	 * <p><code>Execution {@link #getId()} {@link #getResourceLocation()}.toString()
	 */
	public final String toString() {
		return "Execution " + getInstruction().getLocation();
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
	 * @return The {@link Interfaces} for this {@link BasicExecutable}.
	 */
	public final Interfaces getInterfaces() {
		return interfaces;
	}
	
	public Result[] getResults() throws IllegalStateException {
		if(isComplete()) {
			return results;
		}
		throw new IllegalStateException();
	}
	
	/**
	 * 
	 * @return The {@link Instruction#getName()}, compiled through
	 * {@link Mustache}, if {@link Instruction#hasName()} is <code>true</code>.
	 * Returns the {@link Instruction#getLocation()} as a {@link String} otherwise.
	 * @throws MustacheTemplateException If {@link Instruction#getName()} is an invalid {@link MustacheTemplate}.
	 * @throws MissingVariableException If the {@link Instruction#getName()}
	 * cannot be compiled with {@link #getVariables()}.
	 */
	public String getName() throws MissingVariableException,
			MustacheTemplateException {
		if(getInstruction().hasName()) {
			return getInstruction().getName().compile(this);
		} else {
			return getInstruction().getLocation().toString();
		}
	}
}
