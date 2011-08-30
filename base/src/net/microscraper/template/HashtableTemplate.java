package net.microscraper.template;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.HashtableUtils;
import net.microscraper.database.Scope;
import net.microscraper.template.Template;

public class HashtableTemplate {
	private Hashtable table;
	public HashtableTemplate() {
		this.table = new Hashtable();
	}
	
	public void put(Template key, Template value) {
		table.put(key, value);
	}
	
	public int size() {
		return table.size();
	}
	
	/**
	 * Substitute this {@link HashtableTemplate} with values from {@link Database} accessible
	 * to <code>sourceId</code>.
	 * @param scope
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} object, if successful, is an
	 * {@link Hashtable} with all names and values substituted from {@link Variables}.
	 */
	/*public Execution sub(Scope scope) {
		return subEncoded(scope, null);
	}*/
	
	/**
	 * Substitute this {@link HashtableTemplate} with values from {@link Database} accessible
	 * to <code>sourceId</code>.
	 * @param scope
	 * @param encoder
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} object, if successful, is an
	 * {@link Hashtable} with all names and values substituted from {@link Variables}.
	 */
	public Execution sub(Scope scope) {
		Execution[] componentExecutions = new Execution[table.size() * 2];
		Hashtable subbedTable = new Hashtable();
		Enumeration keys = table.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			Template key = (Template) keys.nextElement();
			Template value = (Template) table.get(key);
			Execution subbedKey;
			subbedKey = key.sub(scope);
			/*if(encoder != null) {
				subbedKey = key.subEncoded(scope, encoder);
			} else {
				subbedKey = key.sub(scope);
			}*/
			componentExecutions[i] = subbedKey;
			i++;
			Execution subbedValue;
			subbedValue = value.sub(scope);
			/*if(encoder != null) {
				subbedValue = value.subEncoded(scope, encoder);
			} else {
				subbedValue = value.sub(scope);
			}*/
			componentExecutions[i] = subbedValue;
			i++;
			if(subbedKey.isSuccessful() && subbedValue.isSuccessful()) {
				String subbedKeyStr = (String) subbedKey.getExecuted();
				if(subbedTable.containsKey(subbedKeyStr)) {
					componentExecutions[i - 2] = Execution.templateOverwrites(key, subbedKeyStr);
				} else {
					subbedTable.put( subbedKeyStr, (String) subbedValue.getExecuted());
				}
			}
		}
		Execution combined = Execution.combine(componentExecutions);
		if(combined.isSuccessful()) {
			return Execution.success(subbedTable);
		} else {
			return combined;
		}
	}
	
	/**
	 * Merge another {@link HashtableTemplate} into this one.  Will overwrite keys in this
	 * {@link HashtableTemplate}.
	 * @param other The {@link HashtableTemplate} to merge in.
	 */
	public void merge(HashtableTemplate other) {
		this.table = HashtableUtils.combine(new Hashtable[] { this.table, other.table });
	}
}
