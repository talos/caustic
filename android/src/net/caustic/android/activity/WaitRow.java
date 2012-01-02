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
class WaitRow extends RelativeLayout {
	private final String requestID;
	
	WaitRow(Context context, String name, String requestID) {
		super(context);
		this.requestID = requestID;
		View view = View.inflate(context, R.layout.wait_row, null);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		//ImageButton loadButton = (ImageButton) view.findViewById(R.id.load_button);
	}
	
	String getWaitingRequestID() {
		return requestID;
	}
}
