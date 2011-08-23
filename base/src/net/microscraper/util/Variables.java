package net.microscraper.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.database.Database;

/**
 * {@link Variables} hold information collected through the execution of {@link Instruction}s,
 * as well as default values.
 * @author talos
 *
 */
public class Variables {
	
	public static final int ONLY_RESULT_NUM = 0;
	
	private final Variables parent;
	private final Database database;
	private final Hashtable hashtable;
	
	private final String name;
	private final int number;
	
	/**
	 * New non-branched {@link Variables}.
	 * @param database The {@link Database} to write to.  All the name-value pairs of
	 * <code>hashtable</code> will be stored in this.
	 * @param hashtable A {@link Hashtable} to use as the backing table.  Will be modified.
	 */
	private Variables(Database database, Hashtable hashtable) throws IOException {
		this.hashtable = hashtable;
		this.database = database;
		this.parent = null;
		
		this.number = ONLY_RESULT_NUM;
		this.name = null;
		
		Enumeration keys = hashtable.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) hashtable.get(key);
			database.store(ONLY_RESULT_NUM, i, key, null, value);
			i++;
		}
	}
	
	/**
	 * New branched {@link Variables} that shares a backing table with <code>parent</code>.
	 * @param parent The {@link Variables} parent.
	 * @param name The name of the result that caused the branch.
	 * @param value The value that caused the branch.  Can be null, in which case the value will
	 * not be stored to the backing table.
	 * @throws IOException If there was a problem with the {@link #database}.
	 */
	private Variables(Variables parent, String name, String value) throws IOException {
		this.database = parent.database;
		this.parent = parent;
		
		this.number = this.database.store(parent.number, ONLY_RESULT_NUM, name, parent.name, value);
		this.name = name;
		this.hashtable = parent.hashtable;
		if(value != null) {
			this.hashtable.put(name, value);
		}
	}

	/**
	 * New branched {@link Variables} that inherits from <code>parent</code> but does not share its
	 * backing table.
	 * @param parent The {@link Variables} parent.
	 * @param resultNum The {@link int} order of this result.
	 * @param name The name of the result that caused the branch.
	 * @param value The value that caused the branch.  Can be null, in which case the value will
	 * not be stored to the backing table.
	 * @throws IOException If there was a problem with the {@link #database}.
	 */
	private Variables(Variables parent, int resultNum, String name, String value) throws IOException {
		this.database = parent.database;
		this.parent = parent;
		
		this.number = this.database.store(parent.number, resultNum, name, parent.name, value);
		this.name = name;
		this.hashtable = new Hashtable();
		if(value != null) {
			this.hashtable.put(name, value);
		}
	}

	/**
	 * Initialize an empty {@link Variables} with a {@link Database}.
	 */
	public static Variables empty(Database database) throws IOException {
		return new Variables(database, new Hashtable());
	}
	
	/**
	 * Initialize {@link Variables} values from a {@link Hashtable}.  Its keys and values must
	 * all be {@link String}s.  <code>initialHashtable</code> is copied, and will not be modified
	 * as {@link Variables} changes.
	 * @param database The {@link Database} to persist to.
	 * @param initialHashtable Initial {@link Hashtable} whose mappings should stock {@link Variables}.
	 * @throws IOException If the {@link Database} cannot be written to.
	 */
	public static Variables fromHashtable(Database database, Hashtable initialHashtable) throws IOException {
		try {
			return new Variables(database, (Hashtable) initialHashtable.clone());
		} catch(ClassCastException e) {
			throw new IllegalArgumentException("Variables must be initialized with String-String hashtable.");
		}
	}

	/**
	 * Create a {@link Variables} branch.<p>
	 * It will share a backing table with <code>parent</code>.
	 * @param parent The {@link Variables} to branch from.
	 * @param key A new {@link String} key that will be included in the branch and its parent.
	 * @param value An {@link String} value that will map to <code>key</code> in the branch and its parent.
	 * @param shouldSaveValue Whether <code>value</code> should be saved to the backing table and database.
	 * @return An {@link Variables} branch.
	 * @throws IOException If the {@link Database} cannot be written to.
	 */
	public static Variables singleBranch(Variables parent, String key, String value,
				boolean shouldSaveValue)
			throws IOException {
		return new Variables(parent, key, shouldSaveValue ? value : null);
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
	 * @param shouldSaveValue Whether <code>value</code> should be saved to the backing table and database.
	 * @return An array of {@link Variables} branches.
	 * @throws IOException If the {@link Database} cannot be written to.
	 */
	public static Variables[] multiBranch(Variables parent, String key, String[] values,
				boolean shouldSaveValue)
			throws IOException {
		if(values.length == 0) {
			throw new IllegalArgumentException("Cannot branch without values.");
		} else if(values.length == 1) {
			throw new IllegalArgumentException("Should use singleBranch");
		}
		final Variables[] branches = new Variables[values.length];
		for(int i = 0 ; i < values.length ; i ++) {
			branches[i] = new Variables(parent, i, key, shouldSaveValue ? values[i] : null);
		}
		return branches;
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
