/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import net.caustic.android.R;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * @author talos
 *
 */
class ChildRow {

	static View initialize(Context context, String value, String childID) {
		View view = View.inflate(context, R.layout.child_row, null);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		view.setTag(R.id.child_id, childID);
		return view;
	}
}
