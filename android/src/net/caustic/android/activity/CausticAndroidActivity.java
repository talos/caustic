package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticIntentFilter;
import net.caustic.android.service.CausticServiceIntent;
import net.caustic.android.service.CausticServiceIntent.CausticForceIntent;
import net.caustic.android.service.CausticServiceIntent.CausticRefreshIntent;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class CausticAndroidActivity extends Activity implements CausticAndroidButtons {

	/**
	 * Launch {@link CausticAndroidActivity} looking for some particular data.
	 * @param context
	 * @param observeID The {@link String} ID of data to initially display.
	 */
	public static void launch(Context context, String observeID) {
		Intent intent = new Intent(context, CausticAndroidActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.fromParts(CausticServiceIntent.SCHEME, observeID, null));
		context.startActivity(intent);
	}
	
	private DataView view;
	private DataUpdateReceiver receiver;
	private IntentFilter filter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		receiver = new DataUpdateReceiver();
		filter = new CausticIntentFilter();
		
		ViewGroup main = (ViewGroup) View.inflate(this, R.layout.generic_data_view, null);
		view = new DataView(receiver, main);
		setContentView(main);
		/*
		ListView dataView = (ListView) View.inflate(this, R.layout.data_view, null);
		dataView.setAdapter(adapter);
		
		setContentView(dataView);*/
	}
	
	/*@Override
	protected void onStart() {
		super.onStart();
	}*/
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, filter);

		// refresh for data immediately if supplied in intent.
		//pending.
		//intent.putExtra("test", createPendingResult(1, intent, 0));
		//intent.get
		if(intent.getAction() != null) {
			String id = intent.getData().getSchemeSpecificPart();
			view.update(this, title, id);
			///startService(CausticRefreshIntent.newRefresh(id));
		}
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
