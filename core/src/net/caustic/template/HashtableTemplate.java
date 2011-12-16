package net.caustic.template;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.regexp.StringTemplate;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StringMap;
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
	 * @param db
	 * @param scope
	 * @return An {@link HashtableSubstitution} whose
	 * {@link Hashtable} has been substituted with tags accessible to <code>scope</code>.
	 * @throws HashtableSubstitutionOverwriteException if the substitution caused a mapping to
	 * be overwritten.
	 * @throws DatabaseException if <code>input</code> could not be read.
	 */
	public HashtableSubstitution sub(StringMap context)
			throws HashtableSubstitutionOverwriteException {
		Vector missingTags = new Vector();
		Hashtable subbedTable = new Hashtable();
		Enumeration keys = table.keys();
		while(keys.hasMoreElements()) {
			StringTemplate key = (StringTemplate) keys.nextElement();
			StringSubstitution subbedKey;
			subbedKey = key.sub(context);
			
			StringTemplate value = (StringTemplate) table.get(key);
			StringSubstitution subbedValue;
			subbedValue = value.sub(context);
			
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
	 * Extend another {@link HashtableTemplate} with this one.  Will overwrite keys in this
	 * {@link HashtableTemplate}.
	 * @param other The {@link HashtableTemplate} to merge in.
	 * @param overwrite <code>True</code> to overwrite duplicate values from the original table,
	 * <code>false</code> to leave them as-is.
	 */
	public void extend(HashtableTemplate other, boolean overwrite) {
		if(overwrite == true) {
			this.table = HashtableUtils.combine(new Hashtable[] { this.table, other.table });			
		} else {
			this.table = HashtableUtils.combine(new Hashtable[] { other.table, this.table });
		}
	}
	
	public String toString() {
		return table.toString();
	}
}
