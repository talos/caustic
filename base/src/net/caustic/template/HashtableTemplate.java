package net.caustic.template;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.regexp.StringTemplate;
import net.caustic.util.HashtableUtils;
import net.caustic.util.VectorUtils;

public class HashtableTemplate {
	private Hashtable table;
	public HashtableTemplate() {
		this.table = new Hashtable();
	}
	
	public void put(StringTemplate key, StringTemplate value) {
		table.put(key, value);
	}
	
	public int size() {
		return table.size();
	}
	
	/**
	 * Substitute this {@link HashtableTemplate} with values from {@link Database} accessible
	 * to <code>sourceId</code>.
	 * @param input
	 * @return An {@link HashtableSubstitution} whose
	 * {@link Hashtable} has been substituted with tags accessible to <code>scope</code>.
	 * @throws HashtableSubstitutionOverwriteException if the substitution caused a mapping to
	 * be overwritten.
	 * @throws DatabaseException if <code>input</code> could not be read.
	 */
	public HashtableSubstitution sub(DatabaseView input)
			throws HashtableSubstitutionOverwriteException, DatabaseException {
		Vector missingTags = new Vector();
		Hashtable subbedTable = new Hashtable();
		Enumeration keys = table.keys();
		while(keys.hasMoreElements()) {
			StringTemplate key = (StringTemplate) keys.nextElement();
			StringSubstitution subbedKey;
			subbedKey = key.sub(input);
			
			StringTemplate value = (StringTemplate) table.get(key);
			StringSubstitution subbedValue;
			subbedValue = value.sub(input);
			
			if(!subbedKey.isMissingTags() && !subbedValue.isMissingTags()) {
				String subbedKeyStr = (String) subbedKey.getSubstituted();
				if(subbedTable.containsKey(subbedKeyStr)) {
					throw new HashtableSubstitutionOverwriteException(subbedKeyStr, key);
				} else {
					subbedTable.put( subbedKeyStr, (String) subbedValue.getSubstituted());
				}
			} else {
				VectorUtils.arrayIntoVector( StringSubstitution.combine(
						new DependsOnTemplate[] {subbedKey, subbedValue } ), missingTags);
			}
		}
		if(missingTags.size() == 0) {
			return HashtableSubstitution.newSuccess(subbedTable);
		} else {
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return HashtableSubstitution.newMissingTags(missingTagsAry);
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
