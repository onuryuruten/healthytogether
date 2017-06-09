/**
 * 
 */
package ch.epfl.hci.healthytogether.service;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver only starts the {@link CheckForMessageService} that will then
 * check for new data on the server and shows a notification to the user.
 */
public class CheckForMessageAlarmReceiver extends BroadcastReceiver {

	/**
	 * onReceive must be very quick and not block, so it just fires up a Service
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("CheckMessageAlarmReceiver",
				"Starting CheckMessageService in background");
		WakefulIntentService.sendWakefulWork(context,
				CheckForMessageService.class);
		// context.startService(new Intent(context,
		// CheckForMessageService.class));
	}

}
