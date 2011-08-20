package net.microscraper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.database.TableManipulationException;

/**
 * {@link Variables} hold information collected through the execution of {@link Instruction}s,
 * as well as default values.
 * @author talos
 *
 */
public class Variables {
	
	private final Variables parent;
	private final Database database;
	private Hashtable hashtable; // = new Hashtable();
	
	private final String sourceName;
	private final boolean hasSource;
	private final int sourceNumber;
	
	/**
	 * Variables without a source.
	 * @param database
	 * @param hashtable
	 */
	private Variables(Database database, Hashtable hashtable) {
		this.hashtable = hashtable;
		this.database = database;
		parent = null;
		
		this.hasSource = false;
		this.sourceNumber = 0;
		this.sourceName = null;
	}

	/**
	 * Variables with a source.
	 * @param database
	 * @param hashtable
	 * @param sourceNumber
	 * @param sourceName
	 */
	private Variables(Database database, Hashtable hashtable, int sourceNumber, String sourceName) {
		this.hashtable = hashtable;
		this.database = database;
		parent = null;
		
		this.hasSource = true;
		this.sourceNumber = sourceNumber;
		this.sourceName = sourceName;
	}
	
	/**
	 * Variables branched from another {@link Variables}.
	 * @param parent
	 * @param sourceNumber
	 * @param sourceName
	 */
	private Variables(Variables parent, int sourceNumber, String sourceName) {
		this.parent = parent;
		this.database = parent.database;
		
		this.hasSource = true;
		this.sourceNumber = sourceNumber;
		this.sourceName = sourceName;
	}
	
	private int store(String name, String value, int resultNum, boolean shouldPersistValue)
				throws TableManipulationException, IOException {
		if(shouldPersistValue == false) {
			value = null;
		}
		if(hasSource) {
			return database.store(sourceName, sourceNumber, name, value, resultNum);
		} else {
			return database.storeInitial(name, value, resultNum);
		}
	}
	
	/**
	 * Create {@link Variables} branches.<p>
	 * If the length of <code>values</code> is <code>1</code>, then the resulting (single) {@link Variables}
	 * will share a backing table with <code>parent</code>.
	 * If the length of <code>values</code> is greater than <code>1</code>, then the resulting array of 
	 * {@link Variables} will read from <code>parent</code> if it can't find a key in itself, but will have its
	 * own backing table.
	 * @param parent The {@link Variables} to branch from.
	 * @param key A new {@link String} key that will be included in each branch.
	 * @param value An array of {@link String}s, where each element will be the value for
	 * <code>key</code> in one of the resulting branches. Must be of non-zero length.
	 * @param shouldPersist
	 * @return An array of {@link Variables} branches.
	 * @throws IOException
	 */
	public static Variables[] branch(Variables parent, String key, String[] values, boolean shouldPersistValue)
			throws IOException {
		if(values.length == 0) {
			throw new IllegalArgumentException("Cannot branch without values.");
		}
		final Variables[] branches = new Variables[values.length];
		if(values.length == 1) {
			String value = values[0];
			int sourceNumber = parent.store(key, value, 0, shouldPersistValue);
			branches[0] = new Variables(parent.database, parent.hashtable, sourceNumber, key);
		} else {
			for(int i = 0 ; i < values.length ; i ++) {
				String value = values[i];
				int sourceNumber = parent.store(key, value, i, shouldPersistValue);
				branches[i] = new Variables(parent.database, new Hashtable(), sourceNumber, key);
			}
		}
		return branches;
	}

	/**
	 * Initialize an empty {@link Variables} with a {@link Database}.
	 */
	public static Variables empty(Database database) {
		return new Variables(database, new Hashtable());
	}
	
	/**
	 * Turn a form-encoded {@link String} into {@link Variables}.
	 * @param database
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.  It must be 
	 * correctly formatted.
	 * @param encoding The encoding to use.  <code>UTF-8</code> recommended.
	 * @return A {@link Variables}.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 * @throws IOException If values could not be persisted to <code>database</code>.
	 */
	public static Variables fromFormEncoded(Database database,
				Decoder decoder, String formEncodedData, String encoding)
			throws UnsupportedEncodingException, IOException {
		String[] splitByAmpersands = StringUtils.split(formEncodedData, "&");
		Hashtable hashtable = new Hashtable();
		for(int i = 0 ; i < splitByAmpersands.length; i++) {
			String[] pair = StringUtils.split(splitByAmpersands[i], "=");
			if(pair.length == 2) {
				hashtable.put(decoder.decode(pair[0], encoding),
						decoder.decode(pair[1], encoding));
			} else {
				throw new IllegalArgumentException(StringUtils.quote(splitByAmpersands[i]) + " is not a valid name-value pair.");
			}
		}
		return fromHashtable(database, hashtable);
	}
	
	/**
	 * Initialize {@link Variables} values from a {@link Hashtable}.  Its keys and values must
	 * all be {@link String}s.  <code>initialHashtable</code> is copied.
	 * @param database
	 * @param initialHashtable Initial {@link Hashtable} whose mappings should stock {@link Variables}.
	 * @throws IOException
	 */
	public static Variables fromHashtable(Database database, Hashtable initialHashtable) throws IOException {
		Enumeration enum = initialHashtable.keys();
		Variables variables = Variables.empty(database);
		while(enum.hasMoreElements()) {
			try {
				String key = (String) enum.nextElement();
				String value = (String) initialHashtable.get(key);
				database.storeInitial(key, value, 0);
				//variables.put((String) key, value, true);
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Variables must be initialized with String-String hashtable.", e);
			}
		}
		return variables;
	}

	/**
	 * 
	 * @param key A {@link String} key.
	 * @return A {@link String} value.
	 * @see Hashtable#get
	 * @see #containsKey(String key)
	 */
	public String get(String key) {
		if(hashtable.containsKey(key)) {
			return (String) hashtable.get(key);			
		} else if(parent != null) {
			return parent.get(key);
		} else {
			return null;
		}
	}

	/**
	 * Tests if the specified object is a key in this {@link Variables}. 
	 * @param key The possible {@link String} key 
	 * @return <code>true</code> if and only if the specified <code>key</code> is a key
	 * in this {@link Variables}.
	 * @see Hashtable#containsKey
	 * @see #get(String key)
	 */
	public boolean containsKey(String key) {
		if(hashtable.containsKey(key)) {
			return true;
		} else if(parent != null) {
			return parent.containsKey(key);
		} else {
			return false;
		}
	}
	
	/**
	 * Add a {@link String} - {@link String} pair to this {@link Variables}.
	 * @param key The {@link String} key.
	 * @param value The {@link String} value.
	 * @param shouldPersist Whether this value should be saved to the {@link Database}.
	 */
	/*
	public void put(String key, String value, boolean shouldPersist) {
		hashtable.put(key, value);
		if(shouldPersist == false) {
			value = null;
		}
		if(hasSource) {
			database.store(sourceName, sourceId, name, value, resultNum)
		} else {
			
		}
		if(shouldPersist) {
			database.storeInitial(key, value, 0);
		} else {
			database.store(sourceName, sourceId, key, null, 0);
		}	}
		*/
}
