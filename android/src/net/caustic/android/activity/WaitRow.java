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
 * The ID of the waiting request can be found in the tag at {@link R.id#wait_id}.
 * @author talos
 *
 */
class WaitRow {

	public static View initialize(Context context, String name, String requestID) {
		View view = View.inflate(context, R.layout.wait_row, null);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		view.setTag(R.id.wait_id, requestID);
		return view;
	}
}
