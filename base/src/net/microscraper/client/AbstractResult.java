package net.microscraper.client;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.database.Reference;

public abstract class AbstractResult {
	private final Vector children = new Vector();
	
	public void addOneToOne(Reference scraper_ref, String value) {
		Result child = new Result(this, scraper_ref, value, false);
		children.addElement(child);
	}
	public void addOneToMany(Reference scraper_ref, String[] values) {
		for(int i = 0; i < values.length; i ++) {
			Result child = new Result(this, scraper_ref, values[i], true);
			children.addElement(child);
		}
	}
	
	public Result[] children() {
		Result[] children_ary = new Result[children.size()];
		children.copyInto(children_ary);
		return children_ary;
	}
	
	public Result[] oneToOneDescendents() {
		Result[] children = children();
		Vector oneToOneDescendents = new Vector();
		for(int i = 0 ; i < children.length ; i ++) {
			if(!children[i].isOneToMany()) {
				oneToOneDescendents.addElement(children[i]);
				Utils.arrayIntoVector(children[i].oneToOneDescendents(), oneToOneDescendents);
			}
		}
		Result[] oneToOneDescendents_ary = new Result[oneToOneDescendents.size()];
		oneToOneDescendents.copyInto(oneToOneDescendents_ary);
		return oneToOneDescendents_ary;
	}
	
	public Result[] descendents() {
		Result[] children = children();
		Vector descendents = new Vector();
		for(int i = 0 ; i < children.length ; i ++) {
			descendents.addElement(children[i]);
			Utils.arrayIntoVector(children[i].descendents(), descendents);
		}
		Result[] oneToOneDescendents_ary = new Result[descendents.size()];
		descendents.copyInto(oneToOneDescendents_ary);
		return oneToOneDescendents_ary;
	}
	/*
	public AbstractResult[] livingResults() {
		Vector branches = new Vector();
		if(isOneToMany())
			branches.addElement(this);
		Result[] children = children();
		for(int i = 0; i < children.length; i++) {
			Utils.arrayIntoVector(children[i].livingResults(), branches);
		}
		AbstractResult[] branches_ary = new AbstractResult[branches.size()];
		branches.copyInto(branches_ary);
		return branches_ary;
	}
	*/
	public int size() {
		int size = 1;
		Result[] children = children();
		for(int i = 0; i < children.length; i++) {
			size += children[i].size();
		}
		return size;
	}
	public Variables variables() {
		Variables variables = new Variables();
		Result[] desc = oneToOneDescendents();
		for(int i = 0; i < desc.length; i++) {
			variables.merge(desc[i].variables());
		}
		return variables;
	}
	
	/*
	 * Are there any results for scraping a ref within this scope?
	 */
	public boolean contains(Reference ref) {
		Result[] descendents = descendents();
		for(int i = 0; i < descendents.length; i++) {
			if(descendents[i].ref.equals(ref))
				return true;
		}
		return false;
	}
	
	/*
	 * Get all the results scraping a particular ref within this scope.
	 */
	public Result[] get(Reference ref) {
		Vector results = new Vector();
		Result[] descendents = descendents();
		for(int i = 0; i < descendents.length; i++) {
			if(descendents[i].ref.equals(ref))
				results.addElement(descendents[i]);
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
	}
	
	public abstract boolean isOneToMany();
	public abstract AbstractResult origin();
	public abstract int num();
	
	public static class Result extends AbstractResult {
		public final Reference ref;
		private final boolean one_to_many;
		public final AbstractResult caller;
		private final int num;
		public final String value;
		
		private static int count = 0;
		
		private Result(AbstractResult _caller, Reference _scraper_ref, String _value, boolean _one_to_many) {
			count = count + 1;
			
			num = count;
			caller = _caller;
			
			//source.addChild(this);
			ref = _scraper_ref;
			one_to_many = _one_to_many;
			value = _value;
		}
		
		public Variables variables() {
			Variables variables = super.variables().put(ref, value);
			variables.merge(caller.variables());
			return variables;
		}
		
		public boolean isOneToMany() {
			return one_to_many;
		}
		
		public AbstractResult origin() {
			AbstractResult origin = this;
			while(origin.isOneToMany() == false) {
				origin = origin.origin();
			}
			return origin;
		}
		public int num() {
			return num;
		}
	}
	
	
	public static class ResultRoot extends AbstractResult {
		public boolean isOneToMany() {
			return true;
		}
		public AbstractResult origin() {
			return this;
		}
		public int num() {
			return 0;
		}
	}
}