/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import net.caustic.android.R;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author talos
 *
 */
class ChildRow {

	/**
	 * Inflate a ChildRow with these parameters.
	 * @param context
	 * @param value The text shown as the value of the child.
	 * @param childId The ID of the child for linking purposes.
	 * @param convertView A {@link View} to reuse.  If this is <code>null</code>,
	 * a new {@link View} will be inflated from XML.
	 * @return The inflated ChildRow {@link View}.
	 */
	static View initialize(Context context, String value, String childId, View convertView) {
		final View view;
		if(convertView == null) {
			view = View.inflate(context, R.layout.child_row, null);
		} else {
			view = convertView;
		}
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		Button goToButton = (Button) view.findViewById(R.id.go_to_child_button);
		goToButton.setTag(R.id.child_id, childId);
		return view;
	}
}
