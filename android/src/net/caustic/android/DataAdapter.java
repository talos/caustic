/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import net.caustic.Request;
import net.caustic.android.AndroidRequester;
import net.caustic.android.ChildContainer;
import net.caustic.android.DataRow;
import net.caustic.android.DataView;
import net.caustic.android.Database;
import net.caustic.android.FindDescription;
import net.caustic.android.WaitRow;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

final class DataAdapter implements ListAdapter {

	private static final int DATA_ROW = 0;
	private static final int WAIT_ROW = 1;
	private static final int CHILD_CONTAINER = 2;
	
	private final Map<String, String> data;
	private final Map<String, Request> waits;
	private final Map<String, Map<String, String>> children;
	
	private final TreeMap<String, Integer> dataTypes = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	private final String[] keys;
	private final AndroidRequester requester;
	private final DataView dataView;
	
	DataAdapter(Database db, AndroidRequester requester, DataView dataView, String scope) {
		this.requester = requester;
		this.dataView = dataView;
		data = db.getData(scope, FindDescription.EXTERNAL); // show externally visible data only
		waits = db.getWait(scope);
		children = db.getChildren(scope);
		
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
			row = WaitRow.initialize(context, name, requester, waits.get(name));
			break;
		case CHILD_CONTAINER:
			row = ChildContainer.initialize(context, name, dataView, children.get(name));
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
	
	private String getName(int position) {
		return keys[position];
	}
}