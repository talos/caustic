package net.microscraper.instruction;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplate;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.executable.Executable;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.regexp.RegexpException;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public abstract class Instruction  {
	
	/**
	 * Key for {@link #findManys} when deserializing from JSON.
	 */
	public static final String FINDS_MANY = "finds_many";

	/**
	 * Key for {@link #findOnes} when deserializing from JSON.
	 */
	public static final String FINDS_ONE = "finds_one";

	/**
	 * Key for {@link #spawnPages} when deserializing from JSON.
	 */
	public static final String THEN = "then";
	
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
	 * Name should therefore be retrieved through {@link #getName(Variables, Browser, RegexpCompiler)}.
	 * @see #getName(Variables, Browser, RegexpCompiler)
	 */
	private final MustacheTemplate name;
	
	/**
	 * Get the name for this {@link Instruction}.  Will return {@link #name}, compiled, unless it is
	 * <code>null</code>. In that case, it will return {@link #getDefaultName(Variables, RegexpCompiler, Browser)}
	 * instead.
	 * @param variables {@link Variables} for Mustache substitution.
	 * @param browser A {@link Browser} to use for encoding.
	 * @param compiler A {@link RegexpCompiler}.
	 * @return The {@link String}.
	 * @throws MissingVariableException if a {@link MustacheTemplate} name could not be compiled.
	 * @throws RegexpException
	 */
	private String getName(Variables variables, Browser browser, RegexpCompiler compiler)
			throws MissingVariableException, RegexpException {
		return name != null ? name.compile(variables) : getDefaultName(variables, compiler, browser);
	}
	
	/**
	 * The {@link JSONInterfaceObject} this {@link Instruction} was deserialized from,
	 * as a formatted {@link String}.
	 */
	private final String formattedJSON;

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
	public abstract boolean defaultShouldSaveValue();
	
	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param jsonObject The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Instruction(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		try {
			this.formattedJSON = jsonObject.toString();
			
			if(jsonObject.has(NAME)) {
				name = new MustacheTemplate(jsonObject.getString(NAME));
			} else {
				name = null;
			}
			if(jsonObject.has(SAVE)) {
				shouldSaveValue = jsonObject.getBoolean(SAVE);
			} else {
				shouldSaveValue = this.defaultShouldSaveValue();
			}
			
			Vector children = new Vector();
			if(jsonObject.has(FINDS_MANY)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(FINDS_MANY)) {
					children.add(new FindMany(jsonObject.getJSONObject(FINDS_MANY)));
				} else {
					JSONInterfaceArray array = jsonObject.getJSONArray(FINDS_MANY);
					for(int i = 0 ; i < array.length() ; i ++) {
						children.add(new FindMany(array.getJSONObject(i)));
					}
				}
			}
			
			if(jsonObject.has(FINDS_ONE)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(FINDS_ONE)) {
					children.add(new FindOne(jsonObject.getJSONObject(FINDS_ONE)));
				} else {
					JSONInterfaceArray array = jsonObject.getJSONArray(FINDS_ONE);
					for(int i = 0 ; i < array.length() ; i ++) {
						children.add(new FindOne(array.getJSONObject(i)));
					}
				}					
			}
			
			if(jsonObject.has(THEN)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(THEN)) {
					children.add(new Page(jsonObject.getJSONObject(THEN)));
				} else {
					JSONInterfaceArray array = jsonObject.getJSONArray(THEN);
					for(int i = 0 ; i < array.length() ; i ++) {
						children.add(new Page(array.getJSONObject(i)));
					}
				}
			}
			this.children = new Instruction[children.size()];
			children.copyInto(this.children);
			
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheTemplateException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * @return {@link #formattedJSON}
	 */
	public String toString() {
		return formattedJSON;
	}
	

	/**
	 * @param sources The {@link Result} array from which to generate children.
	 * @return An array of {@link Executable[]}s whose parent is this execution.
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
	public final Executable[] generateChildren(RegexpCompiler compiler, Browser browser,
			Variables variables, Result[] sources, Database database)
				throws MissingVariableException, DeserializationException, IOException {
		Executable[] childExecutables = new Executable[sources.length * children.length];
		
		for(int i = 0; i < sources.length ; i++) {
			Result source = sources[i];
			for(int j = 0 ; j < this.children.length ; j++) {
				childExecutables[i * j + i] =
						new Executable(this.children[j], compiler, browser, variables, source, database);
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
	 * @throws BrowserException
	 * @throws RegexpException
	 * @throws DatabaseException
	 */
	public Result[] execute(RegexpCompiler compiler, Browser browser,
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
			if(source == null) {
				results[i] = database.store(getName(variables, browser, compiler), resultValues[i], i, shouldSaveValue);	
			} else {
				results[i] = database.store(source, getName(variables, browser, compiler), resultValues[i], i, shouldSaveValue);
			}
		}
		return results;
	}
	
	protected abstract String[] generateResultValues(RegexpCompiler compiler, Browser browser,
			Variables variables, String source) throws MissingVariableException,
			BrowserException, RegexpException;
	
	protected abstract String getDefaultName(Variables variables, RegexpCompiler compiler,
			Browser browser) throws MissingVariableException, RegexpException;
}
