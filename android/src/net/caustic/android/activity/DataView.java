/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticIntent;
import net.caustic.android.service.CausticService;
import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author talos
 *
 */
public class DataView {

	private String scope;
	
	private final TextView title;
	private final ListView data;
	private final View view;
	private final Activity activity;
	
	private final DataAdapter adapter = new DataAdapter();
	
	public DataView(Activity activity) {
		this.activity = activity;
		
		view = View.inflate(activity, R.layout.data_view, null);
		//view = (ScrollView) parent.findViewById(R.id.generic_data_view);
		//view = (LinearLayout) View.inflate(parent.getContext(), R.layout.generic_data_view, parent);
		
		this.title = (TextView) view.findViewById(R.id.title);
		this.data = (ListView) view.findViewById(R.id.data);
	}
	
	public void showData(String id, String instruction, String uri) {
		
		if(!scope.equals(this.scope)) {
			this.scope = scope;
			this.title.setText(title);
			redraw();
		}
	}
}
