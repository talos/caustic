package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticIntentFilter;
import net.caustic.android.service.CausticServiceIntent.CausticForceIntent;
import net.caustic.android.service.CausticServiceIntent.CausticRefreshIntent;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CausticAndroidActivity extends Activity implements CausticAndroidButtons {

	/**
	 * Launch {@link CausticAndroidActivity} looking for some particular data.
	 * @param context
	 * @param observeID The {@link String} ID of data to initially display.
	 */
	public static void launch(Context context, String observeID) {
		Intent intent = new Intent(context, CausticAndroidActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.fromParts("caustic", observeID, null));
		context.startActivity(intent);
	}
	
	private DataAdapter adapter;
	private DataUpdateReceiver receiver;
	private IntentFilter filter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new DataAdapter();
		receiver = new DataUpdateReceiver(adapter);
		filter = new CausticIntentFilter();
		
		ListView dataView = (ListView) View.inflate(this, R.layout.data_view, null);
		dataView.setAdapter(adapter);
		
		// refresh for data immediately if supplied in intent.
		Intent intent = getIntent();
		if(intent.getAction() != null) {
			String id = intent.getData().getSchemeSpecificPart();
			startService(CausticRefreshIntent.newRefresh(id));
			receiver.listenTo(id);
		}
		
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
