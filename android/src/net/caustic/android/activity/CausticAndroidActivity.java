package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticIntent;
import net.caustic.android.service.CausticIntent.CausticRequestIntent;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CausticAndroidActivity extends Activity {

	private DataAdapter adapter;
	private DataUpdateReceiver receiver;
	private IntentFilter filter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		adapter = new DataAdapter();
		receiver = new DataUpdateReceiver(adapter);
		filter = new IntentFilter(CausticIntent.REFRESH_DATA_INTENT);
		View dataView = View.inflate(this, R.layout.data_view, null);
		ListView data = (ListView) dataView.findViewById(R.id.data);
		data.setAdapter(adapter);
		
		setContentView(R.layout.data_view);
	}
	
	/*@Override
	protected void onStart() {
		super.onStart();
	}*/
	
	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	/**
	 * This is called when a child row is clicked.
	 * @param view
	 */
	public void viewChild(View view) {
		String childID = ((ChildRow) view).getCausticChildID();
		startService(CausticRequestIntent.newRequest(childID,  , uri, input, force));
	}
	
	/**
	 * This is called when a waiting row is clicked.
	 * @param view
	 */
	public void loadWaitingRequest(View view) {
		String loadID = ((WaitRow) view).getWaitingRequestID();
	}
}
