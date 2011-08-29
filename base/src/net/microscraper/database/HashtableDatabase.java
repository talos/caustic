package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

import net.microscraper.util.StringUtils;
import net.microscraper.util.UUIDFactory;

/**
 * {@link HashtableDatabase} hold information collected through the execution of {@link Instruction}s,
 * as well as default values.
 * @author talos
 *
 */
public class HashtableDatabase implements Database {
	
	/**
	 * A {@link Hashtable} of {@link Scope}s keyed by scope UUIDs as ints.
	 */
	private final Hashtable scopeSources = new Hashtable();
	
	/**
	 * A {@link Hashtable} of {@link Hashtable}s keyed by scope UUIDs as ints.
	 */
	private final Hashtable scopeTables = new Hashtable();
	
	private final UUIDFactory uuidFactory;
	
	public HashtableDatabase(UUIDFactory uuidFactory) {
		this.uuidFactory = uuidFactory;
	}
	
	public void storeOneToOne(Scope source, String name)
			throws TableManipulationException, IOException {
		// No-op: can't put null value in a hashtable.
	}
	
	public void storeOneToOne(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		Hashtable table = (Hashtable) scopeTables.get(source);
		table.put(name, value);
	}

	public Scope storeOneToMany(Scope source, String name)
			throws TableManipulationException, IOException {
		Scope newScope = getScope();
		scopeSources.put(newScope, source);
		// Can't store anything, no value.
		
		return newScope;
	}
	
	public Scope storeOneToMany(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		Scope newScope = storeOneToMany(source, name); // generate new scope without saving value
		storeOneToOne(newScope, name, value); // save value to new scope
		return newScope;
	}
	
	public String get(Scope scope, String key) {
		Hashtable table = (Hashtable) scopeTables.get(scope);
		if(table.containsKey(key)) {
			return (String) table.get(key);
		} else if(scopeSources.containsKey(scope)) {
			Scope source = (Scope) scopeSources.get(scope);
			return get(source, key);
		} else {
			return null;
		}
	}
	
	public String toString(Scope scope) {
		String result = "";
		//Integer sourceId = Integer.valueOf(scope);
		while(scopeSources.containsKey(scope)) {
			Hashtable table = (Hashtable) scopeTables.get(scope);
			result += StringUtils.quote(table.toString()) + " << ";
			scope = (Scope) scopeSources.get(scope);
		}
		return result;
	}
	
	public void close() throws IOException {
		scopeTables.clear();
		scopeSources.clear();
	}
	
	public Scope getScope() throws IOException {
		Scope scope = new Scope(uuidFactory.get());
		scopeTables.put(scope, new Hashtable());
		return scope;
	}
}
