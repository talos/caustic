/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import net.caustic.Request;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

final class DataAdapter extends BaseAdapter {

	private static final int DATA_ROW = 0;
	private static final int WAIT_ROW = 1;
	private static final int CHILD_CONTAINER = 2;
	
	private final TreeMap<String, Integer> dataTypes = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> data;
	private Map<String, String> waits;
	private Map<String, Map<String, String>> children;	
	private String[] keys;
	
	DataAdapter() {
		Map<String, String> data = Collections.emptyMap();
		Map<String, String> waits = Collections.emptyMap();
		Map<String, Map<String, String>> children = Collections.emptyMap();
		setData(data, waits, children);
	}
	
	@Override
	public int getCount() {
		return keys.length;
	}

	@Override
	public Object getItem(int position) {
		String name = getName(position);
		int type = getItemViewType(position);
		switch(type) {
		case DATA_ROW:
			return data.get(name);
		case WAIT_ROW:
			return waits.get(name);
		case CHILD_CONTAINER:
			return children.get(name);
		}
		throw new IllegalArgumentException("Illegal view type: " + type);
	}

	@Override
	public long getItemId(int position) {
		return position;  // TODO
	}

	@Override
	public int getItemViewType(int position) {
		return dataTypes.get(getName(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final String name = getName(position);
		final int type = getItemViewType(position);
		final Context context = parent.getContext();
		
		final View row;
		switch(type) {
		case DATA_ROW:
			row = DataRow.initialize(context, name, data.get(name));
			break;
		case WAIT_ROW:
			row = new WaitRow(context, name, waits.get(name));
			break;
		case CHILD_CONTAINER:
			row = ChildContainer.initialize(context, name, children.get(name));
			break;
		default:
			throw new IllegalArgumentException("Illegal view type: " + type);
		}
		return row;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
		
	}

	@Override
	public boolean hasStableIds() {
		return false; // TODO: ?
	}

	@Override
	public boolean isEmpty() {
		return keys.length == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if(keys.length > position) {
			return false;
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	void setData(Map<String, String> data, Map<String, String> waits, Map<String, Map<String, String>> children) {
		this.dataTypes.clear();
		
		this.data = data;
		this.waits = waits;
		this.children = children;
		
		// load up info for dataTypes
		for(String key : data.keySet()) {
			dataTypes.put(key, DATA_ROW);
		}
		for(String key : waits.keySet()) {
			dataTypes.put(key, WAIT_ROW);
		}
		for(String key : children.keySet()) {
			dataTypes.put(key, CHILD_CONTAINER);
		}
		new ArrayList<String>(dataTypes.keySet());
		this.keys = dataTypes.keySet().toArray(new String[dataTypes.size()]);
	}
	
	private String getName(int position) {
		return keys[position];
	}

}