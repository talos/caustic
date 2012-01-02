/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import net.caustic.Request;
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
	private final Request request;
	
	WaitRow(Context context, String name, Request request) {
		super(context);
		this.request = request;
		View view = View.inflate(context, R.layout.wait_row, null);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		//ImageButton loadButton = (ImageButton) view.findViewById(R.id.load_button);
	}
	
	Request getWaitingRequest() {
		return request;
	}
}
