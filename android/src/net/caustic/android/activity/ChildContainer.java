/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.caustic.android.R;

/**
 * @author talos
 *
 */
class ChildContainer {
	
	static View initialize(Context context, String name, Map<String, String> children) {
		View view = View.inflate(context, R.layout.child_container, null);
		
		ViewGroup childrenView = (ViewGroup) view.findViewById(R.id.children);
		//ListAdapter adapter = new ChildAdapter(dataView, children);
		//childrenListView.setAdapter(adapter);
		
		for(Map.Entry<String, String> entry : children.entrySet()) {
			childrenView.addView(new ChildRow(context, entry.getValue(), entry.getKey()));
		}
		
		return view;
	}
}
