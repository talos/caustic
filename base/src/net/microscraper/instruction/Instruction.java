package net.microscraper.instruction;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.client.Logger;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpException;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;
import net.microscraper.util.VectorUtils;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public abstract class Instruction  {
	
	/**
	 * Key for {@link Find} children when deserializing from JSON.
	 */
	public static final String FIND = "find";

	/**
	 * Key for {@link Page} children when deserializing from JSON.
	 */
	public static final String LOAD = "load";
	
	/**
	 * Key for {@link #name} value when deserializing from JSON.
	 */
	public static final String NAME = "name";
	
	/**
	 * Key for {@link #shouldSaveValue} value when deserializing from JSON.
	 */
	public static final String SAVE = "save";
	
	/**
	 * The {@link MustacheTemplate} name for this {@link Instruction}.  Can be <code>null</code>.
	 * Name should therefore be retrieved through {@link #getName(Variables)}.
	 * @see #getName(Variables, Browser, RegexpCompiler)
	 */
	private final MustacheTemplate name;
	
	/**
	 * Get the name for this {@link Instruction}.  Will return {@link #name}, compiled, unless it is
	 * <code>null</code>. In that case, it will return {@link #getDefaultName(Variables, RegexpCompiler, Browser)}
	 * instead.
	 * @param variables {@link Variables} for Mustache substitution.
	 * @return The {@link String}.
	 * @throws MissingVariableException if a {@link MustacheTemplate} name could not be compiled.
	 * @throws RegexpException
	 */
	//private String getName(Variables variables, Browser browser, RegexpCompiler compiler)
	//		throws MissingVariableException, RegexpException {
	/*private String getName(Variables variables) throws MissingVariableException, RegexpException {
		return name != null ? name.compile(variables) : getDefaultName(variables);
	}*/
	
	/**
	 * The {@link JSONObjectInterface} this {@link Instruction} was deserialized from,
	 * as a formatted {@link String}.
	 */
	//private final String formattedJSON;

	/**
	 * An array of {@link Instruction}s to be turned into {@link Executable}s after
	 * this {@link Execution} is executed.
	 */
	private final Instruction[] children;
	
	private final boolean shouldSaveValue;
	
	/**
	 * 
	 * @return Whether {@link #shouldSaveValue()} should be <code>true</code>
	 * or <code>false</code> by default.
	 * @see #shouldSaveValue
	 */
	//public abstract boolean defaultShouldSaveValue();
	
	/**
	 * The {@link RegexpCompiler} used to compile {@link Pattern}s during the execution of this
	 * {@link Instruction}.
	 */
	private final RegexpCompiler compiler;
	
	/**
	 * The {@link Browser} used to compile {@link Page}s and during the execution of this
	 * {@link Instruction}.
	 */
	private final Browser browser;
	
	/**
	 * The {@link Database} in which this {@link Instruction}'s {@link Result}s will be stored.
	 */
	private final Database database;
	
	/**
	 * The {@link Logger} to this {@link Instruction} sends log messages to.
	 */
	private final Logger log;
	
	/**
	 * 
	 * @param compiler The {@link RegexpCompiler} to use when compiling {@link Pattern}
	 * @param browser The {@link Browser] to use when loading {@link Page}s.
	 * @param database The {@link Database} to save results to.
	 * @param log The {@link Log} to log progress to.  <code>Null</code> to disable logging for this
	 * execution.
	 * @param shouldSaveValue Whether this {@link Instruction}'s results' values should be
	 * saved. <code>True</code> if they should be, <code>false</code> otherwise.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s {@link Result}s. 
	 * @param children An array of {@link Instruction}s to be executed after the execution of
	 * this {@link Instruction}.
	 */
	public Instruction(RegexpCompiler compiler, Browser browser,
			Database database, Logger log, boolean shouldSaveValue,
			MustacheTemplate name, Instruction[] children) {
		this.compiler = compiler;
		this.browser = browser;
		this.database = database;
		this.log = log;
		this.shouldSaveValue = shouldSaveValue;
		this.name = name;
		this.children = children;
	}
	
	public Instruction fromJSON(JSONObjectInterface jsonObject,
			boolean defaultShouldSaveValue, MustacheTemplate defaultName)
				throws DeserializationException, IOException {
		try {			
			if(jsonObject.has(NAME)) {
				name = new MustacheTemplate(jsonObject.getString(NAME));
			} else {
				name = defaultName;
			}
			if(jsonObject.has(SAVE)) {
				shouldSaveValue = jsonObject.getBoolean(SAVE);
			} else {
				shouldSaveValue = defaultShouldSaveValue;
			}
			
			Vector children = new Vector();
			if(jsonObject.has(FIND)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(FIND)) {
					children.add(new Find(jsonObject.getJSONObject(FIND)));
				} else {
					JSONArrayInterface array = jsonObject.getJSONArray(FIND);
					for(int i = 0 ; i < array.length() ; i ++) {
						children.add(new Find(array.getJSONObject(i)));
					}
				}
			}
			
			if(jsonObject.has(LOAD)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(LOAD)) {
					children.add(new Page(jsonObject.getJSONObject(LOAD)));
				} else {
					JSONArrayInterface array = jsonObject.getJSONArray(LOAD);
					for(int i = 0 ; i < array.length() ; i ++) {
						children.add(new Page(array.getJSONObject(i)));
					}
				}
			}
			this.children = new Instruction[children.size()];
			children.copyInto(this.children);
			
			return new Instruction();
		} catch(JSONParserException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheCompilationException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * {@link Instruction} can be initialized with a {@link JSONObjectInterface}, which has a location.
	 * @param jsonObject The {@link JSONObjectInterface} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 * @throws IOException If there is an error loading one of the references.
	 */
	/*public Instruction(JSONObjectInterface jsonObject) throws DeserializationException, IOException {
		
	}*/
	
	/**
	 * @return The raw {@link MustacheTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}

	/**
	 * Execute this {@link Instruction}, including all its children.
	 * @param variables The {@link Variables} to use when compiling {@link MustacheTemplate}s.
	 * @param source The {@link Result} source for this execution.  Can be <code>null</code>.
	 */
	public void execute(RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database,
			Logger log) {
		// Create & initially stock queue.
		Vector queue = new Vector();
		queue.add(new Executable(this, compiler,
				browser, variables, source, database));
		
		// Run queue.
		while(queue.size() > 0) {
			Executable exc = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			
			if(log != null) {
				log.i("Running " + exc.toString());
			}
			exc.run();
			
			// If the execution is complete, add its children to the queue.
			if(exc.isComplete()) {
				VectorUtils.arrayIntoVector(exc.getChildren(), queue);
			} else if (exc.isStuck()) {
				if(log != null) {
					log.i(StringUtils.quote(exc.toString()) + " is stuck on " + StringUtils.quote(exc.stuckOn()));
				}
			} else if (exc.hasFailed()) {
				if(log != null) {
					log.w(exc.failedBecause());
				}
			// If the execution is not stuck and is not failed, return it to the end queue.
			} else {
				queue.addElement(exc);
			}
			
			if(!exc.isComplete() && !exc.isStuck() && !exc.hasFailed()) {
				queue.addElement(exc);
			}
		}
	}
	
	/**
	 * Generate the children of this {@link Instruction} during execution.  There will be as many children
	 * as the product of <code>sources</code> and {@link #children}.
	 * @param sources The {@link Result} array from which to generate children.
	 * @return An array of {@link Executable[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws IOException If there was an error loading the {@link Instruction} for one of the children.
	 * @throws DeserializationException If there was an error deserializing the {@link Instruction} for one
	 * of the children.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	public Executable[] generateChildExecutables(RegexpCompiler compiler, Browser browser,
			Executable parent, Result[] sources, Database database)
				throws MissingVariableException, DeserializationException, IOException {
		Executable[] childExecutables = new Executable[sources.length * children.length];
		
		for(int i = 0; i < sources.length ; i++) {
			Result source = sources[i];
			for(int j = 0 ; j < children.length ; j++) {
				childExecutables[(i * children.length) + j] =
					new Executable(children[j], compiler, browser, parent, source, database);
			}
		}
		return childExecutables;
	}

	/**
	 * @param compiler The {@link RegexpCompiler} to parse with.
	 * @param browser The {@link Browser} to load with.
	 * @param variables The {@link Variables} to execute using.
	 * @param source The {@link String} to use as a source.  Can be <code>null</code>.
	 * @return An array of {@link String}s from executing this particular {@link Instruction}.  Will be passed to
	 * {@link generateChildren}.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws BrowserException If the {@link Browser} experienced an exception loading.
	 * @throws RegexpException If there was a problem matching with {@link RegexpCompiler}.
	 * @throws DatabaseException If there was a problem storing data in {@link Database}.
	 */
	public final Result[] generateResults(RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database) throws MissingVariableException,
			BrowserException, RegexpException, DatabaseException {
		String[] resultValues;
		if(source == null) {
			resultValues = generateResultValues(compiler, browser, variables, null);
		} else {
			resultValues = generateResultValues(compiler, browser, variables, source.getValue());
		}
		Result[] results = new Result[resultValues.length];
		for(int i = 0 ; i < resultValues.length ; i ++) {
			//String name = getName(variables, browser, compiler);
			int id;
			if(source == null) {
				id = database.store(name, shouldSaveValue ? resultValues[i] : null, i);
			} else {
				id = database.store(source.getName(), source.getId(), name, shouldSaveValue ? resultValues[i] : null, i);
			}
			results[i] = new Result(id, name, resultValues[i]);
		}
		return results;
	}
	
	protected abstract String[] generateResultValues(RegexpCompiler compiler, Browser browser,
			Variables variables, String source) throws MissingVariableException,
			BrowserException, RegexpException;
	
	//protected abstract String getDefaultName(Variables variables, RegexpCompiler compiler,
	//		Browser browser) throws MissingVariableException, RegexpException;
}
