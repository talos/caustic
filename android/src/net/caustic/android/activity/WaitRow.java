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
 * The ID of the waiting request can be found in the tag at {@link R.id#wait_id}.
 * @author talos
 *
 */
public class WaitRow extends RelativeLayout {
	WaitRow(Context context, String name, String requestID) {
		super(context);
		View view = View.inflate(context, R.layout.wait_row, null);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		this.setTag(R.id.wait_id, requestID);
	}
}
