package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticServiceIntent;
import net.caustic.android.service.CausticIntentFilter;
import net.caustic.android.service.CausticServiceIntent.CausticForceIntent;
import net.caustic.android.service.CausticServiceIntent.CausticRefreshIntent;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CausticAndroidActivity extends Activity implements CausticAndroidButtons {

	private DataAdapter adapter;
	private DataUpdateReceiver receiver;
	private IntentFilter filter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		adapter = new DataAdapter();
		receiver = new DataUpdateReceiver(adapter);
		filter = new CausticIntentFilter();
		
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
	
	@Override
	/**
	 * This is called when a child row is clicked.
	 * @param view
	 */
	public void viewChild(View view) {
		String childId = (String) view.getTag(R.id.child_id);
		receiver.listenTo(childId);
		startService(CausticRefreshIntent.newRefresh(childId));
	}
	
	@Override
	/**
	 * This is called when a waiting row is clicked.
	 * @param view
	 */
	public void loadWaitingRequest(View view) {
		String waitId = (String) view.getTag(R.id.wait_id);
		startService(CausticForceIntent.newForce(waitId));
	}
}
