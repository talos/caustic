package net.microscraper.util;

import java.io.IOException;
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
	private final Hashtable hashtable;
	
	private final String sourceName;
	private final boolean hasSource;
	private final int sourceNumber;
	
	/**
	 * Non-branched {@link Variables}.
	 * @param database
	 * @param hashtable
	 */
	private Variables(Database database, Hashtable hashtable) {
		this.hashtable = hashtable;
		this.database = database;
		this.parent = null;
		
		this.hasSource = false;
		this.sourceNumber = 0;
		this.sourceName = null;
	}
	
	/**
	 * Branched from another {@link Variables}, but not sharing a table.
	 * @param parent
	 * @param hashtable
	 * @param sourceNumber
	 * @param sourceName
	 */
	private Variables(Variables parent, Hashtable hashtable, int sourceNumber, String sourceName) {
		this.hashtable = hashtable;
		this.database = parent.database;
		this.parent = parent;
		
		this.hasSource = true;
		this.sourceNumber = sourceNumber;
		this.sourceName = sourceName;
	}
	
	/**
	 * Branched from another {@link Variables}, but sharing a table.
	 * @param parent
	 * @param sourceNumber
	 * @param sourceName
	 */
	private Variables(Variables parent, int sourceNumber, String sourceName, String sourceValue, boolean shouldContainKeyValue) {
		this.parent = parent;
		this.hashtable = parent.hashtable;
		this.database = parent.database;
		
		this.hasSource = true;
		this.sourceNumber = sourceNumber;
		this.sourceName = sourceName;
		
		if(shouldContainKeyValue == true) {
			hashtable.put(sourceName, sourceValue);
		} 
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
	 * The resulting array of 
	 * {@link Variables} will read from <code>parent</code> if it can't find a key in itself, but will have its
	 * own backing table.  There will be as many of them as the length of <code>values</code>.
	 * @param parent The {@link Variables} to branch from.
	 * @param key A new {@link String} key that will be included in each branch.
	 * @param values An array of {@link String}s, where each element will be the value for
	 * <code>key</code> in one of the resulting branches. Must be of length greater than one.
	 * Use {@link #singleBranch(Variables, String, String, boolean, boolean)} for single
	 * result.
	 * @param shouldContainKeyValue Whether the new {@link Variables} should contain <code>parent</code>
	 * and <code>key</code> as an entry.
	 * @param shouldPersistValue
	 * @return An array of {@link Variables} branches.
	 * @throws IOException
	 */
	public static Variables[] multiBranch(Variables parent, String key, String[] values,
				boolean shouldContainKeyValue,
				boolean shouldPersistValue)
			throws IOException {
		if(values.length == 0) {
			throw new IllegalArgumentException("Cannot branch without values.");
		} else if(values.length == 1) {
			throw new IllegalArgumentException("Should use singleBranch");
		}
		final Variables[] branches = new Variables[values.length];
		for(int i = 0 ; i < values.length ; i ++) {
			String value = values[i];
			int sourceNumber = parent.store(key, value, i, shouldPersistValue);
			Hashtable branchHashtable = new Hashtable();
			if(shouldContainKeyValue == true) {
				branchHashtable.put(key, value);
			}
			branches[i] = new Variables(parent, branchHashtable, sourceNumber, key);
		}
		return branches;
	}

	/**
	 * Create a {@link Variables} branch.<p>
	 * It will share a backing table with <code>parent</code>.
	 * @param parent The {@link Variables} to branch from.
	 * @param key A new {@link String} key that will be included in the branch and its parent.
	 * @param value An {@link String} value that will map to <code>key</code> in the branch and its parent.
	 * @param shouldContainKeyValue Whether the new {@link Variables} should contain <code>parent</code>
	 * and <code>key</code> as an entry.
	 * @param shouldPersistValue
	 * @return An {@link Variables} branch.
	 * @throws IOException
	 */
	public static Variables singleBranch(Variables parent, String key, String value,
				boolean shouldContainKeyValue,
				boolean shouldPersistValue)
			throws IOException {
		int sourceNumber = parent.store(key, value, 0, shouldPersistValue);
		return new Variables(parent, sourceNumber, key, value, shouldContainKeyValue);
	}
	/**
	 * Initialize an empty {@link Variables} with a {@link Database}.
	 */
	public static Variables empty(Database database) {
		return new Variables(database, new Hashtable());
	}
	
	/**
	 * Initialize {@link Variables} values from a {@link Hashtable}.  Its keys and values must
	 * all be {@link String}s.  <code>initialHashtable</code> is copied, and will not be modified
	 * as {@link Variables} changes.
	 * @param database
	 * @param initialHashtable Initial {@link Hashtable} whose mappings should stock {@link Variables}.
	 * @throws IOException
	 */
	public static Variables fromHashtable(Database database, Hashtable initialHashtable) throws IOException {
		Enumeration enum = initialHashtable.keys();
		while(enum.hasMoreElements()) {
			try {
				String key = (String) enum.nextElement();
				String value = (String) initialHashtable.get(key);
				//database.storeInitial(key, value, 0);
				//variables.put((String) key, value, true);
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Variables must be initialized with String-String hashtable.", e);
			}
		}
		
		return new Variables(database, (Hashtable) initialHashtable.clone());
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
	 * Return a {@link String} representation of this {@link Variables} backing {@link Hashtable}
	 * and that of all its parents, if any.
	 */
	public String toString() {
		String result = "";
		if(parent != null) {
			result += parent.toString() + " >> ";
		}
		result += StringUtils.quote(hashtable.toString());
		return result;
	}
}
