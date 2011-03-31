package net.microscraper.client;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.database.Reference;

public abstract class AbstractResult {
	private final Vector children = new Vector();
	
	/*protected void addChild(Result result) {
		children.addElement(result);
	}*/
	
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
	public AbstractResult[] livingResults() {
		Vector branches = new Vector();
		//if(isOneToMany() && contains(ref))
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
	public Variables variables() {
		Variables variables = new Variables();
		
		Result[] children = children();
		for(int i = 0; i < children.length; i++) {
			if(children[i].isOneToMany() == false) {
				variables.merge(children[i].variables());
			}
		}
		return variables;
	}
	public int size() {
		int size = 1;
		Result[] children = children();
		for(int i = 0; i < children.length; i++) {
			size += children[i].size();
		}
		return size;
	}
	public boolean contains(Reference ref) {
		Result[] children = children();
		for(int i = 0 ; i < children.length ; i ++) {
			if(children[i].scraper_ref.equals(ref))
				return true;
		}
		return false;
	}
	public Result[] get(Reference ref) {
		Vector results = new Vector();
		Result[] children = children();
		for(int i = 0; i < children.length ; i++) {
			if(children[i].scraper_ref.equals(ref))
				//results.addElement(children[i].value);
				results.addElement(children[i]);
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
	}

	public abstract boolean isOneToMany();
	public abstract AbstractResult origin();
	
	public static class Result extends AbstractResult {
		private final Reference scraper_ref;
		private final boolean one_to_many;
		private final AbstractResult source;
		public final String value;
		
		private Result(AbstractResult _source, Reference _scraper_ref, String _value, boolean _one_to_many) {
			source = _source;
			//source.addChild(this);
			scraper_ref = _scraper_ref;
			one_to_many = _one_to_many;
			value = _value;
		}
		
		public Variables variables() {
			Variables variables = super.variables().put(scraper_ref, value);
			variables.merge(source.variables());
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
	}
	
	
	public static class ResultRoot extends AbstractResult {
		public boolean isOneToMany() {
			return true;
		}
		public AbstractResult origin() {
			return this;
		}
	}
}