package net.caustic.android.activity;

import net.caustic.android.R;
import android.view.View;

/**
 * Implementations of this interface handle button clicks from {@link WaitRow}
 * and {@link ChildRow}.
 * @author talos
 *
 */
public interface CausticAndroidButtons {

	/**
	 * The ID of the child request can be found in the tag at {@link R.id#child_id}.
	 * @param view Use {@link View#getTag(int)} to get the ID of the child request.
	 * @author talos
	 *
	 */
	public void viewChild(View view);

	/**
	 * The ID of the waiting request can be found in the tag at {@link R.id#wait_id}.
	 * @param view Use {@link View#getTag(int)} to get the ID of the waiting request.
	 * @author talos
	 *
	 */
	public void loadWaitingRequest(View view);
}
