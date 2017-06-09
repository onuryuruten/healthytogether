package ch.epfl.hci.healthytogether.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.AcceptBuddyActivity;
import ch.epfl.hci.healthytogether.AppContext;
import ch.epfl.hci.healthytogether.Constants;
import ch.epfl.hci.healthytogether.IntroActivity;
import ch.epfl.hci.healthytogether.Main2Activity;
import ch.epfl.hci.healthytogether.SettingsActivity;
import ch.epfl.hci.healthytogether.communication.ServerHelper.AcceptBuddyRequestTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask;
import ch.epfl.hci.healthytogether.service.CheckForMessageAlarmReceiver;

public class Utils {

	private static final String TAG = Utils.class.toString();

	public static boolean isInteger(String s) {
		
		if(s == null || s == "")
		{
			return false;
		}
		
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static String removeNewLines(String s) {

		s = s.replace("\n", "");
		s = s.replace("\r", "");
		return s;
	}

	public static String dataCheck(String s) {
		return removeNewLines(s);
	}

	public String getTimestamp() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String str = dateFormat.format(date);
		return str;
	}

	/**
	 * Triggers the polling of the backend for friend requets, messages,
	 * synchronize with fitbti...
	 * 
	 * More technically:<br/>
	 * Schedule AlarmManager to invoke {@link CheckForMessageAlarmReceiver} and
	 * cancel any existing current PendingIntent. We do this because we *also*
	 * invoke the receiver from a BOOT_COMPLETED receiver so that we make sure
	 * the service runs either when app is installed/started, or when device
	 * boots
	 */
	public static void scheduleAlarmReceiver(Context ctx) {
		Log.i(TAG, "Configuring AlarmManager");
		AlarmManager alarmMgr = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0,
				new Intent(ctx, CheckForMessageAlarmReceiver.class),
				PendingIntent.FLAG_CANCEL_CURRENT);

		// Use inexact repeating which is easier on battery (system can phase
		// events and not wake at exact times)
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Constants.ALARM_TRIGGER_AT_TIME, Constants.ALARM_INTERVAL,
				pendingIntent);
	}

	/**
	 * @return <code>true</code> if a connection is present.
	 */
	/*
	 * public static boolean isConnectionPresent(Activity activity) {
	 * ConnectivityManager cMgr = (ConnectivityManager) activity
	 * .getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo netInfo =
	 * cMgr.getActiveNetworkInfo(); if ((netInfo != null) && (netInfo.getState()
	 * != null)) { return netInfo.getState().equals(State.CONNECTED); } return
	 * false; }
	 */

	public static boolean isConnectionPresent(Activity activity) {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		
		boolean myTest = activeNetworkInfo != null && activeNetworkInfo.isConnected();
		
		if(myTest)
		{
			SharedPreferences prefs = activity.getSharedPreferences(Constants.PROPERTIES_NAME,Context.MODE_PRIVATE);
			Constants.sendLatestLog(prefs);
		}
		
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	
	public static void performAcceptOperation(final Activity caller, final SharedPreferences prefs, final int uid, final String myEmail, final String buddyEmail)
	{
		if (prefs.getBoolean(Constants.PROP_KEY_GAME_STARTED, false)) 
		{
			// defriend the other poor buddy.
			// Toast.makeText(AcceptBuddyActivity.this, "Defriend..",
			// Toast.LENGTH_LONG).show();
			// int uid = AppContext.getInstance().getUserId();
			final int fid = AppContext.getInstance().getFriendId();
			DeFriendTask dfOldBuddy = new DeFriendTask(uid, fid) {

				@Override
				protected void onPostExecute(Boolean result) 
				{
					// Addition: so avoid the remnants
					/*SharedPreferences prefs2 = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);*/
					Constants.internalReset(caller, prefs);
					Constants.externalReset();
					if (fid > 0) {
						// Toast.makeText(AcceptBuddyActivity.this,
						// "Your are unpaired with your old buddy. Let's play with the new one!",
						// Toast.LENGTH_LONG).show();
					}
					performAcceptOperation(caller, prefs, uid, myEmail,buddyEmail);
				}
			};
			dfOldBuddy.execute();
		}
		else
		{
		
			AcceptBuddyRequestTask task = new AcceptBuddyRequestTask(uid, myEmail,buddyEmail) 
			{	
				@Override
				protected void onPostExecute(String result) {
					if (Utils.isInteger(result)) {
						Constants.INVITATION_RECEIVED = true;
						// The request was successful and the server returned the
						// friend's ID
						AppContext.getInstance().setFriendId(
								Integer.parseInt(result));
						Utils.scheduleAlarmReceiver(caller);
	
						// Remove the notification (in case a new one has been
						// issued)
						NotificationManager notificationManager = (NotificationManager) caller.getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager
								.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);
	
						// Set property to indicate invite has been accepted and
						// game started
	
						Editor editor = prefs.edit();
						editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, true);
						Constants.INVITATION_OUTCOME_DISPLAYED = true;
						editor.commit();
						Constants.embarkAcceptanceDate(prefs); // i.e., NOW
						Constants.clearNotificationDate(prefs);
						
						Intent i = caller.getIntent();
						
						i.removeExtra("message");
						
						caller.finish();
						caller.startActivity(i);
						
						
					} else {
	
						if (!Utils.isConnectionPresent(caller)) {
							Toast.makeText(
									caller,
									caller.getResources().getString(R.string.connection_toast_message),
									Toast.LENGTH_LONG).show();
						} else {
							//showProgressDialog(false);
							String msg = caller.getResources().getString(R.string.unfortunately_buddy_changed_mind);
							// Toast.makeText(AcceptBuddyActivity.this, msg,
							// Toast.LENGTH_LONG).show();
							Intent i = new Intent(caller, SettingsActivity.class); // previously:
																// InviteBuddyActivity
							i.putExtra("message", msg);
							caller.startActivity(i);
							caller.finish();
						}
					}
	
				}
			};
			task.execute();	
		}
	}

	public static boolean logout(Activity activity,SharedPreferences prefs2) 
	{
		AppContext.getInstance().setUserCredentialsSet(false);
		Constants.internalReset(activity,prefs2);
		// AppContext.getInstance().setFriendId(0);
		Intent i_login = new Intent(activity, IntroActivity.class);
		activity.startActivity(i_login);
		return true;
	}
	
	public static float getScrenPixelMultiplier(Activity activity, DisplayMetrics displayMetrics)
	{
		
		return displayMetrics.density;
		/*switch (activity.getResources().getDisplayMetrics().densityDpi) {
			case DisplayMetrics.DENSITY_LOW:
			    // ...
			    break;
			case DisplayMetrics.DENSITY_MEDIUM:
			    // ...
			    break;
			case DisplayMetrics.DENSITY_HIGH:
			    // ...
			    break;
			case DisplayMetrics.DENSITY_XHIGH:
			    // ...
			    break;
			default:
				return displayMetrics.xdpi;
		    	
		}*/
	}
	

}
