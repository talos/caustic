package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticIntent;
import net.caustic.android.service.CausticIntent.CausticRequestIntent;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

public class CausticAndroidActivity extends Activity {
/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//this.startService(service)
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	*/
	
	private final DataUpdateReceiver receiver = new DataUpdateReceiver();
	private final IntentFilter filter = new IntentFilter(CausticIntent.REFRESH_DATA_INTENT);
	
	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.data_view);
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
		String loadID = ((WaitRow) view).getWaitingRequest();
	}
}
