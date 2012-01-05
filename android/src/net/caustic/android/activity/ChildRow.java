/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import net.caustic.android.R;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author talos
 *
 */
class ChildRow extends RelativeLayout {

	ChildRow(Context context, String value, String childID) {
		super(context);
		View view = View.inflate(context, R.layout.child_row, null);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		setTag(R.id.child_id, childID);
	}
}
