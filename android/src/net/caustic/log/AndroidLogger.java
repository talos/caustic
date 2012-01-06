package net.caustic.log;

import android.content.Context;
import android.util.Log;

import net.caustic.android.R;

/**
 * Logger interface to LogCat.
 * @author talos
 *
 */
public class AndroidLogger implements Logger {

	private final Context context;
	
	public AndroidLogger(Context context) {
		this.context = context;
	}
	
	@Override
	public void e(Throwable e) {
		Log.e(context.getString(R.string.app_name), e.toString());
		e.printStackTrace();
	}

	@Override
	public void i(String infoText) {
		Log.i(context.getString(R.string.app_name), infoText);
	}
}
