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
 * The ID of the waiting request can be found in the tag at {@link R.id#wait_id}.
 * @author talos
 *
 */
class WaitRow {

	/**
	 * 
	 * @param context
	 * @param name The {@link String} name to display as what will be loaded.
	 * @param requestId The {@link String} ID of the request to load.
	 * @param convertView A {@link View} to reuse, if possible.  If this is <code>null</code>,
	 * a new view will be inflated.
	 * @return A {@link View} of the wait row.
	 */
	public static View initialize(Context context, String name, String requestId, View convertView) {
		final View view;
		if(convertView == null) {
			view = View.inflate(context, R.layout.wait_row, null);
		} else {
			view = convertView;
		}
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		Button loadButton = (Button) view.findViewById(R.id.load_button);
		loadButton.setTag(R.id.wait_id, requestId);
		return view;
	}
}
