package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.schema.Reference;

public class Executions {
	private class Execution {
		public static final boolean TRUNK = true;
		public static final boolean BRANCH = false;
		
		protected final Reference source_scraper_ref;
		protected final Reference scraper_ref;
		protected final String source_value;
		protected final Execution source_execution;
		protected final boolean klass;
		
		protected Execution(Reference _source_scraper_ref, Reference _scraper_ref,
				String _source_value, boolean _klass) {
			source_scraper_ref = _source_scraper_ref;
			source_value = _source_value;
			scraper_ref = _scraper_ref;
			klass = _klass;
			
			if(source_scraper_ref == null) {
				source_execution = null;
			} else {
				if(scraper_value_executions.containsKey(source_scraper_ref)) {
					Hashtable possible_source_executions = (Hashtable) scraper_value_executions.get(source_scraper_ref);
					if(possible_source_executions.containsKey(source_value)) {
						source_execution = (Execution) possible_source_executions.get(source_value);
					} else {
						throw new NullPointerException("Cannot find source execution of value " + source_value + " for " + toString());
					}
				} else {
					throw new NullPointerException("Cannot find source scraper " + source_scraper_ref.toString() + " for " + toString());
				}
			}
			
			if(source_execution == null) {
				null_source_scraper_executions.put(scraper_ref, this);
			} else if(source_execution.klass == TRUNK && klass == TRUNK) { 
				// Allow us to easily track trunk executions down the hierarchy.
				Trunk source_trunk = (Trunk) source_execution;
				while(source_trunk.source_execution != null) {
					if(source_trunk.source_execution.klass == TRUNK) {
						source_trunk = (Trunk) source_trunk.source_execution;
					}
				}
				if(!trunk_child_trunks.containsKey(source_trunk))
					trunk_child_trunks.put(source_trunk, new Vector());
				((Vector) trunk_child_trunks.get(source_trunk)).addElement(this);
			}
		}
		
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof Reference))
				return false;
			return this.toString().equals(obj.toString());
		}
		
		public String toString() {
			if(source_scraper_ref == null)
				return scraper_ref.toString();
			else
				return source_scraper_ref.toString() + Reference.SEPARATOR + scraper_ref.toString();
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	private class Branch extends Execution {
		public final String[] values;
		public Branch(Reference _source, Reference _executor, String source_value, String[] _values) {
			super(_source, _executor, source_value, BRANCH);
			values = _values;
		}
	}
	
	private class Trunk extends Execution {
		public final String value;
		public Trunk(Reference _source, Reference _executor, String source_value, String _value) {
			super(_source, _executor, source_value, TRUNK);
			value = _value;
		}
	}
	
	// Hashtable with executions where trunk is null.
	private final Hashtable null_source_scraper_executions = new Hashtable();
	
	// Hashtable keyed by trunk with vectors of other trunks.
	private final Hashtable trunk_child_trunks = new Hashtable();
	
	// Hashtable keyed by scraper containing Hashtables keyed by result value.
	private final Hashtable scraper_value_executions = new Hashtable();
		
	public void put(Reference executing_scraper_ref, String value) {
		new Trunk(null, executing_scraper_ref, null, value);
	}
	
	public void put(Reference executing_scraper_ref, String[] values) {
		new Branch(null, executing_scraper_ref, null, values);
	}
	
	public void put(Reference source_scraper_ref, Reference executing_scraper_ref, String source_value, String value) {
		new Trunk(source_scraper_ref, executing_scraper_ref, source_value, value);
	}
	
	public void put(Reference source_scraper_ref, Reference executing_scraper_ref, String source_value, String[] values) {
		new Branch(source_scraper_ref, executing_scraper_ref, source_value, values);
	}
	
	/**
	 * Get a hashtable of values appropriate for the context of a scraper execution with a generic source.
	 * @return
	 */
	public Hashtable getVariables() {
		Hashtable variables = new Hashtable();
		Enumeration e;
		
		// Copy in all global values -- those from a trunk execution with a null source.
		e = null_source_scraper_executions.elements();
		while(e.hasMoreElements()) {
			Execution execution = (Execution) e.nextElement();
			if(execution.klass == Execution.TRUNK) {
				variables.put(execution.scraper_ref, ((Trunk) execution).value);
			}
		}
		return variables;
	}
	/**
	 * Get a hashtable of values appropriate for the context of a specific scraper execution.
	 * @param source_scraper_ref The Reference of the executing scraper's source scraper (if any).
	 * @param source_value The source value of the executing scraper's source scraper (if any).
	 * @return A Hashtable, keyed by executing scraper refs, of available values.
	 */
	public Hashtable getVariables(Reference source_scraper_ref, String source_value) {
		Hashtable variables = getVariables();
		if(source_scraper_ref == null) {
			return variables;
		} else {
			// Copy in all values from trunk executions sharing a source with this scraper.
			
			// Copy in values from all this scraper's sources, in addition to any values from 
			// scrapers that share a source as a trunk.
			Execution cur_source = (Execution) ((Hashtable) scraper_value_executions.get(source_scraper_ref)).get(source_value);
			while(cur_source != null) {
				if(cur_source.klass == Execution.TRUNK) {
					Trunk trunk = (Trunk) cur_source;
					variables.put(trunk.scraper_ref, trunk.value);
					Vector child_trunks = (Vector) trunk_child_trunks.get(trunk);
					for(int i = 0; i < child_trunks.size(); i++) {
						Trunk child_trunk = (Trunk) child_trunks.elementAt(i);
						variables.put(child_trunk.scraper_ref, child_trunk.value);
					}
				}
				if(cur_source.source_scraper_ref != null) {
					variables.put(cur_source.source_scraper_ref, cur_source.source_value);
				}
				cur_source = cur_source.source_execution;
			}
			return variables;
		}
	}
}
