package net.caustic.android.activity;

import net.caustic.android.service.CausticIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(CausticIntent.REFRESH_DATA_INTENT)) {
			
		}

	}
}
