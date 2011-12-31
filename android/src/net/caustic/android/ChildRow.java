/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import net.caustic.android.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author talos
 *
 */
class ChildRow {

	static View initialize(Context context, String value, DataView dataView, String childID) {
		View view = View.inflate(context, R.layout.child_row, null);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		ImageButton goToChildButton = (ImageButton) view.findViewById(R.id.go_to_child_button);
		goToChildButton.setOnClickListener(new Listener(dataView, childID, value));
		return view;
	}
	
	private static class Listener implements OnClickListener {
		private final DataView dataView;
		private final String childID;
		private final String value;
		private Listener(DataView dataView, String childID, String value) {
			this.dataView = dataView;
			this.childID = childID;
			this.value = value;
		}
		@Override
		public void onClick(View v) {
			dataView.setScope(childID, value);
			//dataView.expand();
		}
	}
}
