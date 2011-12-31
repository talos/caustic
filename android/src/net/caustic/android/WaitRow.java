/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import net.caustic.Request;
import net.caustic.android.AndroidRequester;
import net.caustic.android.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author talos
 *
 */
class WaitRow {
	static View initialize(Context context, String name, AndroidRequester requester, Request request) {
		View view = View.inflate(context, R.layout.wait_row, null);
		TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(name);
		ImageButton loadButton = (ImageButton) view.findViewById(R.id.load_button);
		loadButton.setOnClickListener(new Listener(requester, request));
		return view;
	}
	
	/**
	 * A listener that will force the requester to load the bound request when clicked.
	 * @author talos
	 *
	 */
	private static class Listener implements OnClickListener {
		private final AndroidRequester requester;
		private final Request request;
		private Listener(AndroidRequester requester, Request request) {
			this.requester = requester;
			this.request = request;
		}

		@Override
		public void onClick(View v) {
			requester.request(request.id, request.instruction, request.uri, request.input, true);			
		}
	}
}
