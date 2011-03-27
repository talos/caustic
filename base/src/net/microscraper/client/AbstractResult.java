package net.microscraper.client;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Scraper;

public abstract class AbstractResult {
	private final Vector children = new Vector();
	
	protected void addChild(Result result) {
		children.addElement(result);
	}
	public Result[] children() {
		Result[] children_ary = new Result[children.size()];
		children.copyInto(children_ary);
		return children_ary;
	}
	public Variables variables() {
		AbstractResult origin = origin();
		Variables variables = new Variables();
		
		AbstractResult[] children = origin.children();
		for(int i = 0; i < children.length; i++) {
			if(children[i].isBranch() == false) {
				variables.merge(children[i].variables());
			}
		}
		return variables;
	}
	
	public abstract boolean isBranch();
	public abstract AbstractResult origin();
	
	public static class Result extends AbstractResult {
		private final Reference scraper_ref;
		private final boolean branch;
		private final AbstractResult source;
		private final String value;
		
		public Result(Result _source, Scraper _scraper, String _value) {
			source = _source;
			source.addChild(this);
			scraper_ref = _scraper.ref;
			branch = _scraper.branch;
			value = _value;
		}
		
		public Variables variables() {
			return super.variables().put(scraper_ref, value);
		}

		public boolean isBranch() {
			return branch;
		}

		public AbstractResult origin() {
			AbstractResult origin = this;
			while(origin.isBranch() == false) {
				origin = origin.origin();
			}
			return origin;
		}
	}
	
	public static class ResultRoot extends AbstractResult {
		public boolean isBranch() {
			return true;
		}
		public AbstractResult origin() {
			return this;
		}
	}
}