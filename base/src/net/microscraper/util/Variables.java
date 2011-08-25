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
		
	private final Variables parent;
	private final Database database;
	private final Hashtable hashtable;
	
	private final int id;
	
	/**
	 * New non-branched {@link Variables}.
	 * @param database The {@link Database} to write to.  All the name-value pairs of
	 * <code>hashtable</code> will be stored in this.
	 * @param hashtable A {@link Hashtable} copy for use as backing table.
	 */
	private Variables(Database database, Hashtable source) throws IOException {
		this.hashtable = new Hashtable();
		this.database = database;
		this.parent = this;
		this.id = database.getFirstId();
		Enumeration keys = source.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) source.get(key);
			save(key, value);
		}
	}

	/**
	 * New branched {@link Variables} that inherits from <code>parent</code> but does not share its
	 * backing table.
	 * @param parent The {@link Variables} parent.
	 * @param resultNum The {@link int} order of this result.
	 * @throws IOException If there was a problem with the {@link #database}.
	 */
	private Variables(Variables parent, int resultNum) throws IOException {
		this.database = parent.database;
		this.parent = parent;
		this.id = database.store(parent.id, resultNum);
		this.hashtable = new Hashtable();
	}
	
	/**
	 * New branched {@link Variables} that inherits from <code>parent</code> but does not share its
	 * backing table.
	 * @param parent The {@link Variables} parent.
	 * @param resultNum The {@link int} order of this result.
	 * @param key The {@link String} key that this {@link Variables} will have, but not share with its
	 * <code>parent</code>.
	 * @param key The {@link String} value that this {@link Variables} will have, but not share with its
	 * <code>parent</code>.
	 * @throws IOException If there was a problem with the {@link #database}.
	 */
	private Variables(Variables parent, int resultNum, String key, String value) throws IOException {
		this.database = parent.database;
		this.parent = parent;
		this.id = database.store(parent.id, resultNum, key, value);
		this.hashtable = new Hashtable();
		this.hashtable.put(key, value);
	}
	
	/**
	 * Create a {@link Variables} branch.<p>
	 * The resulting {@link Variables} will read from <code>parent</code> if it can't find a key in itself,
	 * but will have its own backing table.
	 * @return A {@link Variables} branch.
	 * @throws IOException If the {@link Database} cannot be written to.
	 */
	public Variables branch(int resultNum)
			throws IOException {
		return new Variables(this, resultNum);
	}
	
	public void save(String key, String value) throws IOException {
		parent.hashtable.put(key, value);
		database.store(id, key, value);
	}
	
	/**
	 * Create a {@link Variables} branch.<p>
	 * The resulting {@link Variables} will read from <code>parent</code> if it can't find a key in itself,
	 * but will have its own backing table.
	 * @param resultNum The number of this result.
	 * @param key A new {@link String} key that will be included in the branch but not the parent.
	 * @param value An {@link String} value that will map to <code>key</code> in the branch but not the parent.
	 * @return A {@link Variables} branch.
	 * @throws IOException If the {@link Database} cannot be written to.
	 */
	public Variables saveAndBranch(int resultNum, String key, String value)
			throws IOException {
		return new Variables(this, resultNum, key, value);
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
		} else if(parent != this) {
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
		if(parent != this) {
			result += parent.toString() + " >> ";
		}
		result += StringUtils.quote(hashtable.toString());
		return result;
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
			return new Variables(database, initialHashtable);
		} catch(ClassCastException e) {
			throw new IllegalArgumentException("Variables must be initialized with String-String hashtable.");
		}
	}
}
