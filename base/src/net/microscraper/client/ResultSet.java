package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.Reference;

public class ResultSet {
	private abstract class Result {
		public static final boolean TRUNK = true;
		public static final boolean BRANCH = false;
		
		protected final Reference source_scraper_ref;
		protected final Reference scraper_ref;
		protected final String source_value;
		protected final Result source_result;
		protected final boolean klass;
		
		protected Result(Reference _source_scraper_ref, Reference _scraper_ref,
				String _source_value, boolean _klass) {
			source_scraper_ref = _source_scraper_ref;
			source_value = _source_value;
			scraper_ref = _scraper_ref;
			klass = _klass;
			
			if(source_scraper_ref == null) {
				source_result = null;
			} else {
				if(scraper_value_results.containsKey(source_scraper_ref)) {
					Hashtable possible_source_results = (Hashtable) scraper_value_results.get(source_scraper_ref);
					if(possible_source_results.containsKey(source_value)) {
						source_result = (Result) possible_source_results.get(source_value);
					} else {
						throw new NullPointerException("Cannot find source result of value " + source_value + " for " + toString());
					}
				} else {
					throw new NullPointerException("Cannot find source scraper " + source_scraper_ref.toString() + " for " + toString());
				}
			}
			
			if(source_result == null) {
				null_source_scraper_results.put(scraper_ref, this);
			} else if(source_result.klass == TRUNK && klass == TRUNK) { 
				// Allow us to easily track trunk results down the hierarchy.
				Trunk source_trunk = (Trunk) source_result;
				while(source_trunk.source_result != null) {
					if(source_trunk.source_result.klass == TRUNK) {
						source_trunk = (Trunk) source_trunk.source_result;
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
	
	private class Branch extends Result {
		public final String[] values;
		public Branch(Reference _source, Reference _executor, String source_value, String[] _values) {
			super(_source, _executor, source_value, BRANCH);
			values = _values;
		}
	}
	
	private class Trunk extends Result {
		public final String value;
		public Trunk(Reference _source, Reference _executor, String source_value, String _value) {
			super(_source, _executor, source_value, TRUNK);
			value = _value;
		}
	}
	
	// Hashtable with results where trunk is null.
	private final Hashtable null_source_scraper_results = new Hashtable();
	
	// Hashtable keyed by trunk with vectors of other trunks.
	private final Hashtable trunk_child_trunks = new Hashtable();
	
	// Hashtable keyed by scraper containing Hashtables keyed by result value.
	private final Hashtable scraper_value_results = new Hashtable();
		
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
	 * Get a hashtable of values appropriate for the context of a scraper result with a generic source.
	 * @return
	 */
	public Hashtable getVariables() {
		Hashtable variables = new Hashtable();
		Enumeration e;
		
		// Copy in all global values -- those from a trunk result with a null source.
		e = null_source_scraper_results.elements();
		while(e.hasMoreElements()) {
			Result result = (Result) e.nextElement();
			if(result.klass == Result.TRUNK) {
				variables.put(result.scraper_ref, ((Trunk) result).value);
			}
		}
		return variables;
	}
	/**
	 * Get a hashtable of values appropriate for the context of a specific scraper result.
	 * @param source_scraper_ref The Reference of the executing scraper's source scraper (if any).
	 * @param source_value The source value of the executing scraper's source scraper (if any).
	 * @return A Hashtable, keyed by executing scraper refs, of available values.
	 */
	public Hashtable getVariables(Reference source_scraper_ref, String source_value) {
		Hashtable variables = getVariables();
		if(source_scraper_ref == null) {
			return variables;
		} else {
			// Copy in all values from trunk results sharing a source with this scraper.
			
			// Copy in values from all this scraper's sources, in addition to any values from 
			// scrapers that share a source as a trunk.
			Result cur_source = (Result) ((Hashtable) scraper_value_results.get(source_scraper_ref)).get(source_value);
			while(cur_source != null) {
				if(cur_source.klass == Result.TRUNK) {
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
				cur_source = cur_source.source_result;
			}
			return variables;
		}
	}
}
