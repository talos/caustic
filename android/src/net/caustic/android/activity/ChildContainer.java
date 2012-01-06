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
	
	/**
	 * 
	 * @param context
	 * @param name The {@link String} name to display as the title of this child container.
	 * @param children A {@link Map} of {@link String} child IDs to {@link String} values
	 * to display for children.
	 * @param convertView A {@link View} to reuse, if possible.  If this is <code>null</code>,
	 * a new view will be inflated.
	 * @return A {@link View} containing children.
	 */
	static View initialize(Context context, String name, Map<String, String> children, View convertView) {
		final View view;
		if(convertView == null) {
			view = View.inflate(context, R.layout.child_container, null);
		} else {
			view = convertView;
		}
		ViewGroup childrenList = (ViewGroup) view.findViewById(R.id.children);
		
		int oldSize = childrenList.getChildCount();
		
		int i = 0;
		for(Map.Entry<String, String> entry : children.entrySet()) {
			String childId = entry.getKey();
			String childValue = entry.getValue();
			
			// reuse child views if possible
			View oldChildView = childrenList.getChildAt(i);
			View newChildView = ChildRow.initialize(context, childValue, childId, oldChildView);
			if(oldChildView == null) {
				childrenList.addView(newChildView);
			}
			i++;
		}
		
		// Clear out leftover views hanging out at the bottom
		for( ; i < oldSize ; i++) {
			childrenList.removeViewAt(i);
		}
		
		return view;
	}
}
