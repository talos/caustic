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
class DataRow {
	
	/**
	 * Initialize a row of data.
	 * @param context
	 * @param name The {@link String} name of this piece of data.
	 * @param value The {@link String} value of this piece of data.
	 * @param convertView A {@link View} to reuse, if possible.  If this is <code>null</code>,
	 * a new view will be inflated.
	 * @return The {@link View} data row.
	 */
	static View initialize(Context context, String name, String value, View convertView) {
		final View row;
		if(convertView == null) {
			row = View.inflate(context, R.layout.data_row, null);
		} else {
			row = convertView;
		}
		
		TextView nameView = (TextView) row.findViewById(R.id.name);
		nameView.setText(name);
		
		TextView valueView = (TextView) row.findViewById(R.id.value);
		valueView.setText(value);
		
		return row;
	}
}
