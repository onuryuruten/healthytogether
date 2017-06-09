/**
 * 
 */
package ch.epfl.hci.healthytogether.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ch.epfl.hci.healthytogether.Constants;

/**
 * This receiver is started when the phone is booted up and it registers the
 * {@link CheckForMessageAlarmReceiver} to periodically check for new messages
 * and buddy invites on the server. For this purpose the {@link AlarmManager} is
 * used. Currently every 2 hours the {@link CheckForMessageAlarmReceiver} is
 * started that then checks for new data on the server.
 * 
 * This approach has been taken from
 * http://stackoverflow.com/questions/3859489/android
 * -running-a-background-task-using-alarmmanager
 * 
 */
public class CheckForLogBootReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		if (!Constants.loggedIn) {
			return;
		}

		Log.i("CheckMessageBootReceiver",
				"CheckMessageBootReceiver invoked, configuring AlarmManager");
		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// We create an intent that starts our AlarmReceiver that can be
		// executed by the AlarmManager on behalf of this app.
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(context, CheckForLogAlarmReceiver.class), 0);

		// use inexact repeating which is easier on battery (system can phase
		// events and not wake at exact times)
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Constants.ALARM_TRIGGER_AT_TIME, Constants.ALARM_INTERVAL,
				pendingIntent);
	}

}
